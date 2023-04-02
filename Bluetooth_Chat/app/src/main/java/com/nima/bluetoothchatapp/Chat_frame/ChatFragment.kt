package com.nima.bluetoothchatapp.Chat_frame

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nima.bluetoothchatapp.*
import com.nima.bluetoothchatapp.message.Message_da_gui
import com.nima.bluetoothchatapp.message.MessageStatus
import com.nima.bluetoothchatapp.service.BluetoothChatService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {
    // Layout Views
    private lateinit var EditText_Gui: EditText
    private lateinit var Nut_Gui: Button
    private lateinit var trang_thai_ket_noi : Button
    private lateinit var recycler_chat: RecyclerView
    private lateinit var noMessages: TextView

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var randomUIDGenerator: RandomUIDGenerator

    private val viewMode: ChatViewModel by viewModels()

    private var m_ten_device_duoc_ket_noi: String? = null
    private var m_dia_chi_device_duoc_ket_noi: String? = null
    private var m_dia_chi_device_cua_toi: String? = null


    private var chatId = "-1"
    private var mOutStringBuffer: StringBuffer? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mChatService: BluetoothChatService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        //kiểm tra xem thiết bị Android đã được trang bị Bluetooth hay chưa
//// thông qua việc tìm kiếm BluetoothAdapter mặc định.
        checkForBluetoothAdapter()
        //lấy id của thiết bị được truyền vào FR
        chatId = requireArguments().getString(Constants.DiaChi_DEVICE, "") ?: "-1"
        randomUIDGenerator = RandomUIDGenerator()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        EditText_Gui = view.findViewById(R.id.EditText_Gui_XML)
        Nut_Gui = view.findViewById(R.id.Nut_Gui_XML)
        trang_thai_ket_noi = view.findViewById(R.id.trang_thai_ket_noi_XML)
        recycler_chat = view.findViewById(R.id.recycler_chat_XML)
        noMessages = view.findViewById(R.id.txt_chatF_noMessage)
        //gắn kết recylerView với adapter
        init_RecyclerView()
        //
        load_ket_noi()
        //lấy dữ liệu truyền vào adapter
        subscribeOnChatMessages()
    }
//kiểm tra xem thiết bị Android đã được trang bị Bluetooth hay chưa
// thông qua việc tìm kiếm BluetoothAdapter mặc định.
    @SuppressLint("HardwareIds")
    private fun checkForBluetoothAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//    Nếu BluetoothAdapter không khả dụng,
//    một thông báo Toast sẽ được hiển thị
//    và hoạt động sẽ kết thúc.
        if (mBluetoothAdapter == null) {
            val activity: FragmentActivity = requireActivity()
            Toast.makeText(activity, "Bluetooth không khả dụng", Toast.LENGTH_LONG).show()
            activity.finish()
        } else {
//            Nếu BluetoothAdapter có sẵn, địa chỉ MAC của
//            thiết bị Bluetooth sẽ được lưu trữ vào biến m_dia_chi_device_cua_toi
//            để sử dụng sau này.
            mBluetoothAdapter?.let {
                m_dia_chi_device_cua_toi = it.address
            }
        }
    }

    private fun init_RecyclerView() {
        recycler_chat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            chatAdapter = ChatAdapter()
            adapter = chatAdapter
        }
    }
// Khi người dùng nhấn vào trang_thai_ket_noi, hàm connectDevice() được gọi để kết nối với một thiết bị Bluetooth.
    private fun load_ket_noi() {
        trang_thai_ket_noi.setOnClickListener {
            connectDevice()
//             Sau khi kết nối thành công, trang_thai_ket_noi được ẩn đi (sử dụng setVisibility(View.GONE))
//             để người dùng không thể nhấn lại và gây ra lỗi.
            it.visibility = View.GONE
        }
    }
