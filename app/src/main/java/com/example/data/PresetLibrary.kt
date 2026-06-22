package com.example.data

object PresetLibrary {
    data class PresetDevice(
        val name: String,
        val type: String, // "LAMP", "AC", "TV"
        val brand: String,
        val buttons: List<Button>
    )

    val presets = listOf(
        // Chinese RGB Lamp (very popular 24-key remote)
        PresetDevice(
            name = "LED RGB Лампа (Китай)",
            type = "LAMP",
            brand = "Generic RGB Bulb",
            buttons = listOf(
                Button(name = "Вкл", irPattern = listOf(9000, 4500, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690), carrierFrequency = 38000),
                Button(name = "Выкл", irPattern = listOf(9000, 4500, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Яркость +", irPattern = listOf(9000, 4500, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Яркость -", irPattern = listOf(9000, 4500, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690), carrierFrequency = 38000),
                Button(name = "Красный (R)", irPattern = listOf(9000, 4500, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690), carrierFrequency = 38000),
                Button(name = "Зеленый (G)", irPattern = listOf(9000, 4500, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Синий (B)", irPattern = listOf(9000, 4500, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Белый (W)", irPattern = listOf(9000, 4500, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690), carrierFrequency = 38000),
                Button(name = "Режим: Flash", irPattern = listOf(9000, 4500, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690), carrierFrequency = 38000),
                Button(name = "Режим: Fade", irPattern = listOf(9000, 4500, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690), carrierFrequency = 38000)
            )
        ),
        // Universal TV
        PresetDevice(
            name = "Телевизор (LG/Samsung)",
            type = "TV",
            brand = "Samsung/LG Series",
            buttons = listOf(
                Button(name = "Вкл/Выкл (Power)", irPattern = listOf(4500, 4500, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Громкость +", irPattern = listOf(4500, 4500, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690), carrierFrequency = 38000),
                Button(name = "Громкость -", irPattern = listOf(4500, 4500, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690), carrierFrequency = 38000),
                Button(name = "Канал +", irPattern = listOf(4500, 4500, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690, 560, 560, 560, 1690), carrierFrequency = 38000),
                Button(name = "Канал -", irPattern = listOf(4500, 4500, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Mute", irPattern = listOf(4500, 4500, 560, 1690, 560, 560, 560, 560, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 560), carrierFrequency = 38000),
                Button(name = "Сурс (Input)", irPattern = listOf(4500, 4500, 560, 560, 560, 1690, 560, 1690, 560, 1690, 560, 1690, 560, 560, 560, 1690, 560, 1690)  )
            )
        ),
        // AC Midea/Gree (very popular Chinese split system)
        PresetDevice(
            name = "Кондиционер (Midea/Gree)",
            type = "AC",
            brand = "Midea/Gree/Haier",
            buttons = listOf(
                Button(name = "Вкл/Выкл", irPattern = listOf(3000, 3000, 500, 1500, 500, 500, 500, 1500, 500, 500, 500, 1500, 500, 1500, 500, 500, 500, 500), carrierFrequency = 38000),
                Button(name = "Температура +", irPattern = listOf(3000, 3000, 500, 1500, 500, 1500, 500, 500, 500, 500, 500, 1500, 500, 550, 500, 1500), carrierFrequency = 38000),
                Button(name = "Температура -", irPattern = listOf(3000, 3000, 500, 500, 500, 1500, 500, 1500, 500, 500, 500, 500, 500, 1500, 500, 1500), carrierFrequency = 38000),
                Button(name = "Режим: Охлаждение", irPattern = listOf(3000, 3000, 500, 1500, 500, 500, 500, 1500, 500, 1500, 500, 500, 500, 1500, 500, 1500), carrierFrequency = 38000),
                Button(name = "Режим: Обогрев", irPattern = listOf(3000, 3000, 500, 500, 500, 1500, 500, 500, 500, 1500, 500, 1500, 500, 1500, 500, 500), carrierFrequency = 38000),
                Button(name = "Вентилятор: Авто", irPattern = listOf(3000, 3000, 500, 500, 500, 500, 500, 1500, 500, 1500, 500, 1500, 500, 500, 500, 1500)  )
            )
        )
    )

    fun getPopularButtonsForType(type: String): List<String> {
        return when (type) {
            "LAMP" -> listOf("Вкл/Выкл", "Яркость +", "Яркость -", "Режим: Flash", "Режим: Fade", "Красный (R)", "Зеленый (G)", "Синий (B)", "Белый (W)")
            "AC" -> listOf("Вкл/Выкл", "Температура +", "Температура -", "Режим: Охлаждение", "Режим: Обогрев", "Вентилятор: Авто", "Турбо", "Свинг")
            "TV" -> listOf("Вкл/Выкл (Power)", "Громкость +", "Громкость -", "Канал +", "Канал -", "Mute", "Меню", "Input (Сурс)", "Назад (Back)")
            else -> listOf("Вкл/Выкл (Power)", "ОК", "Режим", "Опции")
        }
    }
}
