package com.example.data

import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {
    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()

    suspend fun getDeviceById(id: String): Device? {
        return deviceDao.getDeviceById(id)
    }

    suspend fun insertDevice(device: Device) {
        deviceDao.insertDevice(device)
    }

    suspend fun deleteDeviceById(id: String) {
        deviceDao.deleteDeviceById(id)
    }

    suspend fun clearAllDevices() {
        deviceDao.clearAllDevices()
    }
}