//lấy các tin nhắn liên quan đến cuộc trò chuyện hiện tại từ ViewModel
// bằng cách sử dụng một Flow trả về danh sách tin nhắn.
    private fun subscribeOnChatMessages() {
//    CoroutineScope để chạy các hoạt động trên Dispatchers.Main (luồng chính),
        CoroutineScope(Dispatchers.Main).launch {
            viewMode.getAllMessages(chatId)?.collect { messages ->
                showMessages(messages)
                withContext(Dispatchers.Main) {
                    messages.forEach {
                        Log.d(TAG, "getChatHistory: $it")
                    }
                }
            }
        }
    }

    private fun changeStatus(state : String){
        trang_thai_ket_noi.apply {
            text = state
            visibility = View.VISIBLE
        }
    }

    private fun showMessages(messages: List<com.nima.bluetoothchatapp.message.Message?>) {
//        Nếu danh sách messages không rỗng,
        if(messages.isNotEmpty()){
//            thì hàm sẽ ẩn đi view hiển thị "Không có tin nhắn"
            showEmptyMessageView(false)
//            submit danh sách messages cho adapter để hiển thị lên RecyclerView.
            chatAdapter.submitList(messages)
//            cuộn RecyclerView đến cuối danh sách tin nhắn bằng cách
//            sử dụng phương thức smoothScrollToPosition()
            recycler_chat.smoothScrollToPosition(messages.size)
        }
//        Nếu danh sách messages rỗng, hàm sẽ hiển thị view "Không có tin nhắn" lên màn hình.
        else if (messages.isEmpty()) showEmptyMessageView(true)
    }
    private fun showEmptyMessageView(show :Boolean){
//        Nếu show là true, nó sẽ hiển thị view "Không có tin nhắn" lên màn hình và ẩn đi RecyclerView,
        noMessages.isVisible = show
//        ngược lại nếu show là false thì sẽ ẩn đi view "Không có tin nhắn" và hiển thị RecyclerView.
        recycler_chat.isVisible = !show
    }
//Hàm setupChat() được gọi khi ứng dụng được khởi động.
// Nó thiết lập một trình điều khiển Bluetooth (mChatService)
// và chuỗi đệm để lưu các dữ liệu gửi đi (mOutStringBuffer).
    private fun setupChat() {
        Nut_Gui.setOnClickListener {
            val view: View? = view
            if (null != view) {
                val textView = view.findViewById<View>(R.id.EditText_Gui_XML) as TextView
                val message = textView.text.toString()
                val m = Message_da_gui(
                    true,
                    MessageStatus.TrangThai_khongco_Thu(),
                    randomUIDGenerator.generate(),
                    message
                )
                sendMessage(m)
            }
        }
        mChatService = BluetoothChatService(requireContext(), xu_ly_thongdiep_duoc_gui_tu_BLChatService)
        mOutStringBuffer = StringBuffer("")
    }

    private fun insertMessage(
        writeMessage: String,
        chatId: String,
        uId: String,
        senderId: String,
        status :MessageStatus,
        isMe: Boolean,
        fatherId: Int
    ) {
        viewMode.insertMessage(writeMessage, chatId, uId, senderId, isMe, fatherId,status)
    }

    private fun sendMessage(message: Message_da_gui) {
//        Nếu trạng thái hiện tại của dịch vụ không phải là BluetoothChatService.STATE_CONNECTED
        if (mChatService!!.state != BluetoothChatService.STATE_CONNECTED) {
//            hàm sẽ thực hiện việc thêm tin nhắn vào cơ sở dữ liệu và xóa nội dung của EditText_Gui.
            message.apply {
                if (content.isNotEmpty()){
                    insertMessage(content,chatId,UID,chatId,status,true,-1)
                    EditText_Gui.setText("")
                }
            }
            return
        }
//        thì hàm sẽ gọi hàm writeMessage để gửi tin nhắn thông qua Bluetooth.
        writeMessage(message)
    }

//    Gởi mảng Byte qua thiết bị khác
    private fun writeMessage(message : Message_da_gui){
        if (message.content.isNotEmpty()) {
//            Trước khi gửi tin nhắn đi,
//            hàm chuyển đổi nó thành một mảng byte thông qua phương thức encode() của đối tượng Message_da_gui.
            val Gui = message.encode().toByteArray()
            mChatService!!.write(Gui)
            mOutStringBuffer!!.setLength(0)
//             Sau khi gửi tin nhắn đi, hàm reset lại giá trị của mOutStringBuffer
//             và xóa nội dung trong EditText_Gui.
            EditText_Gui.setText(mOutStringBuffer)
        }
    }
