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

package com.nima.bluetoothchatapp.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.nima.bluetoothchatapp.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Lớp này thực hiện tất cả các công việc thiết lập và quản lý Bluetooth
 * kết nối với các thiết bị khác. Nó có một chủ đề lắng nghe
 * các kết nối đến, một luồng để kết nối với một thiết bị và một
 *   luồng để thực hiện truyền dữ liệu khi được kết nối.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";

    //Tên cho bản ghi SDP khi tạo ổ cắm máy chủ
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // UUID duy nhất cho ứng dụng này
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // trường thành viên
    private final BluetoothAdapter mAdapter;
    private final Handler xu_ly_thongdiep_duoc_gui_tu_BLChatService;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    //Các hằng số cho biết trạng thái kết nối hiện tại
    public static final int STATE_NONE = 0;       // chúng tôi không làm gì cả
    public static final int STATE_LISTEN = 1;     //hiện đang lắng nghe các kết nối đến
    public static final int STATE_CONNECTING = 2; // hiện đang bắt đầu một kết nối gửi đi
    public static final int STATE_CONNECTED = 3;  //hiện được kết nối với một thiết bị từ xa

    /**
     * Người xây dựng. Chuẩn bị một phiên BluetoothChat mới.
     * Bối cảnh @param Bối cảnh hoạt động giao diện người dùng
     *   Trình xử lý @param Trình xử lý để gửi tin nhắn trở lại Hoạt động giao diện người dùng
     */
    public BluetoothChatService(Context context, Handler handler) {
//        Biến mAdapter lưu trữ đối tượng BluetoothAdapter để truy cập vào các chức năng Bluetooth của thiết bị.
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        xu_ly_thongdiep_duoc_gui_tu_BLChatService = handler;
    }

    /**
     * Cập nhật tiêu đề UI theo trạng thái hiện tại của kết nối trò chuyện
     * Phương thức updateUserInterfaceTitle() được sử dụng để cập nhật trạng thái của kết nối Bluetooth
     * và gửi thông điệp tới Handler được cung cấp để cập nhật giao diện người dùng.
     */
    private synchronized void updateUserInterfaceTitle() {
//        phương thức lấy trạng thái hiện tại của kết nối bằng cách gọi phương thức getState()
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
//        Nếu trạng thái mới khác với trạng thái hiện tại, phương thức gửi thông điệp tới Handler
//        với mã thông điệp Constants.TB_Trang_Thai_thay_doi và trạng thái mới được chuyển đến thông qua đối số mNewState.
        mNewState = mState;
        // Cung cấp trạng thái mới cho Trình xử lý để Hoạt động giao diện người dùng có thể cập nhật
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_Trang_Thai_thay_doi, mNewState, -1).sendToTarget();
    }

    /**
     * Trả về trạng thái kết nối hiện tại.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Bắt đầu dịch vụ trò chuyện. Bắt đầu cụ thể AcceptThread
     * để bắt đầu phiên ở chế độ nghe (máy chủ). Được gọi bởi Hoạt động onResume()
     *
     * Phương thức start() được gọi để bắt đầu quá trình kết nối Bluetooth.
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        //Hủy bất kỳ chuỗi nào đang cố tạo kết nối
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Bắt đầu chuỗi để nghe trên BluetoothServerSocket thông qua lớp AcceptThread.
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        // Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle();
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
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Hủy bất kỳ chuỗi nào đang cố tạo kết nối
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Bắt đầu chuỗi để kết nối với thiết bị đã cho
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Khởi động ConnectedThread để bắt đầu quản lý kết nối Bluetooth
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Hủy chủ đề đã hoàn thành kết nối
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Hủy bất kỳ chuỗi nào hiện đang chạy kết nối
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Hủy chuỗi chấp nhận vì chúng tôi chỉ muốn kết nối với một thiết bị
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Bắt đầu chuỗi để quản lý kết nối và thực hiện truyền
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Hướng dẫn tên của thiết bị được kết nối trở lại Hoạt động giao diện người dùng
        Message msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_Ten_DEVICE);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Ten_DEVICE, device.getName());
        bundle.putString(Constants.DiaChi_DEVICE, device.getAddress());
        msg.setData(bundle);
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Ghi vào ConnectedThread theo cách không đồng bộ
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
//    Đây là một phương thức trong một lớp BluetoothChatService
//    của ứng dụng Android. Phương thức này được sử dụng để gửi một mảng byte
//    (byte array) đến thiết bị Bluetooth đã kết nối.
    public void write(byte[] out) {
        // Tạo đối tượng tạm thời
        ConnectedThread r;
        // Đồng bộ hóa một bản sao của ConnectedThread
//        phương thức được đồng bộ hóa bằng từ khóa synchronized,
        synchronized (this) {
//             nếu trạng thái khác STATE_CONNECTED (đã kết nối) thì phương thức sẽ không làm gì cả và kết thúc
            if (mState != STATE_CONNECTED) return;
//            Nếu trạng thái đã kết nối, phương thức sẽ gửi mảng byte đến đối tượng ConnectedThread, được lưu trữ trong biến mConnectedThread.
//            được thực hiện trên một bản sao của ConnectedThread (được lưu trữ trong biến r).
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        Log.d("TAG", "sendMessage: sent2");
//        Lệnh r.write(out) ghi dữ liệu ra một luồng dữ liệu đã được kết nối. Dữ liệu sẽ được chuyển đến thiết bị Bluetooth đích mà BluetoothChatService đã kết nối tới. Tham số out là một mảng byte chứa dữ liệu được gửi đi.
        r.write(out);
    }

    /**
     * Cho biết rằng nỗ lực kết nối không thành công và thông báo cho Hoạt động giao diện người dùng.
     */
    private void connectionFailed() {
        // Hướng dẫn thông báo lỗi quay lại Hoạt động
        Message msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Không thể kết nối\n"+"\nThử lại");
        msg.setData(bundle);
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg);

        mState = STATE_NONE;
        // Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle();

        //Khởi động lại dịch vụ để khởi động lại chế độ nghe
        this.start();
    }

    /**
     *Cho biết rằng kết nối đã bị mất và thông báo cho Hoạt động giao diện người dùng.
     */
    private void connectionLost() {
        // Hướng dẫn thông báo lỗi quay lại Hoạt động
        Message msg = xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.TB_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "mất kết nối\n"+"\nThử lại");
        msg.setData(bundle);
        xu_ly_thongdiep_duoc_gui_tu_BLChatService.sendMessage(msg);

        mState = STATE_NONE;
        //Cập nhật tiêu đề giao diện người dùng
        updateUserInterfaceTitle();

        //Khởi động lại dịch vụ để khởi động lại chế độ nghe
        this.start();
    }

    /**
     * Chủ đề này chạy trong khi lắng nghe các kết nối đến. Nó cư xử
     * giống như một máy khách phía máy chủ.
     * Nó chạy cho đến khi kết nối được chấp nhận (hoặc cho đến khi bị hủy).
     *
     *  Lớp AcceptThread được định nghĩa là một lớp con của lớp Thread và
     *  sử dụng để lắng nghe kết nối từ thiết bị khác.
     */
    private class AcceptThread extends Thread {
        // mmServerSocket: đối tượng BluetoothServerSocket,
        // là ổ cắm của máy chủ để lắng nghe kết nối từ các thiết bị khác.
        private final BluetoothServerSocket mmServerSocket;
//         loại ổ cắm Bluetooth (Secure hoặc Insecure).
        private String mSocketType;

        //phương thức khởi tạo AcceptThread, xác định loại ổ cắm
        //và tạo BluetoothServerSocket mới để lắng nghe kết nối.
        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Tạo ổ cắm máy chủ nghe mới
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }
        //phương thức chạy luồng lắng nghe kết nối,
        // chấp nhận các kết nối từ các thiết bị khác
        // và tạo một luồng kết nối mới để trao đổi dữ liệu.
        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Lắng nghe ổ cắm máy chủ nếu chúng tôi không kết nối
            while (mState != STATE_CONNECTED) {
                try {
                    // Đây là một cuộc gọi chặn và sẽ chỉ trở lại trên một
                    //  kết nối thành công hoặc ngoại lệ
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // Nếu một kết nối đã được chấp nhận
                if (socket != null) {
                    synchronized (this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }
//        phương thức dùng để hủy kết nối của máy chủ, đóng BluetoothServerSocket.
        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * Chuỗi này chạy trong khi cố tạo kết nối gửi đi với một thiết bị.
     * Nó chạy thẳng qua kết nối thành công hoặc thất bại.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Nhận BluetoothSocket để kết nối với thiết bị Bluetooth nhất định
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Luôn hủy khám phá vì nó sẽ làm chậm kết nối
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // Đây là một cuộc gọi chặn và sẽ chỉ trở lại khi kết nối thành công hoặc một ngoại lệ
                mmSocket.connect();
            } catch (IOException e) {
                // Đóng ổ cắm
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Đặt lại ConnectThread vì chúng tôi đã hoàn tất
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * Chủ đề này chạy trong khi kết nối với một thiết bị từ xa. Nó xử lý tất cả các truyền đến và đi.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Tiếp tục nghe InputStream khi được kết nối
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    //Hướng dẫn các byte thu được cho Hoạt động giao diện người dùng
                    xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.Doc_TB, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Ghi vào OutStream được kết nối.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                Log.d("TAG", "sendMessage: sent3");
                // Chia sẻ tin nhắn đã gửi trở lại Hoạt động giao diện người dùng
                xu_ly_thongdiep_duoc_gui_tu_BLChatService.obtainMessage(Constants.Viet_TB, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
