/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nima.bluetoothchatapp.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.nima.bluetoothchatapp.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * Lớp này thực hiện tất cả các công việc thiết lập và quản lý Bluetooth
 * kết nối với các thiết bị khác. Nó có một chủ đề lắng nghe
 * các kết nối đến, một luồng để kết nối với một thiết bị và một
 * luồng để thực hiện truyền dữ liệu khi được kết nối.
 */
class BluetoothChatService(context: Context?, handler: Handler) {
    // trường thành viên
    private val mAdapter: BluetoothAdapter
    private val xu_ly_thongdiep_duoc_gui_tu_BLChatService: Handler
    private var mSecureAcceptThread: AcceptThread? = null
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    /**
     * Trả về trạng thái kết nối hiện tại.
     */
    @get:Synchronized
    var state: Int
        private set
    private var mNewState: Int

    /**
     * Người xây dựng. Chuẩn bị một phiên BluetoothChat mới.
     * Bối cảnh @param Bối cảnh hoạt động giao diện người dùng
     * Trình xử lý @param Trình xử lý để gửi tin nhắn trở lại Hoạt động giao diện người dùng
     */
    init {
//        Biến mAdapter lưu trữ đối tượng BluetoothAdapter để truy cập vào các chức năng Bluetooth của thiết bị.
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        state = STATE_NONE
        mNewState = state
        xu_ly_thongdiep_duoc_gui_tu_BLChatService = handler
    }