//Hàm xu_ly_trang_thai_ket_noi() có nhiệm vụ xử lý trạng thái kết nối Bluetooth và
// ẩn hiển thị trạng thái kết nối trên giao diện người dùng khi kết nối thành công.
    private fun xu_ly_trang_thai_ket_noi() {
        trang_thai_ket_noi.visibility = View.GONE
//    hàm xu_ly_mess_gui_that_bai() để xử lý các tin nhắn chưa gửi thành công
//    trong trường hợp kết nối bị gián đoạn hoặc mất kết nối.
        xu_ly_mess_gui_that_bai()
    }

//Hàm xu_ly_mess_gui_that_bai() được sử dụng để xử lý các tin nhắn gửi thất bại
// trong trường hợp đang kết nối Bluetooth nhưng không thành công.
    private fun xu_ly_mess_gui_that_bai() {
//    hàm sử dụng CoroutineScope để chạy các tác vụ liên quan đến cơ sở dữ liệu
//    trong một luồng riêng biệt (Dispatchers.IO), để không làm chậm giao diện người dùng.
        CoroutineScope(Dispatchers.IO).launch {
//            getMyFailedMessages(chatId) để lấy danh sách các tin nhắn gửi thất bại
//            của người dùng hiện tại trong phòng chat hiện tại.
            val messages = viewMode.getMyFailedMessages(chatId)
            withContext(Dispatchers.Main) {
//                Nếu danh sách này không rỗng,
                if (messages.isNotEmpty()) {
//                     hàm lặp lại từng tin nhắn trong danh sách
                    messages.forEach {
                        Log.d(TAG, "xu_ly_mess_gui_that_bai: ${it?.content()}")
                        it?.let { message ->
//                            gọi hàm sendMessage() để gửi lại tin nhắn này
                            sendMessage(
                                Message_da_gui(
                                    isMe = true,
//                                    trạng thái MessageStatus.TrangThai_Thu_Da_Gui()
                                    status = MessageStatus.TrangThai_Thu_Da_Gui(),
                                    UID = message.content().uId,
                                    content = message.content().content
                                )
                            )
                        }
                    }
                }
            }
        }
    }
// xu_ly_mess_gui được sử dụng để xử lý thông điệp được gửi đi.
    private fun xu_ly_mess_gui(mAck: Message_da_gui) {
//    Nếu trạng thái của thông điệp là TrangThai_khongco_Thu(), nghĩa là đây là một thông điệp mới được gửi,
        if (mAck.status == MessageStatus.TrangThai_khongco_Thu()) {
//          phương thức sẽ gọi hàm insertMessage để lưu thông điệp này vào cơ sở dữ liệu.
            insertMessage(mAck.content, chatId, mAck.UID, m_dia_chi_device_cua_toi!!,MessageStatus.TrangThai_Thu_Da_Gui(), true, -1)
        }
    }
//xu_ly_mess_phia_may_khach được sử dụng để xử lý các tin nhắn được nhận từ thiết bị khác thông qua Bluetooth.
    private fun xu_ly_mess_phia_may_khach(readMessage: String) {
//    hàm này nhận một chuỗi đại diện cho tin nhắn được mã hóa,
//    giải mã tin nhắn đó thành đối tượng mAck kiểu Message_da_gui
        val mAck = readMessage.decode()
//     Nếu tin nhắn đã được xem (TrangThai_Thu_Da_Seen)
//     thì sẽ cập nhật trạng thái của tin nhắn ở phía mình thông qua hàm cap_nhat_trang_thai_tn_cua_toi.
        if (mAck.status == MessageStatus.TrangThai_Thu_Da_Seen()) {
//            mình cập nhật lại trạng thái đã xem của tn của mình
//            từ cái mess_da_xem như bên dưới họ gởi lại mình .TrangThai_Thu_Da_Seen() của tn mình gởi qua để mình cập nhật
            cap_nhat_trang_thai_tn_cua_toi(mAck)
            Log.d(TAG, "xu_ly_mess_phia_may_khach: haslkdfjladsf")
        }
//        tn của họ qua mình
//        nó sẽ có trạng thái là đã gửi
        else {
//            Nếu tin nhắn chưa được xem bên mình thì hàm sẽ lưu trữ tin nhắn đó
//            và gửi lại tin nhắn xác nhận (mess_da_xem) để thông báo rằng tin nhắn đã được nhận.
            Log.d(TAG, "xu_ly_mess_phia_may_khach: $readMessage")
//            tn của họ gửi tới , mình lưu vào csdl của mình với isMe là false
            mess_da_gui_chua_xem(mAck)
//          mình gởi lại họ trạng thái tn của họ là .TrangThai_Thu_Da_Seen()
            mess_da_xem(mAck)
        }
    }
