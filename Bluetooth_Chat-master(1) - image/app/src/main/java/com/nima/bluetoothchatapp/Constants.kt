package com.nima.bluetoothchatapp

interface Constants {
    companion object {
        // Các loại tin nhắn thông báo được gửi từ Trình xử lý BluetoothChatService
        const val TB_Trang_Thai_thay_doi = 1
        const val Doc_TB = 2
        const val Viet_TB = 3
        const val TB_Ten_DEVICE = 4
        const val TB_TOAST = 5

        // Tên khóa nhận được từ Trình xử lý BluetoothChatService
        const val Ten_DEVICE = "Ten_DEVICE"
        const val DiaChi_DEVICE ="DiaChi_DEVICE"
        const val TOAST = "toast"
        const val DATABASE_NAME = "BLUETOOTH_APPLICATION_CHAT"
        const val SHARED_PREFERENCES = "BCA_SHARED_PREFERENCES"

        //Trạng thái thư
        const val TrangThai_khongco_Thu = "0"
        const val TrangThai_Thu_Da_Gui = "1"
        const val TrangThai_Thu_Da_Seen = "2"

        //Loại Thư
        const val MessageTypeText = "0"
        const val MessageTypeFile = "1"
        const val MessageTypeVoice = "2"
        const val MessageTypeNone = "3"
    }
}