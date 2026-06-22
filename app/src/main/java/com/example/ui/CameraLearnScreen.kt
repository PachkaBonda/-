package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.IrSignalAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraLearnScreen(
    viewModel: IrViewModel,
    deviceId: String,
    buttonId: String,
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Reset capture states on initial entry
    LaunchedEffect(Unit) {
        viewModel.clearCaptureState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обучение через камеру", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0D17))
            )
        },
        containerColor = Color(0xFF07080E)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraLearnContent(
                    viewModel = viewModel,
                    deviceId = deviceId,
                    buttonId = buttonId,
                    onNavigateBack = onNavigateBack
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Camera Permission Guide",
                        tint = Color.LightGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Требуется доступ к камере",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Для захвата и калибровки ИК-сигнала оригинального пульта приложению необходим доступ к камере.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("request_permission_btn")
                    ) {
                        Text("Разрешить камеру", color = Color.White)
                    }
                }
            }
        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun CameraLearnContent(
    viewModel: IrViewModel,
    deviceId: String,
    buttonId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val capturedPattern by viewModel.capturedPattern.collectAsState()
    val captureLogs by viewModel.captureLogs.collectAsState()

    var flashEnabled by remember { mutableStateOf(false) }

    // Multi-threaded executor for camera analyser frames
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Real-time analyzers state tracking
    val analyzer = remember {
        IrSignalAnalyzer(
            onLuminanceSpike = { delta -> viewModel.registerLuminanceSpike(delta) },
            onSignalDetected = { pattern -> viewModel.registerCapturedSignal(pattern) }
        )
    }

    // Bind analyzer status flag with local training scope
    LaunchedEffect(capturedPattern) {
        analyzer.setAnalysisActive(capturedPattern == null)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (capturedPattern == null) {
            // VIEWPORT PREVIEW ACTIVE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color.Black)
            ) {
                // Viewfinder feed binding CameraX
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, analyzer)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                // Simple camera controls can be set if needed
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                // High-tech sci-fi viewfinder overlay
                ScannerOverlay(modifier = Modifier.fillMaxSize())

                // Onboarding tip
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Направьте ИК-диод пульта в центр и нажмите кнопку",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // DIAGNOSTICS PERFORMANCE MONITOR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(Color(0xFF07080E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Лог анализатора частоты",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4E62EC))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "СКАНИРОВАНИЕ...",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4E62EC),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (captureLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ожидание вспышки ИК-сигнала...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0E16))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(captureLogs) { log ->
                                Text(
                                    text = "> $log",
                                    color = if (log.contains("уловлен")) Color(0xFF10B981) else Color(0xFFFFB020),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // PATTERN CAPTURED CONTENT (SUCCESS SCREEN)
            val patternList = capturedPattern!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(84.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Сигнал успешно записан!",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Анализатор зафиксировал вспышку света и декодировал raw импульсы.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131522))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Детали ИК-кода (NEC RAW):",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Несущая частота:", color = Color.Gray, fontSize = 12.sp)
                                Text("38000 Гц", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Количество импульсов:", color = Color.Gray, fontSize = 12.sp)
                                Text("${patternList.size} raw", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Форма уловленного сигнала:",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            IrPatternWaveform(pattern = patternList)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearCaptureState() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        borderColor = Color(0xFF374151),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Cached, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Перезаписать")
                    }

                    Button(
                        onClick = {
                            viewModel.updateButtonPattern(deviceId, buttonId, patternList, 38000)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("save_signal_btn")
                    ) {
                        Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    colors: ButtonColors,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(100.dp)),
        content = content
    )
}

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val cropWidth = width * 0.5f
        val cropHeight = height * 0.5f

        val left = (width - cropWidth) / 2f
        val top = (height - cropHeight) / 2f
        val right = left + cropWidth
        val bottom = top + cropHeight

        // Draw transparent targeted rect corner markers
        val strokeWidth = 3.dp.toPx()
        val cornerLen = 24.dp.toPx()

        val markerColor = Color(0xFFEF4444)

        // Top-Left Corner
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top + cornerLen)
                lineTo(left, top)
                lineTo(left + cornerLen, top)
            },
            color = markerColor,
            style = Stroke(width = strokeWidth)
        )

        // Top-Right Corner
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(right - cornerLen, top)
                lineTo(right, top)
                lineTo(right, top + cornerLen)
            },
            color = markerColor,
            style = Stroke(width = strokeWidth)
        )

        // Bottom-Left Corner
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, bottom - cornerLen)
                lineTo(left, bottom)
                lineTo(left + cornerLen, bottom)
            },
            color = markerColor,
            style = Stroke(width = strokeWidth)
        )

        // Bottom-Right Corner
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(right - cornerLen, bottom)
                lineTo(right, bottom)
                lineTo(right, bottom - cornerLen)
            },
            color = markerColor,
            style = Stroke(width = strokeWidth)
        )

        // Draw targets crosshair
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(width / 2f - 12.dp.toPx(), height / 2f),
            end = Offset(width / 2f + 12.dp.toPx(), height / 2f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(width / 2f, height / 2f - 12.dp.toPx()),
            end = Offset(width / 2f, height / 2f + 12.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}
