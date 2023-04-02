package com.nima.bluetoothchatapp.ChatList_frame

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.nima.bluetoothchatapp.C_devices.BL_Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatListViewModel @ViewModelInject constructor(
    private val repository: ChatListRepository
) :ViewModel(){

    fun getConnectedDevices() = repository.getConnectedDevices()



    fun insertConnectedDevice(blDevice: BL_Device) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertConnectedDevice(blDevice)
        }
    }
}