package com.nima.bluetoothchatapp

import com.nima.bluetoothchatapp.message.Message_da_gui
import com.nima.bluetoothchatapp.message.MessageStatus
//được định nghĩa để chuyển đổi một chuỗi dạng String
// thành một đối tượng Message_da_gui
fun String.decode(): Message_da_gui {
    var isMe = true
    var status: MessageStatus = MessageStatus.TrangThai_khongco_Thu(0)
    var uId = "0000"
    var message = ""
    if (this.isNotEmpty()) {
//         lấy ra ký tự đầu tiên của chuỗi, nếu ký tự này là "0",
//         thì tin nhắn đó là tin nhắn từ người dùng hiện tại (mình),
//         nếu không thì đó là tin nhắn từ một người dùng khác.
        var m = this.substring(0, 1)
        isMe = m == "0"
//      lấy ra ký tự thứ hai của chuỗi để xác định trạng thái của tin nhắn
        m = this.substring(1, 2)
        when (m) {
            "0" -> {
                status = MessageStatus.TrangThai_khongco_Thu()
            }
            "1" -> {
                status = MessageStatus.TrangThai_Thu_Da_Gui()
            }
            "2"-> {
                status = MessageStatus.TrangThai_Thu_Da_Seen()
            }
        }
//       lấy ra 4 ký tự tiếp theo để xác định ID của người gửi
        uId = this.substring(2, 6)
//        nội dung của tin nhắn.
        message = this.substring(6)
    }
    return Message_da_gui(isMe, status, uId, message)
}
//ngược lại đổi
fun Message_da_gui.encode(): String {
//    đổi các giá trị trong biến data thành string luu trong StringBuilder()
    val sb = StringBuilder()
    this.apply {
//        kiểm tra isMe nếu là true thì thêm vào là 0 ngược lại
        if (this.isMe) sb.append("0") else sb.append("1")
        when (this.status) {
            is MessageStatus.TrangThai_khongco_Thu -> sb.append("0")
            is MessageStatus.TrangThai_Thu_Da_Seen -> sb.append("2")
            is MessageStatus.TrangThai_Thu_Da_Gui -> sb.append("1")
        }
        sb.append(this.UID)
        sb.append(this.content)
    }
    return sb.toString()
}