package com.nima.bluetoothchatapp.di

import android.content.Context
import androidx.room.Room
import com.nima.bluetoothchatapp.Constants.Companion.DATABASE_NAME
import com.nima.bluetoothchatapp.database.BCADatabase
import com.nima.bluetoothchatapp.database.MyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
//"@InstallIn" được sử dụng để xác định phạm vi của module,
// trong trường hợp này là SingletonComponent.
// SingletonComponent là một phạm vi đại diện cho toàn bộ ứng dụng
// và được giữ trạng thái duy nhất trong suốt vòng đời của ứng dụng.
@InstallIn(SingletonComponent::class)
//Annotation "@Module" được sử dụng để đánh dấu lớp "RoomModule" là một module Hilt.
@Module
object RoomModule {
//Cả hai phương thức được đánh dấu bởi Annotation "@Singleton"
// để chỉ định rằng các đối tượng được cung cấp là duy nhất trong toàn bộ ứng dụng.
    @Singleton
//     "RoomModule" có hai phương thức được đánh dấu bởi Annotation
//     "@Provides" để cung cấp đối tượng cơ sở dữ liệu Room và DAO
//     liên quan đến cơ sở dữ liệu đó.
    @Provides
//    Phương thức "provideDataBase" cung cấp một đối tượng BCADatabase,
//    sử dụng phương thức "databaseBuilder" của Room để xây dựng
//    đối tượng cơ sở dữ liệu với tên "DATABASE_NAME" và
//    các tùy chọn cho phép khôi phục dữ liệu mất mát trong quá trình migration.
    fun provideDataBase(@ApplicationContext context: Context): BCADatabase {
        return Room.databaseBuilder(
            context, BCADatabase::class.java, DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
//    Phương thức "provideDao" cung cấp một đối tượng MyDao,
//    sử dụng đối tượng BCADatabase được cung cấp bởi
//    phương thức "provideDataBase" để tạo ra một đối tượng MyDao
//    liên quan đến cơ sở dữ liệu đó.
    fun provideDao(dataBase: BCADatabase): MyDao {
        return dataBase.myDao()
    }
}