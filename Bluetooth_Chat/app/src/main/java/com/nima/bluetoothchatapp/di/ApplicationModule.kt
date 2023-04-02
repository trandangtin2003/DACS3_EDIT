package com.nima.bluetoothchatapp.di

import android.content.Context
import android.content.SharedPreferences
import com.nima.bluetoothchatapp.Constants.Companion.SHARED_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@InstallIn" được sử dụng để xác định phạm vi của module
//SingletonComponent là một phạm vi đại diện cho toàn bộ ứng dụng và
// được giữ trạng thái duy nhất trong suốt vòng đời của ứng dụng./
@InstallIn(SingletonComponent::class)
@Module
object ApplicationModule {
//    "@Singleton" được sử dụng để chỉ định rằng đối tượng "SharedPreferences" được cung cấp là duy nhất trong toàn bộ ứng dụng.
    @Singleton
//    @Provides" để cung cấp đối tượng "SharedPreferences" cho việc lưu trữ và quản lý dữ liệu trên thiết bị.
    @Provides
//Tham số "@ApplicationContext" của phương thức "provideSharedPreference"
// là một Annotation đánh dấu tham số kiểu "Context" để Hilt có thể cung cấp đúng kiểu Context được sử dụng trong ứng dụng.

//Khi có yêu cầu cung cấp đối tượng "SharedPreferences" trong ứng dụng, Hilt sẽ sử dụng module "ApplicationModule"
// để cung cấp một đối tượng "SharedPreferences" với tính năng Singleton và tham số được cung cấp bởi "@ApplicationContext"
// của phương thức "provideSharedPreference".
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
}