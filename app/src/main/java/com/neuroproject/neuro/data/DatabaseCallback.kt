package com.neuroproject.neuro.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Callback для инициализации базы данных при первом создании
 *
 * Выполняет предварительное заполнение базы данных вопросами субъективного тестирования
 * при первом запуске приложения. Это обеспечивает наличие всех вопросов в БД
 * без необходимости вручную их добавлять.
 *
 * ## Жизненный цикл:
 * 1. Room создает базу данных
 * 2. Вызывается метод [onCreate]
 * 3. Проверяется наличие INSTANCE
 * 4. В фоновом потоке заполняются вопросы
 *
 * ## Важно:
 * Callback выполняется только один раз при первом создании БД.
 * При последующих запусках (при обновлении схемы) используется [onOpen].
 *
 * @param context Контекст приложения (не используется напрямую, но доступен)
 *
 * @see SubjectiveQuestionsProvider
 * @see MetricsDatabase
 */
class DatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    /**
     * Вызывается при первом создании базы данных
     *
     * Заполняет таблицу subjective_questions предопределенными вопросами.
     *
     * @param db Объект SQLite базы данных (не используется, доступ к БД через INSTANCE)
     */
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let { database ->
            // Используем IO диспетчер для фоновой загрузки
            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase(database)
            }
        }
    }

    /**
     * Заполнение базы данных вопросами
     *
     * @param database Экземпляр MetricsDatabase
     */
    private suspend fun populateDatabase(database: MetricsDatabase) {
        val questionDao = database.subjectiveQuestionDao()
        questionDao.insertAll(
            SubjectiveQuestionsProvider.getQuestions()
        )
    }

    companion object {
        /**
         * Волатильная ссылка на экземпляр базы данных
         *
         * Устанавливается в MetricsDatabase.getInstance() для доступа
         * из callback при инициализации.
         */
        @Volatile
        var INSTANCE: MetricsDatabase? = null
    }
}