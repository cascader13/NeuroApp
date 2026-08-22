package com.neuroproject.neuro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroproject.neuro.domain.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Базовый класс для всех ViewModel
 * 
 * Предоставляет:
 * - Унифицированный способ работы с StateFlow
 * - Стандартную обработку загрузки и ошибок
 * - Общие helper-методы для запуска корутин
 * 
 * Пример использования:
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     private val myUseCase: MyUseCase
 * ) : BaseViewModel<MyState>() {
 *     
 *     init {
 *         setState { copy(isLoading = true) }
 *         loadData()
 *     }
 *     
 *     private fun loadData() {
 *         safeLaunch {
 *             val result = myUseCase()
 *             when (result) {
 *                 is Result.Success -> setState { copy(data = result.data, isLoading = false) }
 *                 is Result.Error -> setState { copy(error = result.message, isLoading = false) }
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * @param T Тип состояния экрана (обычно data class)
 */
abstract class BaseViewModel<T : Any> : ViewModel() {
    
    /**
     * Внутренний изменяемый StateFlow
     * Доступен только внутри ViewModel
     */
    private lateinit var _state: MutableStateFlow<T>
    
    /**
     * Публичный неизменяемый StateFlow
     * Используется в UI для подписки на состояние
     */
    val state: StateFlow<T>
        get() {
            ensureInitialized()
            return _state.asStateFlow()
        }
    
    /**
     * Создание начального состояния экрана
     * Должно быть переопределено в дочерних классах
     */
    protected abstract fun createInitialState(): T

    protected fun initializeState() {
        if (!::_state.isInitialized) {
            _state = MutableStateFlow(createInitialState())
        }
    }

    private fun ensureInitialized() {
        if (!::_state.isInitialized) {
            initializeState()
        }
    }
    
    /**
     * Обновление состояния
     * 
     * @param reducer Лямбда для трансформации текущего состояния
     * 
     * Пример:
     * ```kotlin
     * setState { copy(isLoading = true, error = null) }
     * ```
     */
    protected fun setState(reducer: T.() -> T) {
        ensureInitialized()
        _state.update { currentState ->
            currentState.reducer()
        }
    }
    
    /**
     * Безопасный запуск корутины с автоматической обработкой ошибок
     * 
     * Автоматически ловит исключения и обновляет состояние с ошибкой.
     * Переопределите [com.neuroproject.neuro.domain.model.onError] для кастомной обработки ошибок.
     * 
     * @param block Блок кода для выполнения
     * 
     * Пример:
     * ```kotlin
     * safeLaunch {
     *     val data = repository.getData()
     *     setState { copy(data = data) }
     * }
     * ```
     */
    protected fun safeLaunch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    /**
     * Безопасный запуск корутины с Result<T> паттерном
     * 
     * Автоматически оборачивает результат в Result и обрабатывает ошибки.
     * 
     * @param block Блок кода, возвращающий Result<T>
     * @param onSuccess Обработчик успешного результата
     * @param onError Опциональный обработчик ошибки (по умолчанию вызывается handleError)
     * 
     * Пример:
     * ```kotlin
     * safeLaunchWithResult({ useCase() }) { data ->
     *     setState { copy(data = data) }
     * }
     * ```
     */
    protected fun <T> safeLaunchWithResult(
        block: suspend () -> Result<T>,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: (T) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = block()
                when (result) {
                    is Result.Success -> onSuccess(result.data)
                    is Result.Error -> (onError ?: ::handleError)(result.exception)
                    is Result.Loading -> Unit
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    /**
     * Обработка ошибок
     * 
     * Вызывается при возникновении исключений в safeLaunch/safeLaunchWithResult.
     * Переопределите для кастомной логики обработки ошибок.
     * 
     * @param error Исключение
     */
    protected open fun handleError(error: Throwable) {
        ensureInitialized()
        setState { 
            // Предполагается что у состояния есть поле error
            // Дочерние классы должны это реализовать
            this 
        }
        // Логируем ошибку
        println("ViewModel Error: ${error.message ?: "Unknown error"}")
        error.printStackTrace()
    }
    
    /**
     * Текущее состояние
     * Доступно только внутри ViewModel
     */
    protected val currentState: T
        get() {
            ensureInitialized()
            return _state.value
        }
}
