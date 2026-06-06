# Анализ и исправления потока данных JNI → Room

## Что было проверено

Путь данных в проекте сейчас выглядит так:

1. `app/src/main/cpp/src/jni/jni_capsule_manager.cpp` получает данные от Capsule SDK и вызывает методы `JniCallbackHandler`.
2. `app/src/main/java/com/neuroproject/neuro/jni/JniCallbackHandler.kt` является тонким JNI-мостом и прокидывает callbacks дальше в Kotlin.
3. `app/src/main/java/com/neuroproject/neuro/data/device/CapsuleSensorStreamAdapter.kt` подписывается на callbacks, превращает сырые аргументы JNI в domain-модели и публикует их в `SharedFlow`.
4. `app/src/main/java/com/neuroproject/neuro/services/RecordManager.kt` слушает эти `Flow`, добавляет `userId`, `expeditionId`, `sessionId` и отправляет данные в репозиторий Room.
5. `app/src/main/java/com/neuroproject/neuro/data/MetricsRepository.kt` создаёт Room entities, пишет сырые и сжатые данные через `MetricsDao`.
6. `app/src/main/java/com/neuroproject/neuro/data/MetricsDao.kt` выполняет insert/select/update для таблиц метрик.
7. `app/src/main/java/com/neuroproject/neuro/data/MetricEntities.kt` описывает таблицы Room.

## Главная причина падения Room

Почти все таблицы метрик использовали `timestamp` как единственный `@PrimaryKey`:

```kotlin
@PrimaryKey val timestamp: Long
```

Для потоков от Capsule/JNI это некорректно. Несколько сэмплов внутри одного типа данных могут прийти с одинаковой миллисекундной меткой. Также `timestamp = 0` мог появляться из default domain-моделей или некорректного callback. В результате Room падал на конфликте первичного ключа.

Дополнительная проблема: синхронизация помечала загруженные строки через `WHERE timestamp IN (...)`. После разрешения дублей timestamp это могло помечать больше строк, чем реально было выгружено.

## Что исправлено

### 1. `timestamp` больше не является primary key

Во все таблицы метрик и compressed-метрик добавлен технический первичный ключ:

```kotlin
@PrimaryKey(autoGenerate = true) val rowId: Long = 0
val timestamp: Long
```

`timestamp` оставлен как обычное поле данных, потому что он нужен для сортировки, выгрузки, агрегации и отображения.

Затронуты таблицы:

- `nfb_metrics`, `nfb_metrics_compressed`
- `EEG_Raw_metrics`, `EEG_Raw_metrics_compressed`
- `EEG_Proceed_metrics`, `EEG_Proceed_metrics_compressed`
- `EEG_Artifacts_metrics`, `EEG_Artifacts_metrics_compressed`
- `physiological_metrics`, `physiological_metrics_compressed`
- `physiological_baselines`
- `mems_metrics`, `mems_metrics_compressed`
- `productivity_metrics`, `productivity_metrics_compressed`
- `productivity_indexes`
- `productivity_baselines`
- `emotional_metrics`, `emotional_metrics_compressed`
- `cardio_metrics`, `cardio_metrics_compressed`

### 2. Добавлены индексы для частых запросов

Для таблиц метрик добавлены индексы:

```kotlin
indices = [
    Index(value = ["sessionId", "timestamp"]),
    Index(value = ["timestamp"])
]
```

Это сохраняет быстрый доступ к данным по сессии и времени без требования уникальности `timestamp`.

### 3. Пометка синхронизации переведена с `timestamp` на `rowId`

В `MetricsDao` запросы вида:

```sql
UPDATE ... SET isMarked = 1 WHERE timestamp IN (:timestamps)
```

заменены на:

```sql
UPDATE ... SET isMarked = 1 WHERE rowId IN (:rowIds)
```

В `MetricsUploadRepository` теперь передаются `metrics.map { it.rowId }`, а не `metrics.map { it.timestamp }`.

Это важно, потому что теперь две строки могут иметь одинаковый `timestamp`, но всегда имеют разные `rowId`.

### 4. Добавлена нормализация timestamp перед записью

В `MetricsRepository` добавлена функция:

```kotlin
private fun normalizeTimestamp(timestamp: Long): Long =
    if (timestamp > 0L) timestamp else System.currentTimeMillis()
```

При создании entities используется нормализованное значение. Это защищает БД от пачек записей с `timestamp = 0`, которые появлялись бы при дефолтных моделях или некорректном callback.

### 5. Убрано отбрасывание NFB по `alpha > 1.0`

Раньше NFB-сэмплы отбрасывались так:

```kotlin
if (alpha > 1.0) return
```

Это было опасно, потому что данные исчезали до записи в БД. Сейчас поток не режется по такому правилу. Оставлена только проверка на `NaN`/`Infinity`.

