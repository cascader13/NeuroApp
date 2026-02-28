package com.neuroproject.neuro.data.subtest

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectiveQuestionRepository @Inject constructor(
    private val dao: SubjectiveQuestionDao
) {
    suspend fun getAllQuestions(): List<SubjectiveQuestionEntity> {
        return dao.getAll()
    }
}