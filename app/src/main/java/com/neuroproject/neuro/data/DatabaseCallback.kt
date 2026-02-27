package com.neuroproject.neuro.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        INSTANCE?.let { database ->
            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase(database)
            }
        }
    }

    private suspend fun populateDatabase(database: MetricsDatabase) {
        val questionDao = database.subjectiveQuestionDao()
        questionDao.insertAll(
            SubjectiveQuestionsProvider.getQuestions()
        )
    }

    companion object {
        @Volatile
        var INSTANCE: MetricsDatabase? = null
    }
}