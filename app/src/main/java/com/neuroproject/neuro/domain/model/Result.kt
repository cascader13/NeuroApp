package com.neuroproject.neuro.domain.model

/**
 * Универсальный класс для представления результата операции
 * 
 * Используется для единообразной обработки ошибок во всех UseCases и Repository.
 * Позволяет избежать try-catch в ViewModel и работать с результатами через when.
 * 
 * @param T Тип успешных данных
 * 
 * Пример использования:
 * ```kotlin
 * when (val result = useCase()) {
 *     is Result.Success -> showData(result.data)
 *     is Result.Error -> showError(result.exception)
 *     is Result.Loading -> showLoading()
 * }
 * ```
 */
sealed class Result<out T> {
    /**
     * Успешное завершение операции с данными
     * @property data Результат операции
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Ошибка выполнения операции
     * @property exception Исключение с подробностями ошибки
     * @property message Опциональное сообщение об ошибке для UI
     */
    data class Error(
        val exception: Throwable,
        val message: String? = exception.message
    ) : Result<Nothing>()
    
    /**
     * Операция в процессе выполнения (для long-running задач)
     */
    object Loading : Result<Nothing>()
    
    /**
     * Проверка: является ли результат успешным
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Проверка: является ли результат ошибкой
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Проверка: является ли результат состоянием загрузки
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Получить данные если это Success, иначе null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Получить сообщение об ошибке если это Error, иначе null
     */
    fun getErrorMessage(): String? = when (this) {
        is Error -> message ?: exception.message
        else -> null
    }
}

/**
 * Extension-функция для выполнения suspend-функции и оборачивания результата в Result
 * 
 * Автоматически ловит исключения и маппит их в Result.Error
 * 
 * Пример:
 * ```kotlin
 * suspend fun getUsers(): Result<List<User>> = runCatching {
 *     userRepository.getUsers() // может бросить исключение
 * }
 * ```
 */
suspend fun <T> runCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}

/**
 * Extension-функция для выполнения обычной функции и оборачивания результата в Result
 */
fun <T> runCatchingSync(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}

/**
 * Маппинг данных из одного Result в другой
 * 
 * Пример:
 * ```kotlin
 * val userNames: Result<List<String>> = getUsers().map { users ->
 *     users.map { it.name }
 * }
 * ```
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * Выполнение действия над данными если это Success
 * Не изменяет результат, только выполняет side-effect
 * 
 * Пример:
 * ```kotlin
 * getUsers()
 *     .onSuccess { analytics.logUsersLoaded(it.size) }
 *     .collect { ... }
 * ```
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Выполнение действия при ошибке
 * 
 * Пример:
 * ```kotlin
 * getUsers()
 *     .onError { logger.logError(it.exception) }
 *     .collect { ... }
 * ```
 */
inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(this)
    }
    return this
}

/**
 * Получение данных или значение по умолчанию при ошибке
 * 
 * Пример:
 * ```kotlin
 * val users = getUsers().getOrElse(emptyList())
 * ```
 */
inline fun <T> Result<T>.getOrElse(defaultValue: () -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error, is Result.Loading -> defaultValue()
    }
}

/**
 * Получение данных или null при ошибке
 */
fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception
        is Result.Loading -> throw IllegalStateException("Result is still loading")
    }
}
