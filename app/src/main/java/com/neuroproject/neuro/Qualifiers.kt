package com.neuroproject.neuro

import javax.inject.Qualifier

/**
 * Квалификатор Dagger Hilt для различения экземпляров зависимостей.
 *
 * В приложении используются два сервера:
 * - основной сервер без квалификатора — загрузка метрик;
 * - сервер мониторинга с [SecondServer] — отправка строк в реальном времени.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SecondServer

/** Application-wide CoroutineScope. Нужен для singleton-компонентов, которые живут дольше UI. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

/** IO dispatcher для Room/файлов/сети. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher
