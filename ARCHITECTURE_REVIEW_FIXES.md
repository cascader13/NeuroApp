# Анализ общей архитектуры NeuroApp и внесённые корректировки

## Краткий вывод

Архитектура уже двигается в правильную сторону: в проекте есть разделение на `presentation`, `domain`, `data`, `services`, `jni`, Room-слой и WorkManager-синхронизацию. Но сейчас приложение всё ещё находится в переходном состоянии: часть старой логики живёт в singleton-сервисах и legacy-репозиториях, а часть уже оформлена через domain-контракты/use case. Из-за этого основные риски не в UI, а в жизненном цикле фоновых задач, согласованности записи сенсорных данных, синхронизации и конфигурации серверов.

В первую очередь нужно смотреть на путь:

```text
JNI/Capsule callbacks
    → SensorStreamGateway / CapsuleSensorStreamAdapter
    → RecordManager
    → MetricsRepository
    → MetricsDao / Room
    → MetricsUploadRepository
    → WorkManager или ручной upload
    → backend
```

Именно здесь любая гонка, некорректный scope, фильтр данных или повторная синхронизация приводит к потере измерений, дублям на сервере или «тихим» ошибкам.

---

## Что нужно проверить и стабилизировать в первую очередь

### 1. Жизненный цикл coroutine-задач

Проблема: в проекте были неуправляемые `CoroutineScope` внутри singleton-классов:

- `MetricsRepository` создавал собственный `CoroutineScope(Dispatchers.IO)`;
- `RecordManager` использовал `CoroutineScope(EmptyCoroutineContext)`;
- часть callback-логики в БД тоже создаёт scope напрямую.

Почему это важно: такие scope не связаны с Hilt/Application lifecycle, их сложно тестировать, отменять и контролировать. Для потоковых сенсорных данных это особенно опасно: запись в Room может продолжаться после смены сценария, а ошибки внутри launch могут потеряться в логах.

Что сделано:

- добавлены Hilt-квалификаторы `@ApplicationScope` и `@IoDispatcher`;
- добавлен `CoroutinesModule`;
- `MetricsRepository` теперь использует application-level scope и IO dispatcher из DI;
- `RecordManager` теперь использует application scope вместо `EmptyCoroutineContext`.

Файлы:

- `app/src/main/java/com/neuroproject/neuro/Qualifiers.kt`
- `app/src/main/java/com/neuroproject/neuro/di/CoroutinesModule.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsRepository.kt`
- `app/src/main/java/com/neuroproject/neuro/data/DatabaseModule.kt`
- `app/src/main/java/com/neuroproject/neuro/services/RecordManager.kt`

Рекомендация дальше: постепенно перевести методы сохранения метрик из fire-and-forget `fun save... { appScope.launch { ... } }` в явные `suspend fun save...`, а запуск корутин оставить на уровне источника событий. Это даст backpressure и позволит тестировать запись без ожидания фоновых launch.

---

### 2. Конкурентная синхронизация: ручной upload + WorkManager

Проблема: после добавления WorkManager появилась возможность одновременной отправки одних и тех же строк:

```text
UI ручная синхронизация читает isMarked = 0
WorkManager почти одновременно читает isMarked = 0
оба отправляют одинаковые DTO на backend
оба пытаются пометить строки как отправленные
```

Даже если Room-строки потом корректно пометятся по `rowId`, backend может получить дубли.

Что сделано:

- в `MetricsUploadRepository` добавлен `syncMutex`;
- весь `uploadInBatches()` защищён от одновременного запуска в рамках процесса приложения;
- ручная синхронизация и Worker продолжают использовать один и тот же репозиторий, поэтому поведение одинаковое.

Файл:

- `app/src/main/java/com/neuroproject/neuro/data/MetricsUploadRepository.kt`

Рекомендация дальше: добавить серверную идемпотентность. Например, отправлять стабильный `clientRecordId`/`rowId`/`sessionId + timestamp + type + localRowId` и делать upsert на backend. Клиентский mutex защищает только один процесс приложения, но не защищает от повторной отправки после переустановки, сбоя сети или повторного replay failed batch.