### 6. Добавлена заготовка для обработки артефактов

Добавлен файл:

```text
app/src/main/java/com/neuroproject/neuro/data/SensorArtifactProcessor.kt
```

Сейчас это intentionally no-op конструкция: она не меняет политику обработки и не отбрасывает валидные данные по артефактам. Она только централизует место, куда позже можно добавить правила.

Пример будущего расширения:

```kotlin
class SensorArtifactProcessor {
    fun shouldStoreNfb(...): Boolean = ...
    fun shouldStoreArtifacts(...): Boolean = ...
    fun shouldStorePhysiological(...): Boolean = ...
}
```

Сейчас через неё проходят проверки на нечисловые значения (`NaN`, `Infinity`) для NFB, EEG, EEG artifacts, physiological, MEMS, productivity, emotional и cardio.

### 7. Версия БД повышена

В `MetricsDatabase` версия схемы повышена с `3` до `4`.

Также добавлен `fallbackToDestructiveMigration()`, потому что смена primary key для большого набора таблиц без полноценной миграции требует пересоздания таблиц. Для разработки это самый простой безопасный вариант, чтобы Room не падал на несовпадении схемы.

Если нужно сохранить старые пользовательские данные при обновлении приложения, вместо destructive migration нужно написать полноценную миграцию `3 → 4` с пересозданием каждой таблицы и копированием данных.

## Оставшиеся проблемы и рекомендации

### 1. `isMarked` используется как признак синхронизации, а не как признак артефакта

Сейчас поле `isMarked` фактически означает `isSynced`. Его лучше переименовать в будущем, чтобы не путать с артефактами.

Рекомендация:

```kotlin
val isSynced: Boolean
```

Если нужно хранить артефактность, добавьте отдельные поля, например:

```kotlin
val hasArtifacts: Boolean
val artifactReason: String?
```

### 2. Полноценная обработка артефактов пока не реализована по смыслу

Заготовка есть в `SensorArtifactProcessor`, но бизнес-правила нужно определить отдельно:

- сохранять все сэмплы, но помечать артефактные;
- сохранять сырые данные всегда, а compressed считать только по чистым данным;
- отбрасывать только физически невозможные значения;
- отдельно учитывать `EEG_Artifacts_metrics` при обработке NFB/physiological/productivity.

Рекомендованный подход: сырые данные сохранять всегда, артефактность хранить отдельно, а фильтрацию применять только в аналитике и compression.

### 3. `RecordManager` использует `CoroutineScope(EmptyCoroutineContext)`

Это не лучшая практика. Лучше использовать scope с `SupervisorJob() + Dispatchers.IO` или передавать application scope через DI. Иначе коллекции могут выполняться в неожиданном контексте.

Рекомендация:

```kotlin
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
```

### 4. `MetricsRepository` создаёт собственный `CoroutineScope`

Методы domain-интерфейса объявлены как `suspend`, но legacy repository внутри запускает `scope.launch`. Это делает запись fire-and-forget: вызывающий код не знает, завершилась ли запись.

Рекомендация: постепенно перевести методы сохранения на настоящие `suspend`-функции без внутреннего `launch`.

### 5. Сжатие physiological сейчас выглядит временным

В `flushPhysiologicalBuffer()` используется первый элемент вместо медианы, а поле `none` заполнялось значением `fatigue`. Это стоит пересмотреть отдельно.

Рекомендация: привести physiological compression к той же логике, что и остальные compressed-таблицы: median для числовых полей и majority для boolean-флагов.

### 6. Для production лучше написать миграцию `3 → 4`

Текущий вариант с `fallbackToDestructiveMigration()` хорош для быстрого исправления схемы и устранения падения Room, но удалит старую локальную БД при несовместимой миграции.

Production-вариант:

1. Создать новые таблицы с `rowId`.
2. Скопировать данные из старых таблиц.
3. Удалить старые таблицы.
4. Переименовать новые таблицы.
5. Создать индексы.

## Проверка

Я попытался запустить:

```bash
./gradlew :app:compileDebugKotlin --no-daemon
```

Запуск не дошёл до компиляции, потому что Gradle Wrapper попытался скачать Gradle с `services.gradle.org`, а в окружении нет доступа к сети. До этого у `gradlew` также были CRLF-переносы строк, из-за чего Linux не мог его запустить; файл был нормализован до LF.

## Изменённые файлы

- `app/src/main/java/com/neuroproject/neuro/data/MetricEntities.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsDao.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsDatabase.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsRepository.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsUploadRepository.kt`
- `app/src/main/java/com/neuroproject/neuro/data/SensorArtifactProcessor.kt`
- `gradlew` — нормализованы CRLF → LF для запуска в Linux/macOS.
