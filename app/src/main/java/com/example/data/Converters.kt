package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val buttonListType = Types.newParameterizedType(List::class.java, Button::class.java)
    private val adapter = moshi.adapter<List<Button>>(buttonListType)

    @TypeConverter
    fun fromButtonList(value: List<Button>?): String? {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toButtonList(value: String?): List<Button>? {
        return if (value != null) {
            try {
                adapter.fromJson(value)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