---

### 3. Фильтрация сенсорных данных до Room

Проблема: в `RecordManager` оставался фильтр `alpha <= 1.0`, который мог отбрасывать NFB до попадания в `MetricsRepository`. Ранее аналогичная логика уже была смягчена в `SensorArtifactProcessor`, но второй фильтр всё ещё жил выше по цепочке.

Что сделано:

- убрана проверка `alpha <= 1.0` из `RecordManager`;
- ответственность за валидацию/артефакты оставлена в `SensorArtifactProcessor` и `MetricsRepository`.

Файл:

- `app/src/main/java/com/neuroproject/neuro/services/RecordManager.kt`

Рекомендация дальше: оставить в `RecordManager` только проверку контекста записи: `userId`, `expeditionId`, `sessionId`, `isRecording`. Все проверки качества сигнала, диапазонов и артефактов должны жить в отдельном policy/processor-классе.

---

### 4. Конфигурация серверов и Retrofit baseUrl

Проблема: Retrofit требует, чтобы `baseUrl` заканчивался `/`. Для основного сервера это уже исправлено, но для сервера мониторинга часть дефолтов и пользовательский ввод могли сохраняться без завершающего `/`.

Что сделано:

- дефолтный URL мониторинга приведён к виду `http://10.240.68.80:5000/`;
- `ServerAddressPreferences` нормализует URL при чтении и записи;
- `DynamicServerManager` нормализует URL перед созданием Retrofit;
- `SettingsViewModel` нормализует введённый пользователем адрес и отображает уже нормализованное значение.

Файлы:

- `app/src/main/java/com/neuroproject/neuro/data/MonitorNetworkModule.kt`
- `app/src/main/java/com/neuroproject/neuro/services/DynamicServerManager.kt`
- `app/src/main/java/com/neuroproject/neuro/presentation/screens/settings/SettingsViewModel.kt`

Рекомендация дальше: вынести адреса серверов из исходников в `BuildConfigField`/product flavors/environment config. Сейчас IP-адреса зашиты в код, а `android:usesCleartextTraffic="true"` разрешает HTTP для всего приложения.

---

### 5. Логирование и отладочный код

Проблема: в `MetricsUploadRepository` были `println(...)`. Для Android это хуже, чем `Log`: сложнее фильтровать, сложнее отключать по уровню, неудобно анализировать в Logcat.

Что сделано:

- `println` в `MetricsUploadRepository` заменён на `Log.d/i/w/e`;
- добавлен единый `TAG`.

Файл:

- `app/src/main/java/com/neuroproject/neuro/data/MetricsUploadRepository.kt`

Рекомендация дальше: для сетевого логирования использовать `HttpLoggingInterceptor.Level.BASIC`/`NONE` в release, а `BODY` оставлять только в debug. Сейчас BODY может случайно логировать большие JSON-пакеты и персональные данные.

---

## Архитектурные проблемы, которые ещё стоит закрыть

### A. Legacy `MetricsRepository` смешивает слишком много ответственностей

Сейчас он одновременно:

- принимает данные от записи;
- валидирует часть значений;
- пишет в Room;
- буферизует данные для compressed-таблиц;
- решает, когда flush-ить buffers.

Лучше разделить:

```text
SensorSampleValidator / SensorArtifactProcessor
MetricWriter / RoomMetricDataSource
MetricCompressor / MetricAggregationBuffer
MetricsRepository как orchestration layer
```

Это упростит тестирование и позволит отдельно проверять запись raw-данных, compressed-данных и artifact-policy.

### B. Fire-and-forget запись в Room

Даже после перевода scope в DI методы сохранения всё ещё запускают внутренний `launch`. Это лучше, чем неуправляемый scope, но всё ещё не идеально.

Целевая форма:

```kotlin
suspend fun saveNFBMetric(...)
```

А collect из потока:

```kotlin
sensorStreamGateway.observeNFB().collect { sample ->
    metricsRepository.saveNFBMetric(...)
}
```

