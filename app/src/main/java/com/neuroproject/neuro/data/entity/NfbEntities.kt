package com.neuroproject.neuro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "nfb_metrics", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class NFBMetricEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)

@Entity(tableName = "nfb_metrics_compressed", indices = [Index(value = ["sessionId", "timestamp"]), Index(value = ["timestamp"])])
data class NFBMetricCompressedEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val timestamp: Long,
    val id: String,
    val expedition_id: String,
    val sessionId: Long,
    val alpha: Float,
    val beta: Float,
    val theta: Float,
    val delta: Float,
    val smr: Float,
    val isMarked: Boolean
)
