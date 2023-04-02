package com.nima.bluetoothchatapp.mapper

import com.nima.bluetoothchatapp.Constants.Companion.TrangThai_khongco_Thu
import com.nima.bluetoothchatapp.Constants.Companion.TrangThai_Thu_Da_Seen
import com.nima.bluetoothchatapp.Constants.Companion.TrangThai_Thu_Da_Gui
import com.nima.bluetoothchatapp.Constants.Companion.MessageTypeNone
import com.nima.bluetoothchatapp.Constants.Companion.MessageTypeText
import com.nima.bluetoothchatapp.message.*
import com.nima.bluetoothchatapp.database.entities.ChatMessage
import javax.inject.Inject

class ChatMessageMapper @Inject constructor() : EntityMapper<ChatMessage, Message?> {
//    chuyển đổi một đối tượng ChatMessage sang Message
    override fun mapFromEntity(entity: ChatMessage): Message? {
//     Đầu tiên nó kiểm tra kiểu type của ChatMessage.
        return when(entity.type){
//            Nếu nó là MessageTypeText, nó tạo một đối tượng Text mới
            //với các thuộc tính được chuyển đổi từ ChatMessage sang Content.
            MessageTypeText ->Text(
                content = Content(
                    id  = entity.id,
                    chatId = entity.chatId,
                    time = entity.time,
                    content = entity.content,
                    uId = entity.uId,
                    senderId = entity.senderId,
                    isMe = entity.isMe,
                    status = when(entity.status){
                        "1" -> MessageStatus.TrangThai_Thu_Da_Gui(entity.id)
                        "2" -> MessageStatus.TrangThai_Thu_Da_Seen(entity.id)
                        else -> MessageStatus.TrangThai_khongco_Thu(entity.id)
                    }
                ),
                father = Father(entity.fatherId),
                child = null
            )
            else -> null
        }
    }
//Phương thức mapToEntity chuyển đổi một đối tượng Message sang ChatMessage
    override fun mapToEntity(domainModel: Message?): ChatMessage? {
//    Nó kiểm tra xem đối tượng Message có khác null không.
        domainModel?.let {
//                 Nếu không phải null, nó sử dụng các thuộc tính của
//                 Content trong Message để tạo một đối tượng ChatMessage mới
                message ->
            return ChatMessage(
                id = message.content().id,
                chatId = message.content().chatId,
                senderId = message.content().senderId,
                time = message.content().time,
                content = message.content().content,
                uId = message.content().uId,
                isMe = message.content().isMe,
//                Các thuộc tính của status được chuyển đổi từ MessageStatus sang kiểu String.
                status = when(message.content().status){
                    is MessageStatus.TrangThai_khongco_Thu -> TrangThai_khongco_Thu
                    is MessageStatus.TrangThai_Thu_Da_Seen -> TrangThai_Thu_Da_Seen
                    is MessageStatus.TrangThai_Thu_Da_Gui -> TrangThai_Thu_Da_Gui
                },
//                . Nếu Message là một đối tượng Text, type được đặt là MessageTypeText
                type = if (message is Text) MessageTypeText else MessageTypeNone,
//                fatherId được thiết lập.
                fatherId = message.father().id
            )
        }
//     Nếu Message không phải là một đối tượng Text, type được đặt là MessageTypeNone.
//     Phương thức trả về null nếu domainModel là null.
        return null
    }
//mapFromEntityList và mapToEntityList là các phương thức chuyển đổi danh sách
// các đối tượng ChatMessage và Message. Chúng lặp lại danh sách và
// áp dụng phương thức chuyển đổi tương ứng trên mỗi phần tử của danh sách đó.
// Kết quả là danh sách các đối tượng được chuyển đổi.
    fun mapFromEntityList(response: List<ChatMessage>): List<Message?> =
        response.map { mapFromEntity(it) }

    fun mapToEntityList(response: List<Message>): List<ChatMessage?> =
        response.map { mapToEntity(it) }
}