package com.nima.bluetoothchatapp.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nima.bluetoothchatapp.R
import dagger.hilt.android.AndroidEntryPoint
//Khi sử dụng @AndroidEntryPoint, chúng ta không cần phải tạo ra instance cho các dependency,
// Hilt sẽ tìm và khởi tạo chúng và chúng ta có thể sử dụng chúng trong lớp mà chúng ta đánh dấu.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 100
    private val YEU_CAU_BAT_BL = 101
    private var bluetoothAdapter: BluetoothAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermission()
        setBluetoothAdapter()
    }
//    Đối tượng bluetoothAdapter được khởi tạo và thiết lập trong hàm
//    setBluetoothAdapter (). Nếu thiết bị không hỗ trợ Bluetooth,
//    ứng dụng sẽ hiển thị thông báo cho người dùng thông qua Toast.makeText().
    private fun setBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }
//kiểm tra quyền truy cập vị trí nền của thiết bị. Nếu phiên bản SDK của thiết bị đang chạy là Android 10 trở lên (API level 29+)
// , và quyền truy cập vị trí nền chưa được cấp cho ứng dụng, hàm checkForPermission() sẽ gọi ActivityCompat.requestPermissions()
// để yêu cầu cấp quyền truy cập vị trí nền. Nếu quyền truy cập được cấp, ứng dụng sẽ được phép sử dụng vị trí của thiết bị
// ngay cả khi ứng dụng không được mở.
    private fun checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_CODE
                )
            }
        }
    }
//Phương thức onActivityResult () được sử dụng để xử lý kết quả trả về
// từ hộp thoại xác nhận Bluetooth.
// Nếu người dùng chấp nhận, ứng dụng sẽ không làm gì cả.
// Nếu người dùng không chấp nhận, finish() được gọi để đóng Activity.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TAG", "onActivityResult: $requestCode")
        when (requestCode) {
            YEU_CAU_BAT_BL -> {
                when (resultCode) {
                    RESULT_OK -> {
                    }
                    RESULT_CANCELED -> {
                        finish()
                    }
                }
            }
        }
    }
}