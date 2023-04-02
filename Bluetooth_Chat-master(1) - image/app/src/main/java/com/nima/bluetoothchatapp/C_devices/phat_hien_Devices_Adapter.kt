package com.nima.bluetoothchatapp.C_devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nima.bluetoothchatapp.R

class phat_hien_Devices_Adapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var parent : ViewGroup
    private var position: Int = 0
//     DiffUtil.ItemCallback được tạo để xác định các sự khác biệt giữa các mục
//     trong danh sách, bao gồm các hàm areItemsTheSame() và areContentsTheSame().
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BL_Device>() {
//      xác định xem hai mục có tương tự nhau hay không,
        override fun areItemsTheSame(oldItem: BL_Device, newItem: BL_Device): Boolean {
            return oldItem == newItem
        }
//        xác định xem hai mục có nội dung giống nhau hay không.
        override fun areContentsTheSame(oldItem: BL_Device, newItem: BL_Device): Boolean {
            return oldItem.deviceAddress == newItem.deviceAddress
        }

    }
//    quản lý danh sách.
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

//Phương thức onCreateViewHolder() được gọi khi RecyclerView cần một ViewHolder mới để hiển thị một mục.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.parent = parent
    //gắn kết XML item cho RecylerView
        return BLDevicesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.model_paired_devices,
                parent,
                false
            ),
            interaction,
            position
        )
    }
//Phương thức onBindViewHolder() được gọi khi RecyclerView cần cập nhật nội dung của một ViewHolder đã được tạo trước đó.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
//            Phương thức onBindViewHolder() được gọi khi RecyclerView cần cập nhật nội dung của một ViewHolder đã được tạo trước đó.
            is BLDevicesViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
//    Phương thức submitList() được sử dụng để cập nhật danh sách và gọi phương thức differ.submitList() để đồng bộ hóa danh sách mới với danh sách cũ.
    fun submitList(list: List<BL_Device>) {
        differ.submitList(list)
    }

    class BLDevicesViewHolder(
        itemView: View,
        private val interaction: Interaction?,
        private val pos: Int
    ) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: BL_Device) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(bindingAdapterPosition, item)
            }
            val deviceName = itemView.findViewById<TextView>(R.id.txt_modelPairedDev_name)
            val deviceAddress = itemView.findViewById<TextView>(R.id.txt_modelPairedD_address)
            val date = itemView.findViewById<TextView>(R.id.txt_modelPairedD_date)
            deviceAddress.text = item.deviceAddress
            deviceName.text = item.deviceName
            date.text = item.date ?: ""
        }
    }
//Cuối cùng, giao diện Interaction được định nghĩa để tương tác với các mục trên danh sách. Nó có một phương thức onItemSelected() để xử lý sự kiện khi một mục trên danh sách được chọn.
    interface Interaction {
        fun onItemSelected(position: Int, item: BL_Device)
    }
}