Так будет понятнее, когда запись реально завершилась.

### C. Миграции Room

Сейчас используется `fallbackToDestructiveMigration()`. Это удобно на этапе разработки, но опасно для реальных данных: при несовместимой схеме база будет пересоздана.

Следующий шаг: заменить destructive fallback на явные `Migration(4, 5)`, `Migration(5, 6)` и хранить schema JSON в репозитории.

### D. Дублирование BaseViewModel

В проекте есть минимум два похожих файла:

- `domain/BaseViewModel.kt`
- `presentation/BaseViewModel.kt`

`BaseViewModel` относится к presentation-слою, поэтому domain-слой не должен зависеть от Android/ViewModel-концепций. Лучше оставить один вариант в `presentation/common` или `presentation/base`.

### E. SettingsViewModel слишком большой

`SettingsViewModel` отвечает за:

- настройки пользователя;
- адрес сервера;
- ручную синхронизацию;
- статистику БД;
- сохранение JSON;
- прогресс upload.

Стоит разделить на use case/manager-ы:

- `ObserveUploadStatsUseCase`;
- `RunManualSyncUseCase`;
- `SaveServerSettingsUseCase`;
- `ExportMetricsUseCase`.

### F. Мониторинговый сервер и основной сервер имеют разные механизмы конфигурации

Основной сервер задан константой в `NetworkModule`, мониторинговый — через SharedPreferences и `DynamicServerManager`. Лучше привести к единой схеме конфигурации.

### G. Нужны тесты на самый критичный путь

Минимальный набор тестов:

1. `SensorArtifactProcessorTest`: NaN/Infinity/валидные значения.
2. `MetricsRepositoryTest`: одинаковые timestamp сохраняются как разные строки.
3. `MetricsUploadRepositoryTest`: `result=false` не помечает строки как отправленные.
4. `MetricsUploadRepositoryTest`: параллельные upload-запуски не читают один и тот же batch одновременно.
5. `ServerAddressPreferencesTest`: URL всегда заканчивается `/`.
6. `MetricsSyncWorkerTest`: `NoData` и `Completed` дают success, ошибки дают retry/failure.

---

## Внесённые корректировки

### Добавлены

- `app/src/main/java/com/neuroproject/neuro/di/CoroutinesModule.kt`
- `@ApplicationScope`
- `@IoDispatcher`

### Изменены

- `Qualifiers.kt`
- `DatabaseModule.kt`
- `MetricsRepository.kt`
- `RecordManager.kt`
- `MetricsUploadRepository.kt`
- `MonitorNetworkModule.kt`
- `DynamicServerManager.kt`
- `SettingsViewModel.kt`

---

## Что я не стал менять радикально

Я не стал полностью переписывать `MetricsRepository` на suspend API и не стал удалять legacy-слой, потому что это затронет много экранов, сервисов и use case. Сейчас внесены точечные изменения, которые уменьшают риск гонок и ошибок жизненного цикла, но не ломают текущую структуру проекта.

---

## Проверка сборки

Gradle-сборку в этом окружении запустить не удалось: wrapper пытается скачать Gradle с `services.gradle.org`, а сетевой доступ недоступен.

Команда, которую нужно выполнить локально:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

После сборки особенно проверь:

1. запуск приложения;
2. открытие Settings;
3. ручную синхронизацию;
4. запуск WorkManager-синхронизации;
5. запись сессии с реальной гарнитуры;
6. создание Retrofit для мониторингового сервера с адресом без `/` и с `/`.

---

## Приоритетный план дальнейшей доработки

1. Перевести запись метрик на `suspend` без внутреннего `launch`.
2. Добавить явные Room migrations вместо `fallbackToDestructiveMigration()`.
3. Ввести server-side идемпотентность для upload.
4. Разделить `MetricsRepository` на writer/compressor/validator.
5. Вынести server config в BuildConfig/product flavors.
6. Уменьшить `SettingsViewModel`, вынести sync/export/settings в use cases.
7. Добавить unit/integration-тесты для записи и синхронизации.
