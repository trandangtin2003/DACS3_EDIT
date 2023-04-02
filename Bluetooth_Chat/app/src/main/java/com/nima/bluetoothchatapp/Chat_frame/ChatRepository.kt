package com.nima.bluetoothchatapp.Chat_frame

import androidx.lifecycle.LiveData
import com.nima.bluetoothchatapp.message.Message
import com.nima.bluetoothchatapp.message.MessageStatus
import com.nima.bluetoothchatapp.database.MyDao
import com.nima.bluetoothchatapp.database.entities.ChatMessage
import com.nima.bluetoothchatapp.mapper.ChatMessageMapper
import kotlinx.coroutines.flow.*
import javax.inject.Inject
//Đây là một lớp ChatRepository được chú thích bằng @Inject
// để cho phép nó được sử dụng như một phụ thuộc của các lớp khác
// trong quá trình dependency injection.
class ChatRepository @Inject constructor(private val myDao: MyDao) {

    @Inject
    lateinit var chatMessageMapper: ChatMessageMapper
//       getNewMessage(id: Int): Phương thức này trả về một đối tượng LiveData
    //   chứa danh sách các tin nhắn mới nhất cho một cuộc trò chuyện
    //   được xác định bởi ID.
    fun getNewMessage(id: Int): LiveData<List<ChatMessage>> {
        return myDao.getLastMessage(id)
    }

    //Phương thức này trả về một đối tượng Flow chứa danh sách tất cả
    // các tin nhắn cho một cuộc trò chuyện được xác định bởi chatId
    fun getAllMessages(chatId: String): Flow<List<Message?>>? {
        val messages = myDao.getMessages(chatId)
        return messages.map { chatMessageMapper.mapFromEntityList(it) }

    }
//Phương thức này chuyển đổi một đối tượng Message sang đối tượng ChatMessage
// và sau đó chèn nó vào cơ sở dữ liệu thông qua đối tượng DAO.
    fun insert(message: Message) {
        val chatMessage = chatMessageMapper.mapToEntity(message)
        chatMessage?.let { myDao.insertMessage(it) }
    }
// Phương thức này cập nhật trạng thái của tin nhắn trong cơ sở dữ liệu
// dựa trên trạng thái của MessageStatus, uId và message.
    fun cap_nhat_trang_thai_tn_cua_toi(status: MessageStatus, uId: String, message: String) {
        val state = when (status) {
            is MessageStatus.TrangThai_khongco_Thu -> "0"
            is MessageStatus.TrangThai_Thu_Da_Seen -> "2"
            is MessageStatus.TrangThai_Thu_Da_Gui -> "1"
        }
        myDao.cap_nhat_trang_thai_tn_cua_toi(state, uId, message)
    }
// Phương thức này trả về danh sách các tin nhắn không thành công
// cho một cuộc trò chuyện được xác định bởi chatID.
    fun getMyFailedMessages(chatID: String): List<Message?> {
        val failedMessages = myDao.getMyFailedMessages(chatID)
        return failedMessages.map { chatMessageMapper.mapFromEntity(it) }
    }
}