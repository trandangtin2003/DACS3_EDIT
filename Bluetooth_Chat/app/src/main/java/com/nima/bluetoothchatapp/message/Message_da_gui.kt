package com.nima.bluetoothchatapp.message

data class Message_da_gui(
    var isMe : Boolean,
    var status :MessageStatus,
    val UID : String,
    val content: String
)