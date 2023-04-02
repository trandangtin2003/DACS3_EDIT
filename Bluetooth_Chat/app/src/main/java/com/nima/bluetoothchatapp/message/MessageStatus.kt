package com.nima.bluetoothchatapp.message

sealed class MessageStatus {
    data class TrangThai_Thu_Da_Gui(var id: Int? = null) : MessageStatus()
    data class TrangThai_Thu_Da_Seen(var id: Int? = null) : MessageStatus()
    data class TrangThai_khongco_Thu(var id: Int? = null) : MessageStatus()
}