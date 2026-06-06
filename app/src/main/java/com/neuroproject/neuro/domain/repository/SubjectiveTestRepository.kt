package com.neuroproject.neuro.domain.repository

import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import kotlinx.coroutines.flow.Flow

interface SubjectiveTestRepository {

    suspend fun importQuestions()
    suspend fun getQuestions(): List<SubjectiveQuestion>
    suspend fun saveAnswers(sessionId: Long, answers: List<SubjectiveAnswer>)
    suspend fun getAnswers(sessionId: Long): List<SubjectiveAnswer>
    suspend fun saveComment(sessionId: Long, comment: String)

    suspend fun getLastAnswerScoreById(id: Int, userId: String): Int?
}