    /**
     * Cập nhật tiêu đề UI theo trạng thái hiện tại của kết nối trò chuyện
     * Phương thức updateUserInterfaceTitle() được sử dụng để cập nhật trạng thái của kết nối Bluetooth
     * và gửi thông điệp tới Handler được cung cấp để cập nhật giao diện người dùng.
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
//        phương thức lấy trạng thái hiện tại của kết nối bằng cách gọi phương thức getState()
        state = state
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + state)
        //        Nếu trạng thái mới khác với trạng thái hiện tại, phương thức gửi thông điệp tới Handler
//        với mã thông điệp Constants.TB_Trang_Thai_thay_doi và trạng thái mới được chuyển đến thông qua đối số mNewState.
        mNewState = state
        // Cung cấp trạng thái mới cho Trình xử lý để Hoạt động giao diện người dùng có thể cập nhật
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(
            Constants.TB_Trang_Thai_thay_doi,
            mNewState,
            -1
        ).sendToTarget()
    }

    /**
     * Bắt đầu dịch vụ trò chuyện. Bắt đầu cụ thể AcceptThread
     * để bắt đầu phiên ở chế độ nghe (máy chủ). Được gọi bởi Hoạt động onResume()
     *
     * Phương thức start() được gọi để bắt đầu quá trình kết nối Bluetooth.
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        //Hủy bất kỳ chuỗi nào đang cố tạo kết nối
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Bắt đầu chuỗi để nghe trên BluetoothServerSocket thông qua lớp AcceptThread.
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread(true)
            mSecureAcceptThread!!.start()
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread(false)
            mInsecureAcceptThread!!.start()
        }
        // Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle()
    }

    /**
     * Bắt đầu ConnectThread để bắt đầu kết nối với thiết bị từ xa.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     *
     * Phương thức connect ở trên đây là một phương thức đồng bộ,
     * nó được sử dụng để kết nối với một thiết bị Bluetooth đã cho.
     * Phương thức này nhận vào một đối tượng BluetoothDevice
     * và một biến boolean secure, biểu thị cho việc kết nối bảo mật hay không.
     */
    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean) {
        Log.d(TAG, "connect to: $device")

        // Hủy bất kỳ chuỗi nào đang cố tạo kết nối
        if (state == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Bắt đầu chuỗi để kết nối với thiết bị đã cho
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Khởi động ConnectedThread để bắt đầu quản lý kết nối Bluetooth
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice, socketType: String) {
        Log.d(TAG, "connected, Socket Type:$socketType")

        // Hủy chủ đề đã hoàn thành kết nối
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        //Hủy chuỗi chấp nhận vì chúng tôi chỉ muốn kết nối với một thiết bị
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }

        // Bắt đầu chuỗi để quản lý kết nối và thực hiện truyền
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread!!.start()

        // Hướng dẫn tên của thiết bị được kết nối trở lại Hoạt động giao diện người dùng
        val msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_Ten_DEVICE)
        val bundle = Bundle()
        bundle.putString(Constants.Ten_DEVICE, device.name)
        bundle.putString(Constants.DiaChi_DEVICE, device.address)
        msg.data = bundle
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg)
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }
        state = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Ghi vào ConnectedThread theo cách không đồng bộ
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    //    Đây là một phương thức trong một lớp BluetoothChatService
    //    của ứng dụng Android. Phương thức này được sử dụng để gửi một mảng byte
    //    (byte array) đến thiết bị Bluetooth đã kết nối.
    fun write(out: ByteArray?) {
        // Tạo đối tượng tạm thời
        var r: ConnectedThread?
        // Đồng bộ hóa một bản sao của ConnectedThread
//        phương thức được đồng bộ hóa bằng từ khóa synchronized,
        synchronized(this) {

//             nếu trạng thái khác STATE_CONNECTED (đã kết nối) thì phương thức sẽ không làm gì cả và kết thúc
            if (state != STATE_CONNECTED) return
            //            Nếu trạng thái đã kết nối, phương thức sẽ gửi mảng byte đến đối tượng ConnectedThread, được lưu trữ trong biến mConnectedThread.
//            được thực hiện trên một bản sao của ConnectedThread (được lưu trữ trong biến r).
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        Log.d("TAG", "sendMessage: sent2")
        //        Lệnh r.write(out) ghi dữ liệu ra một luồng dữ liệu đã được kết nối. Dữ liệu sẽ được chuyển đến thiết bị Bluetooth đích mà BluetoothChatService đã kết nối tới. Tham số out là một mảng byte chứa dữ liệu được gửi đi.
        r!!.write(out)
    }

    /**
     * Cho biết rằng nỗ lực kết nối không thành công và thông báo cho Hoạt động giao diện người dùng.
     */
    private fun connectionFailed() {
        // Hướng dẫn thông báo lỗi quay lại Hoạt động
        val msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_TOAST)
        val bundle = Bundle()
        bundle.putString(
            Constants.TOAST, """
     Không thể kết nối
     
     Thử lại
     """.trimIndent()
        )
        msg.data = bundle
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg)
        state = STATE_NONE
        // Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle()

        //Khởi động lại dịch vụ để khởi động lại chế độ nghe
        start()
    }

    /**
     * Cho biết rằng kết nối đã bị mất và thông báo cho Hoạt động giao diện người dùng.
     */
    private fun connectionLost() {
        // Hướng dẫn thông báo lỗi quay lại Hoạt động
        val msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_TOAST)
        val bundle = Bundle()
        bundle.putString(
            Constants.TOAST, """
     mất kết nối
     
     Thử lại
     """.trimIndent()
        )
        msg.data = bundle
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg)
        state = STATE_NONE
        //Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle()

        //Khởi động lại dịch vụ để khởi động lại chế độ nghe
        start()
    }

    /**
     * Chủ đề này chạy trong khi lắng nghe các kết nối đến. Nó cư xử
     * giống như một máy khách phía máy chủ.
     * Nó chạy cho đến khi kết nối được chấp nhận (hoặc cho đến khi bị hủy).
     *
     * Lớp AcceptThread được định nghĩa là một lớp con của lớp Thread và
     * sử dụng để lắng nghe kết nối từ thiết bị khác.
     */
    private inner class AcceptThread(secure: Boolean) : Thread() {
        // mmServerSocket: đối tượng BluetoothServerSocket,
        // là ổ cắm của máy chủ để lắng nghe kết nối từ các thiết bị khác.
        private val mmServerSocket: BluetoothServerSocket?

        //         loại ổ cắm Bluetooth (Secure hoặc Insecure).
        private val mSocketType: String

        //phương thức khởi tạo AcceptThread, xác định loại ổ cắm
        //và tạo BluetoothServerSocket mới để lắng nghe kết nối.
        init {
            var tmp: BluetoothServerSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Tạo ổ cắm máy chủ nghe mới
            try {
                tmp = if (secure) {
                    mAdapter.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE,
                        MY_UUID_SECURE
                    )
                } else {
                    mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e)
            }
            mmServerSocket = tmp
            state = STATE_LISTEN
        }

        //phương thức chạy luồng lắng nghe kết nối,
        // chấp nhận các kết nối từ các thiết bị khác
        // và tạo một luồng kết nối mới để trao đổi dữ liệu.
        override fun run() {
            Log.d(
                TAG, "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this
            )
            name = "AcceptThread$mSocketType"
            var socket: BluetoothSocket? = null

            // Lắng nghe ổ cắm máy chủ nếu chúng tôi không kết nối
            while (state != STATE_CONNECTED) {
                socket = try {
                    // Đây là một cuộc gọi chặn và sẽ chỉ trở lại trên một
                    //  kết nối thành công hoặc ngoại lệ
                    mmServerSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e)
                    break
                }

                // Nếu một kết nối đã được chấp nhận
                if (socket != null) {
                    synchronized(this) {
                        when (state) {
                            STATE_LISTEN, STATE_CONNECTING ->                                 // Situation normal. Start the connected thread.
                                connected(
                                    socket, socket.remoteDevice,
                                    mSocketType
                                )
                            STATE_NONE, STATE_CONNECTED ->                                 // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", e)
                                }
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: $mSocketType")
        }

        //        phương thức dùng để hủy kết nối của máy chủ, đóng BluetoothServerSocket.
        fun cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this)
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e)
            }
        }
    }

    /**
     * Chuỗi này chạy trong khi cố tạo kết nối gửi đi với một thiết bị.
     * Nó chạy thẳng qua kết nối thành công hoặc thất bại.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Nhận BluetoothSocket để kết nối với thiết bị Bluetooth nhất định
            try {
                tmp = if (secure) {
                    device.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE
                    )
                } else {
                    device.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }
            mmSocket = tmp
            state = STATE_CONNECTING
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Luôn hủy khám phá vì nó sẽ làm chậm kết nối
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // Đây là một cuộc gọi chặn và sẽ chỉ trở lại khi kết nối thành công hoặc một ngoại lệ
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Đóng ổ cắm
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }
                connectionFailed()
                return
            }

            // Đặt lại ConnectThread vì chúng tôi đã hoàn tất
            synchronized(this) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect $mSocketType socket failed", e)
            }
        }
    }

    /**
     * Chủ đề này chạy trong khi kết nối với một thiết bị từ xa. Nó xử lý tất cả các truyền đến và đi.
     */
    private inner class ConnectedThread(socket: BluetoothSocket?, socketType: String) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            state = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Tiếp tục nghe InputStream khi được kết nối
            while (state == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    //Hướng dẫn các byte thu được cho Hoạt động giao diện người dùng
                    xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(
                        Constants.Doc_TB,
                        bytes,
                        -1,
                        buffer
                    )
                        .sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Ghi vào OutStream được kết nối.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)
                Log.d("TAG", "sendMessage: sent3")
                // Chia sẻ tin nhắn đã gửi trở lại Hoạt động giao diện người dùng
                xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(
                    Constants.Viet_TB,
                    -1,
                    -1,
                    buffer
                )
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    companion object {
        // Debugging
        private const val TAG = "BluetoothChatService"

        //Tên cho bản ghi SDP khi tạo ổ cắm máy chủ
        private const val NAME_SECURE = "BluetoothChatSecure"
        private const val NAME_INSECURE = "BluetoothChatInsecure"

        // UUID duy nhất cho ứng dụng này
        private val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

        //Các hằng số cho biết trạng thái kết nối hiện tại
        const val STATE_NONE = 0 // chúng tôi không làm gì cả
        const val STATE_LISTEN = 1 //hiện đang lắng nghe các kết nối đến
        const val STATE_CONNECTING = 2 // hiện đang bắt đầu một kết nối gửi đi
        const val STATE_CONNECTED = 3 //hiện được kết nối với một thiết bị từ xa
    }
}
