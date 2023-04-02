package com.nima.bluetoothchatapp.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
/*Lớp "BaseApplication" kế thừa lớp "Application",
đây là lớp cơ sở cho mọi ứng dụng Android,
đại diện cho một khung của toàn bộ ứng dụng và chạy trước các hoạt động khác trong ứng dụng.

Việc kế thừa lớp "Application" giúp "BaseApplication" có thể ghi đè và cài đặt lại các phương thức của "Application"
, như onCreate() để cấu hình và khởi tạo các thư viện, đặc biệt là Hilt.*/
@HiltAndroidApp
class BaseApplication :Application()