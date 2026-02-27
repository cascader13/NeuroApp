// [file name]: Converters.kt
package com.neuroproject.neuro.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neuroproject.neuro.data.subtest.BlockType
import java.lang.reflect.Type
import java.sql.Timestamp
import java.util.Date

class Converters {

    @TypeConverter
    fun fromBlockType(value: BlockType): String {
        return value.name
    }

    @TypeConverter
    fun toBlockType(value: String): BlockType {
        return BlockType.valueOf(value)
    }

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

    private val gson = Gson()

    @TypeConverter
    fun fromStringToMap(value: String?): Map<Int, Int> {
        val type: Type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(value ?: "", type) ?: emptyMap()
    }

    @TypeConverter
    fun fromMapToString(map: Map<Int, Int>?): String {
        return gson.toJson(map ?: mapOf<Int, Int>())
    }
}