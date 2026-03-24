package com.neuroproject.neuro

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gelo.capsule.CapsuleNative
import com.gyf.immersionbar.ktx.immersionBar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Главная активность приложения Neuro Project
 *
 * Является точкой входа в приложение. Настроена на использование Compose UI
 * и Hilt для внедрения зависимостей.
 *
 * ## Основные функции:
 * - Настройка системного UI (edge-to-edge)
 * - Предотвращение выключения экрана во время работы
 * - Инициализация нативной библиотеки Capsule
 * - Запрос разрешений для работы с устройством
 * - Установка Compose контента
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Вызывается при создании активности.
     *
     * Выполняет:
     * 1. Включение edge-to-edge режима
     * 2. Установку флага для предотвращения выключения экрана
     * 3. Инициализацию нативной библиотеки Capsule
     * 4. Запрос необходимых разрешений
     * 5. Установку Compose UI
     *
     * @param savedInstanceState Сохраненное состояние (не используется)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включает edge-to-edge режим (контент под системные панели)
        enableEdgeToEdge()

        // Предотвращает выключение экрана во время работы с устройством
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Инициализация нативной библиотеки Capsule для работы с нейро-гарнитурой
        CapsuleNative.initCapsule()

        // Запрос разрешений на Bluetooth и местоположение
        CapsuleNative.requestPermissions(this)

        // Установка Compose контента
        setContent {
            NeuroApplication()
        }
    }
}