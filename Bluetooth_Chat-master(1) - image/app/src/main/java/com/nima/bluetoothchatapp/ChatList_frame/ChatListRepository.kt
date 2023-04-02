package com.nima.bluetoothchatapp.ChatList_frame

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.nima.bluetoothchatapp.database.MyDao
import com.nima.bluetoothchatapp.C_devices.BL_Device
import com.nima.bluetoothchatapp.mapper.ChatListMapper
import javax.inject.Inject
//lấy thiết bị bluetooth phát hiện được thêm vào các thiết bị đẫ kết nối
class ChatListRepository @Inject constructor(private val dao: MyDao) {

    @Inject
    lateinit var chatLisMapper: ChatListMapper

    fun getConnectedDevices(): LiveData<List<BL_Device?>> {
        val connectedDevices = dao.getConnectedDevices()
        return connectedDevices.map { chatLisMapper.mapFromEntityList(it) }
    }

    fun insertConnectedDevice(blDevice: BL_Device) {
        val device = chatLisMapper.mapToEntity(blDevice)
        dao.insertConnectedDevices(device)
    }
}