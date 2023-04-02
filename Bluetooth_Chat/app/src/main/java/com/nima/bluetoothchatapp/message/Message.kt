package com.nima.bluetoothchatapp.message

interface Message {
    fun content() : Content
    fun father() : Father
    fun child() : Child?
}