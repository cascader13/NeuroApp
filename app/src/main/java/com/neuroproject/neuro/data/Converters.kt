package com.neuroproject.neuro.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.neuroproject.neuro.data.session.SessionCategory
import com.neuroproject.neuro.data.subtest.BlockType
import java.lang.reflect.Type
import java.sql.Timestamp
import java.util.Date

/**
 * Конвертеры типов для Room Database
 *
 * Обеспечивает преобразование сложных типов данных в типы, поддерживаемые SQLite.
 * Room не поддерживает напрямую такие типы, как Date, Timestamp, Enum и коллекции,
 * поэтому требуются конвертеры для их сохранения в базе данных.
 *
 * ## Поддерживаемые преобразования:
 * - [BlockType] (enum) → String
 * - [Date] → Long (timestamp)
 * - [Timestamp] → Long (timestamp)
 * - Map<Int, Int> → JSON String
 *
 * ## Использование:
 * Конвертеры автоматически применяются Room при аннотации класса базы данных:
 * ```kotlin
 * @Database(
 *     entities = [...],
 *     version = 1,
 *     exportSchema = false
 * )
 * @TypeConverters(Converters::class)
 * abstract class MetricsDatabase : RoomDatabase() { ... }
 * ```
 *
 */
class Converters {

    // ==================== ENUM CONVERTERS ====================

    /**
     * Преобразование [BlockType] в строку для хранения в БД
     *
     * @param value Enum значение для преобразования
     * @return Строковое представление enum (название константы)
     */
    @TypeConverter
    fun fromBlockType(value: BlockType): String {
        return value.name
    }

    /**
     * Преобразование строки из БД обратно в [BlockType]
     *
     * @param value Строковое представление enum
     * @return Соответствующий enum или исключение если значение не найдено
     * @throws IllegalArgumentException если строка не соответствует ни одному enum
     */
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

    // ==================== DATE CONVERTERS ====================

    /**
     * Преобразование Long timestamp в объект [Date]
     *
     * Используется для полей типа Date в сущностях Room.
     *
     * @param value Timestamp в миллисекундах или null
     * @return Объект Date или null если значение null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Преобразование [Date] в Long timestamp для хранения в БД
     *
     * @param date Объект Date или null
     * @return Timestamp в миллисекундах или null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ==================== SQL TIMESTAMP CONVERTERS ====================

    /**
     * Преобразование Long timestamp в [java.sql.Timestamp]
     *
     * Используется для работы с SQL-совместимыми временными метками.
     *
     * @param value Timestamp в миллисекундах или null
     * @return Объект java.sql.Timestamp или null
     */
    @TypeConverter
    fun fromLongToSqlTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    /**
     * Преобразование [java.sql.Timestamp] в Long для хранения в БД
     *
     * @param timestamp Объект java.sql.Timestamp или null
     * @return Timestamp в миллисекундах или null
     */
    @TypeConverter
    fun sqlTimestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.time
    }

    // ==================== MAP CONVERTERS ====================

    private val gson = Gson()

    /**
     * Преобразование JSON строки в Map<Int, Int>
     *
     * Используется для хранения ответов на субъективные вопросы в сжатом виде.
     *
     * @param value JSON строка с данными карты или пустая строка
     * @return Десериализованная Map или пустая Map если строка пуста
     */
    @TypeConverter
    fun fromStringToMap(value: String?): Map<Int, Int> {
        val type: Type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(value ?: "", type) ?: emptyMap()
    }

    /**
     * Преобразование Map<Int, Int> в JSON строку
     *
     * @param map Карта для сериализации или null
     * @return JSON строка или "{}" если map пуст
     */
    @TypeConverter
    fun fromMapToString(map: Map<Int, Int>?): String {
        return gson.toJson(map ?: mapOf<Int, Int>())
    }


}