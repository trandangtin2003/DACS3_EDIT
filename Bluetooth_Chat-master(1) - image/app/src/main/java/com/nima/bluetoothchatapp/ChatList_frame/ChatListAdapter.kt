package com.nima.bluetoothchatapp.ChatList_frame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nima.bluetoothchatapp.R
import com.nima.bluetoothchatapp.C_devices.BL_Device
import com.nima.bluetoothchatapp.Chat_frame.ChatAdapter
import com.nima.bluetoothchatapp.message.Message

class ChatListAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var parent : ViewGroup
    private var position: Int = 0
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BL_Device>() {

        override fun areItemsTheSame(oldItem: BL_Device, newItem: BL_Device): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: BL_Device, newItem: BL_Device): Boolean {
            return oldItem.deviceAddress == newItem.deviceAddress
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.parent = parent
        return ChatListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.model_chat_list,
                parent,
                false
            ),
            interaction,
            position
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatListViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
//Phương thức submitList() được sử dụng để cập nhật danh sách và gọi phương thức differ.submitList() để đồng bộ hóa danh sách mới với danh sách cũ.
    fun submitList(list: List<BL_Device?>) {
        differ.submitList(list)
    }

    class ChatListViewHolder(
        itemView: View,
        private val interaction: Interaction?,
        private val pos: Int
    ) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: BL_Device ) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(bindingAdapterPosition, item)
            }
            val deviceName = itemView.findViewById<TextView>(R.id.txt_modelChatListF_deviceName)
//            val date = itemView.findViewById<TextView>(R.id.txt_modelPairedD_date)
            deviceName.text = item.deviceName

//            val deviceMess = itemView.findViewById<TextView>(R.id.txt_modelChatListF_mess)
//            deviceMess.text=

        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: BL_Device)
    }
}