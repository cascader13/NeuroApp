package com.neuroproject.neuro.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neuroproject.neuro.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore для хранения настроек темы приложения
 *
 * Использует Preferences DataStore для сохранения выбранной темы
 * (светлая, темная или системная). DataStore обеспечивает асинхронную
 * работу с корутинами и потоковую передачу данных.
 *
 * ## Преимущества DataStore:
 * - Асинхронная работа с корутинами
 * - Типобезопасность
 * - Поддержка Flow для реактивного обновления UI
 *
 * ## Пример использования:
 * ```kotlin
 * class ThemeViewModel @Inject constructor(
 *     private val themePreferences: ThemePreferences
 * ) : ViewModel() {
 *
 *     val themeMode = themePreferences.themeMode
 *
 *     fun setTheme(mode: ThemeMode) {
 *         viewModelScope.launch {
 *             themePreferences.setThemeMode(mode)
 *         }
 *     }
 * }
 * ```
 *
 * @property context Контекст приложения (инжектится через Hilt)
 * @see ThemeMode
 */
private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        /** Ключ для хранения темы в DataStore */
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    /**
     * Поток текущей темы приложения
     *
     * Автоматически обновляется при изменении настроек.
     * При отсутствии сохраненного значения возвращает SYSTEM.
     *
     * @return Flow с текущим режимом темы
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val name = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(name)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    /**
     * Установка темы приложения
     *
     * Сохраняет выбранный режим темы в DataStore.
     *
     * @param mode Новый режим темы (LIGHT, DARK, SYSTEM)
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
}