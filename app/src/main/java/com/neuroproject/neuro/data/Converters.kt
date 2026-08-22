package com.neuroproject.neuro.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neuroproject.neuro.data.session.SessionCategory
import com.neuroproject.neuro.data.subtest.BlockType
import java.lang.reflect.Type

/**
 * Конвертеры типов для Room, обеспечивающие преобразование сложных типов в примитивные.
 */
class Converters {

    @TypeConverter
    fun fromBlockType(value: BlockType): String {
        return value.name
    }

    @TypeConverter
    fun toBlockType(value: String): BlockType {
        return BlockType.valueOf(value)
    }

    @TypeConverter
    fun fromSessionCategory(category: SessionCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toSessionCategory(category: String?): SessionCategory? {
        return category?.let { SessionCategory.valueOf(it) }
    }

    private val gson = Gson()

    /** Преобразование JSON строки в Map<Int, Int> */
    @TypeConverter
    fun fromStringToMap(value: String?): Map<Int, Int> {
        val type: Type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(value ?: "", type) ?: emptyMap()
    }

    /** Преобразование Map<Int, Int> в JSON строку */
    @TypeConverter
    fun fromMapToString(map: Map<Int, Int>?): String {
        return gson.toJson(map ?: mapOf<Int, Int>())
    }
}