package com.nima.bluetoothchatapp.C_devices

data class BL_Device(
    val deviceName : String,
    val deviceAddress:String,
    val date:String? = null
) {
}