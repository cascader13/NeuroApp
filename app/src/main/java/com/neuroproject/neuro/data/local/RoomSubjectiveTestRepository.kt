
package com.neuroproject.neuro.data.local

import com.neuroproject.neuro.data.subtest.SubjectiveAnswerDao
import com.neuroproject.neuro.data.subtest.SubjectiveAnswerEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionDao
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionEntity
import com.neuroproject.neuro.data.subtest.SubjectiveQuestionsProvider
import com.neuroproject.neuro.domain.model.BlockType
import com.neuroproject.neuro.domain.model.SubjectiveAnswer
import com.neuroproject.neuro.domain.model.SubjectiveQuestion
import com.neuroproject.neuro.domain.repository.SubjectiveTestRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSubjectiveTestRepository @Inject constructor(
    private val questionDao: SubjectiveQuestionDao,
    private val answerDao: SubjectiveAnswerDao
) : SubjectiveTestRepository {
    override suspend fun importQuestions() {
        if (getQuestions().isEmpty()){
            questionDao.insertAll(SubjectiveQuestionsProvider.getQuestions())
        }
    }

    override suspend fun getQuestions(): List<SubjectiveQuestion> {
        return questionDao.getAll().map { it.toDomain() }
    }

    override suspend fun saveAnswers(sessionId: Long, answers: List<SubjectiveAnswer>) {
        answerDao.deleteBySession(sessionId)
        val entities = answers.map {
            SubjectiveAnswerEntity(
                sessionId = sessionId,
                questionId = it.questionId,
                value = it.value
            )
        }
        answerDao.insertAll(entities)
    }

    override suspend fun saveAnswer(sessionId: Long, answer: SubjectiveAnswer) {
        answerDao.deleteBySessionAndQuestion(sessionId, answer.questionId)
        answerDao.insert(
            SubjectiveAnswerEntity(
                sessionId = sessionId,
                questionId = answer.questionId,
                value = answer.value
            )
        )
    }

    override suspend fun getAnswers(sessionId: Long): List<SubjectiveAnswer> {
        return answerDao.getBySession(sessionId).map {
            SubjectiveAnswer(it.questionId, it.value)
        }
    }

    override suspend fun saveComment(sessionId: Long, comment: String) {
        // Обновление комментария в сессии
    }

    override suspend fun getLastAnswerScoreById(id: Int, userId: String): Int? {
        return answerDao.getLastById(id, userId)?.value
    }
}

fun SubjectiveQuestionEntity.toDomain(): SubjectiveQuestion = SubjectiveQuestion(
    id = id,
    text = text,
    blockType = when (blockType) {
        com.neuroproject.neuro.data.subtest.BlockType.COGNITIVE -> BlockType.COGNITIVE
        com.neuroproject.neuro.data.subtest.BlockType.EMOTIONAL -> BlockType.EMOTIONAL
        com.neuroproject.neuro.data.subtest.BlockType.PHYSICAL -> BlockType.PHYSICAL
    },
    displayOrder = displayOrder,
    isReversed = isReversed
)

fun SubjectiveQuestion.fromDomain(): SubjectiveQuestionEntity = SubjectiveQuestionEntity(
    id = id,
    text = text,
    blockType = when (blockType) {
        BlockType.PHYSICAL -> com.neuroproject.neuro.data.subtest.BlockType.PHYSICAL
        BlockType.EMOTIONAL -> com.neuroproject.neuro.data.subtest.BlockType.EMOTIONAL
        BlockType.COGNITIVE -> com.neuroproject.neuro.data.subtest.BlockType.COGNITIVE
    },
    displayOrder = displayOrder,
    isReversed = isReversed
)