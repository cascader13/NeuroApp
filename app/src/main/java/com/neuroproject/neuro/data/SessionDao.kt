package com.neuroproject.neuro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: Long): SessionEntity?

    @Query("""
        UPDATE sessions SET 
        objectiveCognitive = :objCog,
        objectiveEmotional = :objEmo,
        objectivePhysical = :objPhys,
        subjectiveCognitive = :subCog,
        subjectiveEmotional = :subEmo,
        subjectivePhysical = :subPhys,
        totalIndex = :total
        WHERE sessionId = :sessionId
    """)
    suspend fun updateIndexes(
        sessionId: Long,
        objCog: Int?,
        objEmo: Int?,
        objPhys: Int?,
        subCog: Int?,
        subEmo: Int?,
        subPhys: Int?,
        total: Int?
    )

    @Query("""
        UPDATE sessions SET 
        comment = :comment
        WHERE sessionId = :sessionId
    """)
    suspend fun updateComment(
        sessionId: Long,
        comment: String?
    )

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}