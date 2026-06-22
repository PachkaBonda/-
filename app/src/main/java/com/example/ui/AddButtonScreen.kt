package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Button
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddButtonScreen(
    viewModel: IrViewModel,
    deviceId: String,
    onNavigateBack: () -> Unit,
    onStartRecording: (buttonId: String) -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val device = devices.firstOrNull { it.id == deviceId }

    var buttonName by remember { mutableStateOf("") }
    var carrierFrequency by remember { mutableStateOf(38000) }

    if (device == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F)),
            contentAlignment = Alignment.Center
        ) {
            Text("Устройство не найдено", color = Color.White)
        }
        return
    }

    // Get suggestions based on Device type
    val suggestionButtons = when (device.type) {
        "LAMP" -> listOf("Вкл/Выкл", "Яркость +", "Яркость -", "Режим: Flash", "Режим: Fade", "Красный (R)", "Синий (B)", "Белый (W)")
        "AC" -> listOf("Вкл/Выкл", "Температура +", "Температура -", "Режим: Охлаждение", "Режим: Обогрев", "Турбо", "Авто свинг")
        "TV" -> listOf("Вкл/Выкл (Power)", "Громкость +", "Громкость -", "Канал +", "Канал -", "Mute", "Input (Сурс)", "Назад")
        else -> listOf("Вкл/Выкл", "ОК", "Режим", "Громкость +", "Громкость -", "Вверх", "Вниз")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Добавить кнопку",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "для ${device.name}",
                            fontSize = 12.sp,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                )
            )
        },
        containerColor = Color(0xFF1C1B1F)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Description Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "ИК датчик",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Шаг 1: Назовите команду",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Вы можете выбрать готовый шаблон кнопок или ввести любое ручное имя.",
                            fontSize = 12.sp,
                            color = Color(0xFF938F99)
                        )
                    }
                }
            }

            // Input field
            OutlinedTextField(
                value = buttonName,
                onValueChange = { buttonName = it },
                label = { Text("Название кнопки (например: Яркость +)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color(0xFF49454F),
                    focusedLabelColor = Color(0xFFD0BCFF),
                    unfocusedLabelColor = Color(0xFF938F99)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_button_name_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestions Title
            Text(
                text = "Быстрые шаблоны:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFD0BCFF),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Horizontal suggestions row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionButtons) { suggestion ->
                    val isSelected = buttonName == suggestion
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFD0BCFF) else Color(0xFF36343B))
                            .clickable { buttonName = suggestion }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = suggestion,
                            color = if (isSelected) Color(0xFF381E72) else Color(0xFFE6E1E5),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frequency configuration section
            Text(
                text = "Несущая частота волны:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFD0BCFF),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val frequencies = listOf(38000 to "38 кГц (Стандарт)", 40000 to "40 кГц", 56000 to "56 кГц")
                frequencies.forEach { (freq, label) ->
                    val isSelected = carrierFrequency == freq
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF49454F) else Color(0xFF2B2930))
                            .clickable { carrierFrequency = freq }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF938F99),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B2930))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Инфо",
                    tint = Color(0xFF938F99),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "95% пультов используют стандартную частоту 38 кГц.",
                    fontSize = 11.sp,
                    color = Color(0xFF938F99)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Beautiful Call-to-Action gradient Button to start IR signal learning
            val isButtonNameValid = buttonName.trim().isNotEmpty()

            val mainButtonGradient = if (isButtonNameValid) {
                Brush.horizontalGradient(listOf(Color(0xFFD0BCFF), Color(0xFFB09CFF)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF36343B), Color(0xFF36343B)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(mainButtonGradient)
                    .clickable(enabled = isButtonNameValid) {
                        val newButtonId = UUID.randomUUID().toString()
                        val newButton = Button(
                            id = newButtonId,
                            name = buttonName.trim(),
                            irPattern = null,
                            carrierFrequency = carrierFrequency
                        )
                        viewModel.addButtonDirect(device.id, newButton)
                        onStartRecording(newButtonId)
                    }
                    .testTag("start_recording_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Обучить",
                        tint = if (isButtonNameValid) Color(0xFF21005D) else Color(0xFF938F99),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Обучить кнопку ИК",
                        color = if (isButtonNameValid) Color(0xFF21005D) else Color(0xFF938F99),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
