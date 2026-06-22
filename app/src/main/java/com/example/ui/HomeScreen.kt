package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Button
import com.example.data.Device
import com.example.data.PresetLibrary

enum class CreateStep {
    SELECT_TYPE,
    CONFIGURE_METHOD,
    CHOOSE_PRESET,
    MANUAL_SETUP_BUTTONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: IrViewModel,
    onNavigateToDevice: (String) -> Unit
) {
    val devices by viewModel.devices.collectAsState()

    // Filter Type tab: "ALL", "LAMP", "AC", "TV", "OTHER"
    var selectedTab by remember { mutableStateOf("ALL") }

    // Multi-step Creation Dialog logic
    var isCreationOpen by remember { mutableStateOf(false) }
    var creationStep by remember { mutableStateOf(CreateStep.SELECT_TYPE) }

    var selectedType by remember { mutableStateOf("LAMP") }
    var customTypeName by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("PRESET") } // "PRESET" or "MANUAL"

    // Temporary lists of manual buttons
    var manualButtonsList = remember { mutableStateListOf<String>() }
    var customManualButtonInput by remember { mutableStateOf("") }

    val statusColor = if (viewModel.hasHardwareIr) Color(0xFF10B981) else Color(0xFFF59E0B)
    val statusText = if (viewModel.hasHardwareIr) "ИК-бластер готов (Аппаратный)" else "Режим эмуляции (ИК-бластер не найден)"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SettingsRemote,
                            contentDescription = "ИК Пульт",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "ИК Копир & Контроль",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2B2930))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Reset add states
                    selectedType = "LAMP"
                    customTypeName = ""
                    deviceName = "Моя Лампа"
                    selectedMethod = "PRESET"
                    manualButtonsList.clear()
                    creationStep = CreateStep.SELECT_TYPE
                    isCreationOpen = true
                },
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("add_device_fab")
                    .padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить устройство", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = Color(0xFF1C1B1F) // Theme dark background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Hardware transmitter status badge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .drawBehind {
                                drawCircle(color = statusColor)
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = statusText,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Диапазон сигнала: ${viewModel.supportedFrequencies}",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Tabs / Horizontal scroll filters
            Text(
                text = "Устройства",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    "ALL" to "Все пульты",
                    "LAMP" to "Лампы 💡",
                    "TV" to "Телевизоры 📺",
                    "AC" to "Климат ❄️",
                    "OTHER" to "Другое ⚙️"
                )

                items(tabs) { (code, label) ->
                    val isSelected = selectedTab == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFD0BCFF) else Color(0xFF36343B))
                            .clickable { selectedTab = code }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF381E72) else Color(0xFFE6E1E5),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Filter hardware list
            val filteredDevices = if (selectedTab == "ALL") {
                devices
            } else {
                devices.filter { it.type == selectedTab }
            }

            if (filteredDevices.isEmpty()) {
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
                            imageVector = Icons.Outlined.DeviceHub,
                            contentDescription = "Empty devices",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Нет добавленных пультов в этой категории",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E5),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Нажмите '+' в правом нижнем углу, чтобы скопировать или обучить новый пульт!",
                            textAlign = TextAlign.Center,
                            color = Color(0xFF938F99),
                            fontSize = 13.sp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredDevices, key = { it.id }) { device ->
                        DeviceCardItem(
                            device = device,
                            onClick = { onNavigateToDevice(device.id) },
                            onDelete = { viewModel.deleteDevice(device.id) }
                        )
                    }
                }
            }
        }

        // Gorgeous Multistep Add Device Dialog
        if (isCreationOpen) {
            Dialog(onDismissRequest = { isCreationOpen = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Title header
                        val stepTitle = when (creationStep) {
                            CreateStep.SELECT_TYPE -> "Выбор типа устройства"
                            CreateStep.CONFIGURE_METHOD -> "Добавить устройство"
                            CreateStep.CHOOSE_PRESET -> "Выбрать из базы пультов"
                            CreateStep.MANUAL_SETUP_BUTTONS -> "Набор кнопок"
                        }
                        Text(
                            text = stepTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFE6E1E5),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        when (creationStep) {
                            CreateStep.SELECT_TYPE -> {
                                Text(
                                    text = "Выберите категорию пульта ИК для более удобного Bento-интерфейса:",
                                    fontSize = 13.sp,
                                    color = Color(0xFF938F99),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                val types = listOf(
                                    Triple("LAMP", "Лампа (RGB, LED, Настольная)", Icons.Default.Lightbulb),
                                    Triple("AC", "Кондиционер (Сплит-система)", Icons.Default.AcUnit),
                                    Triple("TV", "Телевизор (Smart TV / Monitor)", Icons.Default.Tv),
                                    Triple("OTHER", "Другое устройство", Icons.Default.SettingsRemote)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    types.forEach { (code, label, icon) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selectedType == code) Color(0xFF49454F) else Color(0xFF36343B))
                                                .clickable {
                                                    selectedType = code
                                                    deviceName = when (code) {
                                                        "LAMP" -> "Моя Лампа RGB"
                                                        "AC" -> "Кондиционер"
                                                        "TV" -> "Smart Телевизор"
                                                        else -> "ИК Устройство"
                                                    }
                                                }
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (selectedType == code) Color(0xFFD0BCFF) else Color(0xFF2B2930)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (selectedType == code) Color(0xFF381E72) else Color(0xFFD0BCFF),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Text(
                                                text = label,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                if (selectedType == "OTHER") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = customTypeName,
                                        onValueChange = { customTypeName = it },
                                        label = { Text("Свой тип (например, Проектор)") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFD0BCFF),
                                            unfocusedBorderColor = Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { isCreationOpen = false }) {
                                        Text("Отмена", color = Color(0xFFD0BCFF))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { creationStep = CreateStep.CONFIGURE_METHOD },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                                    ) {
                                        Text("Далее", color = Color(0xFF381E72))
                                    }
                                }
                            }

                            CreateStep.CONFIGURE_METHOD -> {
                                OutlinedTextField(
                                    value = deviceName,
                                    onValueChange = { deviceName = it },
                                    label = { Text("Название пульта") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFD0BCFF),
                                        unfocusedBorderColor = Color(0xFF49454F)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Выберите метод добавления кнопок:",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE6E1E5),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Clickable Row for Presets
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selectedMethod == "PRESET") Color(0xFF49454F) else Color(0xFF36343B))
                                        .clickable { selectedMethod = "PRESET" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMethod == "PRESET",
                                        onClick = { selectedMethod = "PRESET" },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD0BCFF))
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text("Выбрать из базы", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Загрузить готовые ИК-коды популярных брендов", color = Color(0xFF938F99), fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Clickable Row for Manual
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selectedMethod == "MANUAL") Color(0xFF49454F) else Color(0xFF36343B))
                                        .clickable { selectedMethod = "MANUAL" }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMethod == "MANUAL",
                                        onClick = { selectedMethod = "MANUAL" },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD0BCFF))
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text("Обучить вручную", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Записать raw импульсы ИК c пульта через камеру", color = Color(0xFF938F99), fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { creationStep = CreateStep.SELECT_TYPE }) {
                                        Text("Назад", color = Color(0xFFD0BCFF))
                                    }
                                    Button(
                                        onClick = {
                                            if (selectedMethod == "PRESET") {
                                                creationStep = CreateStep.CHOOSE_PRESET
                                            } else {
                                                // Prepopulate proposed list
                                                manualButtonsList.clear()
                                                manualButtonsList.addAll(PresetLibrary.getPopularButtonsForType(selectedType))
                                                creationStep = CreateStep.MANUAL_SETUP_BUTTONS
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                                    ) {
                                        Text("Далее", color = Color(0xFF381E72))
                                    }
                                }
                            }

                            CreateStep.CHOOSE_PRESET -> {
                                Text(
                                    text = "Доступные ИК-профили в библиотеке:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF938F99),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                val relevantPresets = PresetLibrary.presets.filter { it.type == selectedType }

                                if (relevantPresets.isEmpty()) {
                                    Text(
                                        text = "К сожалению, готовых пресетов для этого типа нет. Попробуйте ручную запись!",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        relevantPresets.forEach { preset ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF36343B))
                                                    .clickable {
                                                        // Create device from preset immediately
                                                        viewModel.createDevice(
                                                            name = deviceName,
                                                            type = selectedType,
                                                            buttons = preset.buttons
                                                        )
                                                        isCreationOpen = false
                                                    }
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(preset.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Бренд: ${preset.brand} • Кнопок: ${preset.buttons.size}", color = Color(0xFF938F99), fontSize = 11.sp)
                                                }
                                                Icon(Icons.Default.Upload, contentDescription = "Выбрать", tint = Color(0xFFD0BCFF))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { creationStep = CreateStep.CONFIGURE_METHOD }) {
                                        Text("Назад", color = Color(0xFFD0BCFF))
                                    }
                                }
                            }

                            CreateStep.MANUAL_SETUP_BUTTONS -> {
                                Text(
                                    text = "Выберите шаблоны кнопок для обучения:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF938F99),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Display checked list of options
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .background(Color(0xFF1C1B1F), RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(manualButtonsList) { buttonName ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(buttonName, color = Color.White, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { manualButtonsList.remove(buttonName) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Убрать", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Add custom button input row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customManualButtonInput,
                                        onValueChange = { customManualButtonInput = it },
                                        placeholder = { Text("Имя кнопки, например: Усиление") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFD0BCFF),
                                            unfocusedBorderColor = Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (customManualButtonInput.isNotBlank()) {
                                                val name = customManualButtonInput.trim()
                                                if (!manualButtonsList.contains(name)) {
                                                    manualButtonsList.add(name)
                                                }
                                                customManualButtonInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) {
                                        Text("+", color = Color(0xFF21005D), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { creationStep = CreateStep.CONFIGURE_METHOD }) {
                                        Text("Назад", color = Color(0xFFD0BCFF))
                                    }
                                    Button(
                                        onClick = {
                                            // Convert manualButtonsList into Button objects with null patterns
                                            val generatedButtons = manualButtonsList.map { name ->
                                                Button(name = name, irPattern = null, carrierFrequency = 38000)
                                            }
                                            viewModel.createDevice(
                                                name = deviceName,
                                                type = selectedType,
                                                buttons = generatedButtons
                                            )
                                            isCreationOpen = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
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
    }
}

@Composable
fun DeviceCardItem(
    device: Device,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("device_card_${device.name}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2930) // Elegant dark slate card matching "Living room lamp" mockup text style
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle Remote Icon accent
                val typeGradient = when (device.type) {
                    "LAMP" -> Brush.linearGradient(listOf(Color(0xFFF1C40F), Color(0xFFD35400))) // Radiant orange/yellow lamp
                    "AC" -> Brush.linearGradient(listOf(Color(0xFF3498DB), Color(0xFF2C3E50))) // Chilly ice blue
                    "TV" -> Brush.linearGradient(listOf(Color(0xFF9B59B6), Color(0xFF8E44AD))) // Royal tv violet
                    else -> Brush.linearGradient(listOf(Color(0xFF4E62EC), Color(0xFF23318C))) // Classic primary iris blue
                }

                val typeIcon = when (device.type) {
                    "LAMP" -> Icons.Default.Lightbulb
                    "AC" -> Icons.Default.AcUnit
                    "TV" -> Icons.Default.Tv
                    else -> Icons.Default.SettingsRemote
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(typeGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = "Device Remote Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = device.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val buttonsCount = device.buttons.size
                    val trainedCount = device.buttons.count { it.irPattern != null }

                    Text(
                        text = "Обучено кнопок: $trainedCount из $buttonsCount",
                        color = Color(0xFF938F99),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_device_${device.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Удалить устройство",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
