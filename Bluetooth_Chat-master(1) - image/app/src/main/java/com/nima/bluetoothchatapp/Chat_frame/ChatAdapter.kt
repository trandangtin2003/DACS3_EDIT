package com.nima.bluetoothchatapp.Chat_frame

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nima.bluetoothchatapp.R
import com.nima.bluetoothchatapp.message.Message
import com.nima.bluetoothchatapp.message.MessageStatus
import com.nima.bluetoothchatapp.toTime

class ChatAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var parent : ViewGroup
    private var position: Int = 0
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.content() == newItem.content()
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return (oldItem.content().id == newItem.content().id)
        }

    }
//    differ là một đối tượng AsyncListDiffer
//    được sử dụng để cập nhật danh sách item của adapter.
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

//Phương thức onCreateViewHolder được gọi khi RecyclerView cần một ViewHolder mới
// để hiển thị dữ liệu. Nó khởi tạo một đối tượng ChatViewHolder và trả về nó.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.parent = parent
        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.model_chat_item,
                parent,
                false
            ),
            position
        )
    }
//Phương thức onBindViewHolder được gọi khi RecyclerView cần hiển thị một ViewHolder đã có dữ liệu.
// Nó kết nối dữ liệu với ViewHolder được chỉ định.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
//được sử dụng để cập nhật danh sách hiện tại của adapter.
    fun submitList(list: List<Message?>) {
        differ.submitList(list)
    }

    class ChatViewHolder(
        itemView: View,
        private val pos: Int
    ) : RecyclerView.ViewHolder(itemView) {

        private lateinit var relativeLayout : RelativeLayout
        private lateinit var message : TextView
        private lateinit var state : ImageView
        private lateinit var datAndTime : TextView
        fun bind(item: Message) = with(itemView) {
            relativeLayout =findViewById(R.id.rl_chatItem_layout)
            message  =findViewById(R.id.txt_chatItem_content)
            state  =findViewById(R.id.img_chatItem_status)
            datAndTime  =findViewById(R.id.txt_chatItem_time)
//            Đối với mỗi tin nhắn, nó sẽ kiểm tra xem tin nhắn đó được gửi bởi mình hay không
//            bằng cách sử dụng thuộc tính isMe của đối tượng MessageContent.
            item.content().apply {
//                Nếu tin nhắn được gửi bởi mình
                if (isMe) {
//                    hình ảnh trạng thái sẽ được hiển thị
                    state.visibility = View.VISIBLE
//                    layout sẽ được căn giữa bên phải màn hình.
                    (relativeLayout.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END
                    relativeLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.chat_message_host,null)
//                    kiểm tra trạng thái tin nhắn
                    when(status){
                        is MessageStatus.TrangThai_khongco_Thu -> {state.setImageResource(R.drawable.ic_status_none)}
                        is MessageStatus.TrangThai_Thu_Da_Gui -> {state.setImageResource(R.drawable.ic_status_sent)}
                        is MessageStatus.TrangThai_Thu_Da_Seen -> {state.setImageResource(R.drawable.ic_status_seen)}
                    }
                }
//                 Nếu tin nhắn được gửi bởi một người khác,
                else{
//                     layout sẽ được căn giữa bên trái
                    (relativeLayout.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
                    relativeLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.chat_message_guest,null)
//                    hình ảnh trạng thái sẽ không được hiển thị.
                    state.visibility = View.GONE
                }
                message.text = content
                datAndTime.text = time.toTime()
            }
        }
    }
}