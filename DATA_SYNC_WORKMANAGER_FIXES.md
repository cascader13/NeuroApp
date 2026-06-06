# Анализ отправки данных на backend и правки синхронизации

## Что проверено

Сопоставлен Android-клиент с backend `backend_arctic_team-main`:

- endpoint загрузки метрик на backend: `POST /api/metrics/upload` и дублирующий v1 endpoint `POST /api/v1/metrics/upload`;
- backend DTO `UploadRequest` и все вложенные DTO в `metrics/model/dto/request`;
- клиентские DTO в `UploadDto.kt`;
- преобразование Room entity → network DTO в `EntityToDtoExtensions.kt`;
- логика пакетной отправки и пометки строк как синхронизированных в `MetricsUploadRepository.kt`;
- возможность фоновой отправки через WorkManager.

## Найденные проблемы

### 1. Retrofit baseUrl был без завершающего `/`

В `NetworkModule.kt` был указан URL:

```kotlin
private const val BASE_URL = "http://159.194.217.94:8080"
```

Для Retrofit baseUrl должен оканчиваться `/`. Иначе Retrofit может падать при создании клиента с `IllegalArgumentException: baseUrl must end in /`.

Исправлено:

```kotlin
private const val BASE_URL = "http://159.194.217.94:8080/"
```

### 2. Клиент считал HTTP 200 успешным даже при `result=false`

Backend в `UploadServiceImpl` ловит исключения и возвращает:

```json
{"result": false}
```

При этом HTTP-код остаётся 200. Раньше клиент проверял только `response.isSuccessful`, поэтому мог помечать локальные строки как отправленные, хотя backend их не сохранил.

Исправлено: успешной отправкой теперь считается только:

```kotlin
response.isSuccessful && response.body()?.result == true
```

Если backend вернул `result=false`, пакет считается неуспешным, JSON сохраняется в `failed_requests`, а WorkManager сможет выполнить retry.

### 3. `rowId` использовался для пометки Room-строк, но отсутствовал в DTO

После предыдущего исправления Room primary key был перенесён с `timestamp` на `rowId`, и `MetricsUploadRepository.markBatchAsUploaded()` уже помечал строки по `rowId`.

Но DTO-классы в `UploadDto.kt` не содержали `rowId`, из-за чего код с `metrics.map { it.rowId }` был неконсистентным.

Исправлено:

- во все DTO метрик, baseline и indexes добавлено локальное поле:

```kotlin
@Transient
val rowId: Long = 0L
```

- `@Transient` не отправляет поле в JSON, поэтому backend не получает лишнее поле;
- `EntityToDtoExtensions.kt` теперь переносит `rowId` из Room entity в DTO;
- пометка `isMarked = 1` после успешной отправки работает по реальным локальным ключам.

### 4. Сессии помечались как отправленные по неправильному идентификатору

Для `sessionResults` в DTO поле `session` — это значение для backend, преобразованное из `sessionId` в секунды. Раньше оно использовалось и для локальной пометки:

```kotlin
sessionDao.safeMarkSessionResultAsSynced(metrics.map { it.session })
```

Но локальный primary key в Room — `SessionEntity.sessionId: Long` в миллисекундах. Поэтому сессии могли не помечаться как синхронизированные.

Исправлено:

- в `SessionDto` добавлено локальное transient-поле:

```kotlin
@Transient
val localSessionId: Long? = null
```

- `SessionEntity.toServerDto()` сохраняет туда исходный `sessionId`;
- `SessionDao.markSessionResultAsSynced()` и `safeMarkSessionResultAsSynced()` переведены с `List<Int>` на `List<Long>`;
- пометка сессий теперь идёт по локальному `sessionId`, а не по API-полю `session`.

### 5. `UploadRequest.hasData()` не учитывал `sessionResults`

Если пакет содержал только результаты сессии, `hasData()` мог считать его пустым.

Исправлено: `sessionResult?.isNotEmpty() == true` добавлен в проверку.

### 6. Неполные sessionResults могли ломать весь upload на backend

Backend в `SessionDto.toEntity()` делает:

```java
Long.valueOf(dto.expeditionId())
```

Если клиент отправит сессию без `expeditionId` или `individualNumber`, backend упадёт внутри upload и вернёт `result=false`.

Исправлено на клиенте: к отправке допускаются только sessionResults, у которых заполнены `id` и `expedition_id`.

```kotlin
val sessions = sessionDao.getUnmarkedSessionResult()
    .filter { !it.id.isNullOrBlank() && !it.expedition_id.isNullOrBlank() }
```

## Реализация WorkManager

Добавлены файлы:

- `app/src/main/java/com/neuroproject/neuro/data/sync/MetricsSyncWorker.kt`
- `app/src/main/java/com/neuroproject/neuro/data/sync/MetricsSyncScheduler.kt`

