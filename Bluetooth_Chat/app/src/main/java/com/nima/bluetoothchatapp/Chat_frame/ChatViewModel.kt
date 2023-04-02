package com.nima.bluetoothchatapp.Chat_frame

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.nima.bluetoothchatapp.message.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ChatViewModel @ViewModelInject constructor
    (private val repository: ChatRepository) : ViewModel() {

    private var failedMessages: List<Message?>? = null
    private var allMessages: Flow<List<Message?>>? = null
//chèn một tin nhắn mới vào cơ sở dữ liệu.
    fun insertMessage(
        writeMessage: String,
        chatId: String,
        uid: String,
        senderId: String,
        isMe: Boolean,
        fatherId: Int,
        status: MessageStatus
    ) {
        val message = Text(
            content = Content(
                0,
                chatId,
                getTimeCurrent(),
                uid,
                writeMessage,
                senderId,
                isMe,
                status
            ),
            father = Father(fatherId),
            child = null
        )
        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(message)
        }
    }
// được sử dụng để trả về thời gian hiện tại ở định dạng chuỗi.
    private fun getTimeCurrent(): String {
        val currentDateTime = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd G 'at' h:mm a");
        return dateFormat.format(currentDateTime.time).toString()
    }
//được sử dụng để cập nhật trạng thái của một tin nhắn của người dùng.
    fun cap_nhat_trang_thai_tn_cua_toi(status: MessageStatus, uId: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.cap_nhat_trang_thai_tn_cua_toi(status, uId, message)
        }
    }
//được sử dụng để trả về danh sách các tin nhắn không thành công của người dùng.
    fun getMyFailedMessages(chatID: String): List<Message?> {
        return repository.getMyFailedMessages(chatID)
    }
//ược sử dụng để trả về một luồng dữ liệu của tất cả các tin nhắn của một cuộc trò chuyện cụ thể. Luồng dữ liệu này có thể được quan sát để theo dõi các thay đổi về tin nhắn và đưa chúng lên giao diện người dùng.
    fun getAllMessages(chatID: String): Flow<List<Message?>>? {
        return repository.getAllMessages(chatID)?.distinctUntilChanged()
    }

}