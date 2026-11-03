package com.barati.zehnsaz.data.local

import androidx.room.TypeConverter
import com.barati.zehnsaz.domain.model.LevelConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLevelConfig(value: LevelConfig): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toLevelConfig(value: String): LevelConfig {
        return json.decodeFromString(value)
    }
}