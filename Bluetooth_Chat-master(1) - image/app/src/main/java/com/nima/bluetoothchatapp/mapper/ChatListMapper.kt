package com.nima.bluetoothchatapp.mapper

import com.nima.bluetoothchatapp.database.entities.ConnectedDevices
import com.nima.bluetoothchatapp.C_devices.BL_Device
import javax.inject.Inject
// "@Inject" để Hilt có thể tạo ra đối tượng của lớp này khi cần thiết.
//Class này là một mapper (bộ chuyển đổi) giữa hai đối tượng dữ liệu
// ConnectedDevices và BLDevice.
// Mapper này implement interface
// "EntityMapper" với các phương thức "mapFromEntity" và "mapToEntity"
// để chuyển đổi giữa hai đối tượng.
class ChatListMapper @Inject constructor() :EntityMapper<ConnectedDevices,BL_Device> {
//    Phương thức "mapFromEntity" chuyển đổi đối tượng ConnectedDevices thành đối tượng BLDevice.
    override fun mapFromEntity(entity: ConnectedDevices): BL_Device {
//    sử dụng phương thức "apply" để truy cập vào các thuộc tính của đối tượng và trả về đối tượng đã được chuyển đổi.
        entity.apply {
            return BL_Device(
                deviceName = deviceName,
                deviceAddress = deviceAddress,
                date = date
            )
        }
    }
//Phương thức "mapToEntity" chuyển đổi đối tượng BLDevice thành đối tượng ConnectedDevices.
    override fun mapToEntity(domainModel: BL_Device): ConnectedDevices {
//    sử dụng phương thức "apply" để truy cập vào các thuộc tính của đối tượng và trả về đối tượng đã được chuyển đổi.
        domainModel.apply {
            return ConnectedDevices(
                id = 0,
                chatId = deviceAddress,
                deviceName = deviceName,
                deviceAddress = deviceAddress,
                date = date
            )
        }
    }
//Phương thức "mapFromEntityList" chuyển đổi danh sách các đối tượng
// ConnectedDevices thành danh sách các đối tượng BLDevice.
    fun mapFromEntityList(response: List<ConnectedDevices>): List<BL_Device?> =
        response.map { mapFromEntity(it) }
//Phương thức "mapToEntityList" chuyển đổi danh sách các đối tượng
// BLDevice thành danh sách các đối tượng ConnectedDevices.
    fun mapToEntityList(response: List<BL_Device>): List<ConnectedDevices?> =
        response.map { mapToEntity(it) }
}