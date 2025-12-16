// [file name]: Converters.kt
package com.neuroproject.neuro.data

import androidx.room.TypeConverter
import java.sql.Timestamp
import java.util.Date

class Converters {

    // 1. Функции для java.util.Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // 2. Функции для java.sql.Timestamp с РАЗНЫМИ именами
    @TypeConverter
    fun fromLongToSqlTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    @TypeConverter
    fun sqlTimestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.time
    }
}