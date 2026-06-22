package com.example.data

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

class IrTransmitter(private val context: Context) {
    private val irManager: ConsumerIrManager? by lazy {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    }

    /**
     * Checks if the device has an IR emitter.
     */
    fun hasIrEmitter(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    /**
     * Transmit the custom IR signal.
     * Raw pattern lists alternating ON/OFF duration times in microseconds.
     */
    fun transmit(carrierFrequency: Int, pattern: List<Int>): Boolean {
        val manager = irManager ?: {
            Log.e("IrTransmitter", "ConsumerIrManager is not supported on this platform")
            false
        }.let { return false }

        if (!manager.hasIrEmitter()) {
            Log.e("IrTransmitter", "No hardware IR emitter found")
            return false
        }

        return try {
            manager.transmit(carrierFrequency, pattern.toIntArray())
            Log.d("IrTransmitter", "Successfully transmitted $carrierFrequency Hz signal: size ${pattern.size}")
            true
        } catch (e: Exception) {
            Log.e("IrTransmitter", "Error transmitting raw IR signal", e)
            false
        }
    }

    /**
     * Query ranges of supported carrier frequencies.
     */
    fun getCarrierFrequenciesString(): String {
        val manager = irManager ?: return "ИК-бластер не поддерживается системой"
        if (!manager.hasIrEmitter()) return "ИК-бластер физически отсутствует"
        return try {
            val ranges = manager.carrierFrequencies
            if (ranges.isEmpty()) {
                "38000 Гц"
            } else {
                ranges.joinToString(", ") { "${it.minFrequency}-${it.maxFrequency} Гц" }
            }
        } catch (e: Exception) {
            "38000 Гц (типичная частота)"
        }
    }
}