Добавлены зависимости в `app/build.gradle.kts`:

```kotlin
implementation("androidx.hilt:hilt-work:1.2.0")
kapt("androidx.hilt:hilt-compiler:1.2.0")
implementation("androidx.work:work-runtime-ktx:2.9.1")
```

`MainApplication` теперь реализует `Configuration.Provider` и подключает `HiltWorkerFactory`.

### Как работает `MetricsSyncWorker`

Worker использует тот же `MetricsUploadRepository.uploadInBatches()`, что и ручная синхронизация. Это важно: ручной и фоновый режимы имеют одинаковые правила пакетирования, retry внутри пакета, проверку `result=true` и пометку Room-строк.

Логика результата:

- `Completed` или `NoData` → `Result.success()`;
- `PartialSuccess`, `Stopped`, `Error` → `Result.retry()` до лимита попыток;
- после превышения лимита → `Result.failure()`.

### Как включать отправку через WorkManager

Для разовой фоновой отправки:

```kotlin
metricsSyncScheduler.enqueueManualBackgroundSync()
```

Для периодической фоновой отправки:

```kotlin
metricsSyncScheduler.enablePeriodicSync(repeatIntervalMinutes = 60)
```

Для отключения периодической отправки:

```kotlin
metricsSyncScheduler.disablePeriodicSync()
```

Периодический WorkManager не может запускаться чаще чем раз в 15 минут, поэтому в scheduler стоит нижняя граница `15` минут.

## Ручная синхронизация сохранена

Существующая ручная синхронизация через `MetricsUploadRepository.uploadInBatches()` не удалялась. UI может продолжать вызывать её напрямую и получать `Flow<BatchUploadProgress>` для отображения прогресса.

WorkManager добавлен как дополнительный путь запуска той же синхронизации, а не как замена ручной отправки.

## Изменённые файлы

- `app/src/main/java/com/neuroproject/neuro/data/NetworkModule.kt`
- `app/src/main/java/com/neuroproject/neuro/data/remote/UploadDto.kt`
- `app/src/main/java/com/neuroproject/neuro/data/EntityToDtoExtensions.kt`
- `app/src/main/java/com/neuroproject/neuro/data/MetricsUploadRepository.kt`
- `app/src/main/java/com/neuroproject/neuro/data/session/SessionDao.kt`
- `app/src/main/java/com/neuroproject/neuro/MainApplication.kt`
- `app/src/main/java/com/neuroproject/neuro/data/sync/MetricsSyncWorker.kt`
- `app/src/main/java/com/neuroproject/neuro/data/sync/MetricsSyncScheduler.kt`
- `app/build.gradle.kts`

## Рекомендации для backend

1. Лучше возвращать HTTP 4xx/5xx при ошибке сохранения, а не HTTP 200 с `result=false`. Сейчас клиент это обрабатывает, но стандартная HTTP-семантика была бы надёжнее.
2. Добавить в `UploadResponse` текст ошибки или код ошибки. Сейчас клиент видит только `result=false`, без причины.
3. На backend желательно добавить Bean Validation для обязательных полей `individualNumber`, `expeditionId`, `timestamp`, `session`.
4. Для `SessionDto` стоит безопасно валидировать `expeditionId` до `Long.valueOf()`, иначе одна плохая сессия откатывает весь upload.
5. Рассмотреть идемпотентность загрузки: сейчас при retry backend может получить дубли, если часть данных была сохранена перед ошибкой. Лучше добавить естественный уникальный ключ или `clientRowId`/`deviceRecordId`.
6. В перспективе лучше разделить endpoint по типам данных или добавить серверную поддержку batch id, чтобы клиент мог подтверждать частичный успех точнее.

## Рекомендации для Android-клиента

1. В UI добавить явный переключатель фоновой синхронизации, который вызывает `enablePeriodicSync()` / `disablePeriodicSync()`.
2. Показывать пользователю количество неотправленных записей из `getStats()`.
3. Добавить экран/экспорт для `filesDir/failed_requests`, чтобы быстрее разбирать проблемные JSON.
4. Если сессии без `id`/`expedition_id` должны когда-то отправляться, нужно сначала определить на backend допустимую схему для таких записей. Сейчас они намеренно не отправляются, чтобы не ломать весь пакет.
5. Для больших потоков EEG/MEMS лучше держать небольшой `batchSize`, например 50–100, потому что backend дополнительно flush-ит по 50 записей.

## Что не удалось проверить автоматически

Gradle-сборку запустить не удалось: wrapper снова пытается скачать Gradle с `services.gradle.org`, а в окружении нет доступа к сети. Проверены структура проекта, баланс скобок изменённых Kotlin-файлов и соответствие клиентских JSON-полей backend DTO.
