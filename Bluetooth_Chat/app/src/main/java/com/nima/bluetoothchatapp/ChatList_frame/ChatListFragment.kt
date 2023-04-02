package com.nima.bluetoothchatapp.ChatList_frame

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nima.bluetoothchatapp.Constants
import com.nima.bluetoothchatapp.R
import com.nima.bluetoothchatapp.C_devices.BL_Device
import com.nima.bluetoothchatapp.C_devices.phat_hien_Devices_DialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment(R.layout.fragment_chat_list), phat_hien_Devices_DialogFragment.OnClick,
    ChatListAdapter.Interaction {

    private lateinit var chatListAdapter: ChatListAdapter
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var emptyChatList: TextView
    private lateinit var tim_Devices: FloatingActionButton
    private lateinit var recycler_chat: RecyclerView
    private val viewMode: ChatListViewModel by viewModels()
    private var blDevices: List<BL_Device?>? = null
    private val YEU_CAU_BAT_BL = 3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tim_Devices = view.findViewById(R.id.tim_Devices_XML)
        emptyChatList = view.findViewById(R.id.emptyChatList_XML)
        recycler_chat = view.findViewById(R.id.recycler_chat_XML)
        //lấy và hiển thị các thiết bị đã ghép nối
        getConnectedDevices()
        // Adapter với RecylerView
        init_RecyclerView()
        //nhấn vào nút tìm kết nối , hiểm thị dialogFragment hiển thị các thiết bị đã được ghép nối với thiết bị
        load_ket_noi()
        //hiển thị tb không có bl
        setBluetoothAdapter()
    }
// Adapter với RecylerView
    private fun init_RecyclerView() {
        recycler_chat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            chatListAdapter = ChatListAdapter(this@ChatListFragment)
            adapter = chatListAdapter
        }
    }
//show các tb đã ghép nối
    private fun showConnectedDevices(devices: List<BL_Device?>) {
        if (devices.isNotEmpty()) {
            submitDevices(devices)
        } else emptyChatList.visibility = View.VISIBLE
    }
    private fun submitDevices(devices: List<BL_Device?>) {
        chatListAdapter.submitList(devices)
    }
// lấy các tb đã ghếp nối đưa vào showw
    private fun getConnectedDevices() {
        viewMode.getConnectedDevices().observe(viewLifecycleOwner) { device ->
            blDevices = device
            Log.d("TAG", "getConnectedDevices: $device")
            showConnectedDevices(device)
        }
    }

    private fun setBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(
                requireContext(),
                "thiết bị không hỗ trợ Bluetooth!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun load_ket_noi() {
//         khi người dùng nhấn vào nút tim_Devices,
        tim_Devices.setOnClickListener {
//          kiểm tra xem Bluetooth đã được bật chưa bằng cách kiểm tra bluetoothAdapter?.isEnabled
//           Nếu Bluetooth chưa được bật, ứng dụng sẽ tạo một Intent để yêu cầu người dùng bật Bluetooth
//           bằng cách sử dụng hằng số YEU_CAU_BAT_BL và gọi phương thức startActivityForResult(). .
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, YEU_CAU_BAT_BL)
            }
//            Nếu Bluetooth đã được bật, ứng dụng sẽ hiển thị danh sách thiết bị đã ghép nối bằng cách gọi phương thức showPairedDevices().
            else showPairedDevices()
        }
    }
// sử dụng để hiển thị các thiết bị Bluetooth đã được ghép nối với thiết bị hiện tại.
    private fun showPairedDevices() {
//        lấy các tb đã ghép nối
        val blDevices = queryPairedDevices()
//    nếu danh sách blDevices khác null,
        blDevices?.let {
//            hàm tạo một đối tượng phat_hien_Devices_DialogFragment và truyền danh sách blDevices vào
//            để hiển thị trong một dialog fragment.
            val pairedDevicesDialogFragment = phat_hien_Devices_DialogFragment(this, it)
            pairedDevicesDialogFragment.show(childFragmentManager, "FromMainActivityToPaired")
        }
    }











//Hàm insertDevice(item: BL_Device) được sử dụng để chèn một thiết bị Bluetooth vào danh sách các thiết bị đã kết nối.
    private fun insertDevice(item: BL_Device) {
//
        blDevices?.let {
//            Nếu danh sách blDevices đã tồn tại và chứa item, thì không làm gì cả.
            if (it.contains(item))
                return
//            Nếu danh sách blDevices đã tồn tại nhưng không chứa item, thì gọi phương thức insertConnectedDevice(item) của viewMode để chèn item vào danh sách.
            else viewMode.insertConnectedDevice(item)
        }
//    Nếu danh sách blDevices chưa tồn tại, thì cũng gọi phương thức insertConnectedDevice(item) của viewMode để tạo mới danh sách và chèn item vào.
        if (blDevices == null) {
            viewMode.insertConnectedDevice(item)
        }
    }
//Hàm queryPairedDevices() là để lấy danh sách các thiết bị Bluetooth đã được ghép nối (paired) với thiết bị Android hiện tại
    private fun queryPairedDevices(): List<BL_Device>? {
//     Đầu tiên, hàm lấy danh sách các thiết bị đã ghép nối bằng cách gọi phương thức bondedDevices trên đối tượng BluetoothAdapter.
        val tim_Devices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
//     Kết quả trả về là một tập hợp các đối tượng BluetoothDevice. Sau đó, danh sách các thiết bị này
//     được chuyển đổi thành danh sách các đối tượng BL_Device bằng cách sử dụng phương thức map của Kotlin.
        return tim_Devices?.map { BL_Device(it.name, it.address) }
    }


    private fun navigateToFragment(item: BL_Device) {
        val bundle = Bundle()
        bundle.putString(Constants.DiaChi_DEVICE, item.deviceAddress)
        bundle.putString(Constants.Ten_DEVICE, item.deviceName)
        findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            YEU_CAU_BAT_BL ->{
                if (resultCode == Activity.RESULT_OK) showPairedDevices() else {
                    Toast.makeText(
                        requireContext(), R.string.BL_chua_duoc_bat_roi_khoi,
                        Toast.LENGTH_SHORT
                    ).show()
                    activity?.finish()
                }
            }
        }
    }

    override fun pairedDeviceSelected(position: Int, item: BL_Device) {
        insertDevice(item)
        navigateToFragment(item)
    }


    override fun onItemSelected(position: Int, item: BL_Device) {
        navigateToFragment(item)
    }
}
