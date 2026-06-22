package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Button
import com.example.data.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: IrViewModel,
    deviceId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLearn: (buttonId: String) -> Unit,
    onNavigateToAddButton: (deviceId: String) -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val device = devices.firstOrNull { it.id == deviceId }

    val isTransmitting by viewModel.isTransmitting.collectAsState()
    val lastButton by viewModel.lastTransmittedButton.collectAsState()

    var showAddButtonDialog by remember { mutableStateOf(false) }
    var newButtonName by remember { mutableStateOf("") }
    
    // Track last sent signal text for mock log
    var lastSentLog by remember { mutableStateOf("Сигналы еще не передавались") }

    if (device == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Устройство не найдено", color = Color.White)
        }
        return
    }

    // Isolate power button if present to place on top
    val powerButton = device.buttons.firstOrNull { 
        val name = it.name.lowercase()
        name.contains("power") || name.contains("питание") || name.contains("вкл") || name.contains("выкл")
    }
    // Filter remaining buttons for Bento Grid content
    val remainingButtons = device.buttons.filter { it.id != powerButton?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = device.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        val subTypeLabel = when (device.type) {
                            "LAMP" -> "Xiaomi IR Blaster • Лампа"
                            "AC" -> "Xiaomi IR Blaster • Климат"
                            "TV" -> "Xiaomi IR Blaster • Телевизор"
                            else -> "Xiaomi IR Blaster • Активен"
                        }
                        Text(
                            text = subTypeLabel,
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
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
                actions = {
                    IconButton(
                        onClick = { onNavigateToAddButton(device.id) },
                        modifier = Modifier.testTag("add_custom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить кнопку",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                )
            )
        },
        containerColor = Color(0xFF1C1B1F) // Theme dark background match
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Live transmission pulse animator log
            AnimatedVisibility(
                visible = isTransmitting,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                lastButton?.let { button ->
                    button.irPattern?.let { pattern ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ПЕРЕДАЧА ИК: ${button.name.uppercase()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "${button.carrierFrequency} Гц",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFD0BCFF)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                IrPatternWaveform(pattern = pattern)
                            }
                        }
                    }
                }
            }

            // Power control card at top (As specified in theme specs)
            if (powerButton != null) {
                val isTrained = powerButton.irPattern != null
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = powerButton.name,
                                color = Color(0xFFD0BCFF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTrained) lastSentLog else "Кнопка питания не обучена",
                                color = Color(0xFF938F99),
                                fontSize = 12.sp
                            )
                        }

                        // Big circular interactive red power key
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isTrained) Color(0xFFEF4444) else Color(0xFF49454F))
                                .clickable {
                                    if (isTrained) {
                                        viewModel.transmitIr(powerButton)
                                        lastSentLog = "Сигнал отправлен только что"
                                    } else {
                                        onNavigateToLearn(powerButton.id)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Питание",
                                tint = if (isTrained) Color.White else Color(0xFF938F99),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Buttons Grid (as specified: Bento Grid)
            if (remainingButtons.isEmpty() && powerButton != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Дополнительные кнопки пульта будут отображаться здесь",
                        color = Color(0xFF938F99),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (device.buttons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = "No Buttons",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Пульт пуст",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Добавьте кнопки (например, Громкость, Режимы) с помощью кнопки '+' сверху",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF938F99),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(remainingButtons, key = { it.id }) { button ->
                        BentoButtonCard(
                            button = button,
                            onClick = {
                                if (button.irPattern != null) {
                                    viewModel.transmitIr(button)
                                    lastSentLog = "Отправлено: ${button.name}"
                                } else {
                                    onNavigateToLearn(button.id)
                                }
                            },
                            onLearn = { onNavigateToLearn(button.id) },
                            onDelete = { viewModel.deleteButton(device.id, button.id) }
                        )
                    }
                }
            }

            // Main learning action trigger at the bottom
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEADDFF))
                    .clickable { onNavigateToAddButton(device.id) }
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF21005D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Learn camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Копировать новую кнопку",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Направьте ИК оригинального пульта на камеру",
                            color = Color(0xFF21005D).copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Перейти",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Add Button dialog
        if (showAddButtonDialog) {
            Dialog(onDismissRequest = { showAddButtonDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Добавление кнопки",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = newButtonName,
                            onValueChange = { newButtonName = it },
                            label = { Text("Имя кнопки (например, Громкость +)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                              ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_name_field")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showAddButtonDialog = false
                                newButtonName = ""
                            }) {
                                Text("Отмена", color = Color(0xFFD0BCFF))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newButtonName.isNotBlank()) {
                                        viewModel.addButton(device.id, newButtonName.trim())
                                        showAddButtonDialog = false
                                        newButtonName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                                modifier = Modifier.testTag("save_button_btn")
                            ) {
                                Text("Создать", color = Color(0xFF381E72))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoButtonCard(
    button: Button,
    onClick: () -> Unit,
    onLearn: () -> Unit,
    onDelete: () -> Unit
) {
    val isTrained = button.irPattern != null
    val nameLower = button.valueName().lowercase()

    val symbol = when {
        nameLower.contains("ярче") || nameLower.contains("яркость +") -> "☀"
        nameLower.contains("темнее") || nameLower.contains("яркость -") -> "☁"
        nameLower.contains("красн") -> "🔴"
        nameLower.contains("зелен") -> "🟢"
        nameLower.contains("син") -> "🔵"
        nameLower.contains("бел") -> "⚪"
        nameLower.contains("таймер") or nameLower.contains("timer") -> "⏰"
        nameLower.contains("громк") -> "🔊"
        nameLower.contains("канал") -> "📺"
        nameLower.contains("вкл") or nameLower.contains("выкл") -> "🔌"
        else -> "📡"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isTrained) Color(0xFF36343B) else Color.Transparent)
            .border(
                width = if (isTrained) 0.dp else 2.dp,
                color = if (isTrained) Color.Transparent else Color(0xFF49454F),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .testTag("remote_button_${button.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header small handler with delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // mini green dot if trained
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isTrained) Color(0xFF10B981) else Color(0xFF938F99))
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить кнопку",
                        tint = Color(0xFF938F99),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Big visual emoji/symbol representing key context
            Text(
                text = symbol,
                fontSize = 24.sp,
                color = if (isTrained) Color.White else Color(0xFF938F99)
            )

            // Button label
            Text(
                text = button.name,
                fontWeight = FontWeight.Bold,
                color = if (isTrained) Color.White else Color(0xFF938F99),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Status tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isTrained) Color(0xFF381E72) else Color(0xFF2B2930))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isTrained) "ГОТОВО" else "ОБУЧИТЬ",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTrained) Color(0xFFD0BCFF) else Color(0xFF938F99)
                )
            }
        }
    }
}

// Extension to avoid empty name compile mismatches
fun Button.valueName(): String = this.name
