package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String = "OTHER", // "LAMP", "AC", "TV", "OTHER"
    val buttons: List<Button> = emptyList()
)

data class Button(
    val id: String = UUID.randomUUID().toString(),
    val name: String,                    // например "Power", "Ярче", "Красный"
    val irPattern: List<Int>? = null,    // сырой паттерн в микросекундах (чередование ON/OFF)
    val carrierFrequency: Int = 38000    // обычно 38000
)
