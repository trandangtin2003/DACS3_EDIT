package com.nima.bluetoothchatapp.C_devices

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nima.bluetoothchatapp.R

class phat_hien_Devices_DialogFragment(
    private val onClick : OnClick,
    private val blDevices: List<BL_Device>
    ): DialogFragment(R.layout.fragment_paired_devices), phat_hien_Devices_Adapter.Interaction {
    private lateinit var pairedDevicesAdapter: phat_hien_Devices_Adapter
    private lateinit var recycler_chat : RecyclerView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_chat = view.findViewById(R.id.recycler_pairedDevice_models)
        init_RecyclerView()
        pairedDevicesAdapter.submitList(blDevices)
    }
    private fun init_RecyclerView() {
        recycler_chat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            pairedDevicesAdapter = phat_hien_Devices_Adapter(this@phat_hien_Devices_DialogFragment)
            adapter = pairedDevicesAdapter
        }
    }

    interface OnClick {
        fun pairedDeviceSelected(position: Int, item: BL_Device)
    }

    override fun onItemSelected(position: Int, item: BL_Device) {
        onClick.pairedDeviceSelected(position,item)
        dismiss()
    }
}