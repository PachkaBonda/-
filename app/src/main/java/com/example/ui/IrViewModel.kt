package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Button
import com.example.data.Device
import com.example.data.DeviceRepository
import com.example.data.IrTransmitter
import com.example.data.PresetLibrary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class IrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeviceRepository
    private val irTransmitter: IrTransmitter

    // Reactive flow of device list
    val devices: StateFlow<List<Device>>

    // Hardware IR emitter configuration states
    val hasHardwareIr: Boolean
    val supportedFrequencies: String

    // Dynamic state for selected device detail view
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice.asStateFlow()

    // Temporary storage for signals captured during camera scanning
    private val _capturedPattern = MutableStateFlow<List<Int>?>(null)
    val capturedPattern: StateFlow<List<Int>?> = _capturedPattern.asStateFlow()

    // Status message logs shown on UI charts
    private val _captureLogs = MutableStateFlow<List<String>>(emptyList())
    val captureLogs: StateFlow<List<String>> = _captureLogs.asStateFlow()

    // Dynamic UI states for active transmission indicators
    private val _isTransmitting = MutableStateFlow(false)
    val isTransmitting: StateFlow<Boolean> = _isTransmitting.asStateFlow()

    private val _lastTransmittedButton = MutableStateFlow<Button?>(null)
    val lastTransmittedButton: StateFlow<Button?> = _lastTransmittedButton.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DeviceRepository(database.deviceDao())

        devices = repository.allDevices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        irTransmitter = IrTransmitter(application)
        hasHardwareIr = irTransmitter.hasIrEmitter()
        supportedFrequencies = irTransmitter.getCarrierFrequenciesString()

        // Populate sample mock databases if empty for initial exploration friendliness
        viewModelScope.launch {
            devices.collect { list ->
                if (list.isEmpty()) {
                    createDefaultDevices()
                }
            }
        }
    }

    /**
     * Set active selected device.
     */
    fun selectDevice(device: Device?) {
        _selectedDevice.value = device
    }

    /**
     * Add a whole new physical controller device.
     */
    fun createDevice(name: String, type: String, buttons: List<Button> = emptyList()) {
        viewModelScope.launch {
            val newDevice = Device(
                name = name,
                type = type,
                buttons = buttons
            )
            repository.insertDevice(newDevice)
            _selectedDevice.value = newDevice
        }
    }

    /**
     * Delete a device.
     */
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDeviceById(deviceId)
            if (_selectedDevice.value?.id == deviceId) {
                _selectedDevice.value = null
            }
        }
    }

    /**
     * Create an empty button template that requires learning.
     */
    fun addButton(deviceId: String, name: String, carrierFrequency: Int = 38000) {
        viewModelScope.launch {
            val device = repository.getDeviceById(deviceId) ?: return@launch
            val newButton = Button(name = name, carrierFrequency = carrierFrequency)
            val updatedButtons = device.buttons + newButton
            val updatedDevice = device.copy(buttons = updatedButtons)

            repository.insertDevice(updatedDevice)
            _selectedDevice.value = updatedDevice
        }
    }

    /**
     * Direct addition of a completely constructed Button.
     */
    fun addButtonDirect(deviceId: String, button: Button) {
        viewModelScope.launch {
            val device = repository.getDeviceById(deviceId) ?: return@launch
            val updatedButtons = device.buttons + button
            val updatedDevice = device.copy(buttons = updatedButtons)

            repository.insertDevice(updatedDevice)
            _selectedDevice.value = updatedDevice
        }
    }

    /**
     * Update an existing buttons template with raw ir signals from training mode.
     */
    fun updateButtonPattern(deviceId: String, buttonId: String, pattern: List<Int>, carrierFrequency: Int) {
        viewModelScope.launch {
            val device = repository.getDeviceById(deviceId) ?: return@launch
            val updatedButtons = device.buttons.map { button ->
                if (button.id == buttonId) {
                    button.copy(irPattern = pattern, carrierFrequency = carrierFrequency)
                } else {
                    button
                }
            }
            val updatedDevice = device.copy(buttons = updatedButtons)

            repository.insertDevice(updatedDevice)
            _selectedDevice.value = updatedDevice

            Log.d("IrViewModel", "Successfully saved training pattern for button $buttonId")
        }
    }

    /**
     * Delete a single button from a device.
     */
    fun deleteButton(deviceId: String, buttonId: String) {
        viewModelScope.launch {
            val device = repository.getDeviceById(deviceId) ?: return@launch
            val updatedButtons = device.buttons.filter { it.id != buttonId }
            val updatedDevice = device.copy(buttons = updatedButtons)

            repository.insertDevice(updatedDevice)
            _selectedDevice.value = updatedDevice
        }
    }

    /**
     * Transmit the custom raw IR pattern over physical device.
     * Triggers vibration and screen flash even if there is no physical emitter for UX satisfaction.
     */
    fun transmitIr(button: Button) {
        val pattern = button.irPattern ?: return
        triggerHapticFeedback()

        viewModelScope.launch {
            _isTransmitting.value = true
            _lastTransmittedButton.value = button

            val success = irTransmitter.transmit(button.carrierFrequency, pattern)
            if (!success) {
                // Since most emulators lack IR, provide simulation message details in system logs
                Log.d("IrViewModel", "Simulated Transmission of ${button.name}: Freq=${button.carrierFrequency}Hz, pulses=${pattern.size}")
            }

            // Keep indicator active for a moments
            delay(400)
            _isTransmitting.value = false
            _lastTransmittedButton.value = null
        }
    }

    /**
     * Reset camera capture states when entering/exiting camera feeds.
     */
    fun clearCaptureState() {
        _capturedPattern.value = null
        _captureLogs.value = emptyList()
    }

    /**
     * Handle temporary captures during interactive scanner.
     */
    fun registerLuminanceSpike(delta: Double) {
        val newLog = "Световой всплеск: +${String.format("%.1f", delta)} люм."
        _captureLogs.value = (_captureLogs.value + newLog).takeLast(6)
    }

    fun registerCapturedSignal(pattern: List<Int>) {
        _capturedPattern.value = pattern
        val newLog = "ИК сигнал уловлен! Паттерн: ${pattern.size} импульсов"
        _captureLogs.value = (_captureLogs.value + newLog).takeLast(6)
    }

    /**
     * Generates device placeholders to help user explore features immediately.
     */
    private suspend fun createDefaultDevices() {
        // Device 1: Smart TV
        val tvButtons = listOf(
            Button(id = UUID.randomUUID().toString(), name = "Вкл/Выкл (Power)", irPattern = generateDefaultIrPattern(true), carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Громкость +", irPattern = generateDefaultIrPattern(false), carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Громкость -", irPattern = generateDefaultIrPattern(false), carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Канал +", irPattern = null, carrierFrequency = 38000), // requires training
            Button(id = UUID.randomUUID().toString(), name = "Mute", irPattern = null, carrierFrequency = 38000)      // requires training
        )
        val tvDevice = Device(name = "Телевизор в гостиной", type = "TV", buttons = tvButtons)

        // Device 2: Chinese RGB Bulb
        val lampButtons = listOf(
            Button(id = UUID.randomUUID().toString(), name = "Свет (Вкл)", irPattern = generateDefaultIrPattern(true), carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Свет (Выкл)", irPattern = generateDefaultIrPattern(true), carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Режим: Красный", irPattern = null, carrierFrequency = 38000),
            Button(id = UUID.randomUUID().toString(), name = "Режим: Синий", irPattern = null, carrierFrequency = 38000)
        )
        val lampDevice = Device(name = "Китайская лампа", type = "LAMP", buttons = lampButtons)

        repository.insertDevice(tvDevice)
        repository.insertDevice(lampDevice)
    }

    private fun generateDefaultIrPattern(isPower: Boolean): List<Int> {
        val pattern = mutableListOf<Int>()
        if (isPower) {
            // NEC lead-in
            pattern.add(9000); pattern.add(4500)
            // 8 data pulses
            for (i in 0 until 8) {
                pattern.add(560)
                pattern.add(if (i % 2 == 0) 1690 else 560)
            }
        } else {
            // Sony lead-in
            pattern.add(2400); pattern.add(600)
            for (i in 0 until 12) {
                pattern.add(1200)
                pattern.add(600)
            }
        }
        return pattern
    }

    private fun triggerHapticFeedback() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(80)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("IrViewModel", "Failed to perform haptic feedback", e)
        }
    }
}