// được sử dụng để cập nhật trạng thái của tin nhắn được gửi đi. 
// Khi nhận được thông báo từ thiết bị đích rằng tin nhắn đã được xem, 
// trạng thái của tin nhắn được cập nhật thành TrangThai_Thu_Da_Seen() bằng cách gọi hàm này.
    private fun cap_nhat_trang_thai_tn_cua_toi(mAck: Message_da_gui) {
        mAck.apply {
            viewMode.cap_nhat_trang_thai_tn_cua_toi(status, UID, content)
        }
    }
//Phương thức mess_da_gui_chua_xem nhận vào một đối tượng Message_da_gui,
// sau đó thực hiện lưu trữ thông tin tin nhắn đó vào cơ sở dữ liệu.
    private fun mess_da_gui_chua_xem(mAck: Message_da_gui) {
        insertMessage(
            writeMessage = mAck.content,
            chatId = chatId,
            uId = mAck.UID,
            senderId = m_dia_chi_device_duoc_ket_noi!!,
            isMe = false,
            fatherId = -1,
            status = MessageStatus.TrangThai_Thu_Da_Gui()
        )
    }
//được sử dụng để gửi một thông báo đánh dấu là đã xem (seen) đối với một tin nhắn đã nhận được.
    private fun mess_da_xem(message: Message_da_gui) {
        message.isMe = false
        message.status = MessageStatus.TrangThai_Thu_Da_Seen()
//     gửi lại tin nhắn đã nhận được với trạng thái đã được cập nhật này đi.
        sendMessage(message)
    }

//    sử dụng Handler để xử lý các thông điệp được gửi từ BluetoothChatService.
//    Nó chứa các trường hợp xử lý cho các thông điệp khác nhau,
    private val xu_ly_thongdiep_duoc_gui_tu_BLChatService: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
//                xử lý thay đổi trạng thái kết nối Bluetooth (kết nối, đang kết nối, chưa kết nối).
                Constants.TB_Trang_Thai_thay_doi -> when (msg.arg1) {
                    BluetoothChatService.STATE_CONNECTED -> xu_ly_trang_thai_ket_noi()
                    BluetoothChatService.STATE_CONNECTING -> changeStatus(resources.getString(R.string.dang_ket_noi))
                    BluetoothChatService.STATE_LISTEN, BluetoothChatService.STATE_NONE -> changeStatus(resources.getString(R.string.Chua_ket_noi))
                }
//                 xử lý thông điệp viết từ thiết bị khác gửi đến.
                Constants.Viet_TB -> {
//                  lấy mảng byte
                    val writeBuf = msg.obj as ByteArray
//                  đổi mảng byte sang chuỗi
                    val writeMessage = String(writeBuf)
//                  sử dụng extension function decode()
//                  để giải mã chuỗi UTF-8 từ mảng byte.
                    val message = writeMessage.decode()
                    xu_ly_mess_gui(message)
                }
//                xử lý thông điệp đọc từ thiết bị khác.
                Constants.Doc_TB -> {
//                     thông qua đối tượng msg, nó sẽ được lưu vào một mảng byte readBuf
                    val readBuf = msg.obj as ByteArray
//                    mảng byte này sẽ được chuyển đổi thành chuỗi readMessage với độ dài bằng giá trị msg.arg1
                    val readMessage = String(readBuf, 0, msg.arg1)

                    xu_ly_mess_phia_may_khach(readMessage)
                }
//                : xử lý thông điệp khi thiết bị khác được kết nối.
                Constants.TB_Ten_DEVICE -> {
                    m_ten_device_duoc_ket_noi = msg.data.getString(Constants.Ten_DEVICE)
                    m_dia_chi_device_duoc_ket_noi = msg.data.getString(Constants.DiaChi_DEVICE)
                    chatId = m_dia_chi_device_duoc_ket_noi ?: "-1"
                    Toast.makeText(requireContext(), "Đã kết nối với "
                            + m_ten_device_duoc_ket_noi, Toast.LENGTH_SHORT
                    ).show()
                }
//                 xử lý thông điệp khi cần hiển thị một Toast thông báo.
                Constants.TB_TOAST -> Toast.makeText(
                    requireContext(), msg.data.getString(Constants.TOAST),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
//Hàm onActivityResult được gọi khi một Activity khác kết thúc và trả về kết quả.
//    nó xử lý hai trường hợp
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
//            Nếu requestCode trả về từ REQUEST_CONNECT_DEVICE_SECURE
            REQUEST_CONNECT_DEVICE_SECURE ->
//                resultCode là RESULT_OK
                if (resultCode == Activity.RESULT_OK) {
//                    thì nó gọi hàm connectDevice() với dữ liệu trả về từ activity con.
                    data?.let { connectDevice() }
                }
            YEU_CAU_BAT_BL ->
                if (resultCode == Activity.RESULT_OK) {
                    setupChat()
                } else {
                    //  nghĩa là người dùng không bật Bluetooth
                    //  hoặc có lỗi xảy ra, thì nó sẽ hiển thị thông báo và kết thúc activity hiện tại.
                    Log.d(TAG, "BT not enabled")
                    Toast.makeText(
                        requireContext(), R.string.BL_chua_duoc_bat_roi_khoi,
                        Toast.LENGTH_SHORT
                    ).show()
                    activity?.finish()
                }
        }
    }


    private fun connectDevice() {
//        .getRemoteDevice(chatId) là phương thức được gọi trên đối tượng BluetoothAdapter
//        để lấy đối tượng BluetoothDevice tương ứng với địa chỉ Bluetooth
//        được cung cấp trong biến chatId.
        val device = mBluetoothAdapter!!.getRemoteDevice(chatId)
//        mChatService là một đối tượng BluetoothChatService,
//        được khởi tạo và sử dụng để quản lý kết nối Bluetooth
//        giữa thiết bị hiện tại và thiết bị được chọn.
//        mChatService?.connect(device, true)
//        là phương thức được gọi để bắt đầu kết nối Bluetooth với
//        thiết bị được chỉ định (device), với tham số thứ hai
//        đánh dấu cho phép thiết bị kết nối lại nếu kết nối bị mất.
        mChatService?.connect(device, true)
    }

    override fun onStart() {
        super.onStart()
//        nếu Bluetooth chưa được bật, sẽ hiển thị một Intent yêu cầu bật Bluetooth và đợi kết quả từ người dùng.
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, YEU_CAU_BAT_BL)
        }
//        Nếu Bluetooth đã được bật và mChatService chưa được khởi tạo, thì hàm setupChat() sẽ được gọi để thiết lập kết nối Bluetooth.
        else if (mChatService == null) {
            setupChat()
        }
    }


//, nếu mChatService đã được khởi tạo và trạng thái của nó là STATE_NONE,
// thì mChatService sẽ được khởi động để bắt đầu trao đổi dữ liệu.
    override fun onResume() {
        super.onResume()
        if (mChatService != null) {
            if (mChatService!!.state == BluetoothChatService.STATE_NONE) {
                mChatService!!.start()
            }
        }
    }

//    Trong hàm onDestroy() và onDestroyView(),
//    mChatService sẽ được dừng để giải phóng tài nguyên và ngăn ngừa lỗi xảy ra khi Fragment bị hủy.
    override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mChatService?.stop()
    }

    companion object {
        private const val TAG = "BluetoothChatFragment"

        // Intent request codes
//        để kết nối với một thiết bị Bluetooth an toàn,
        private const val REQUEST_CONNECT_DEVICE_SECURE = 1
//        yêu cầu bật Bluetooth.
        private const val YEU_CAU_BAT_BL = 3
    }
}