package com.android.zr2.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.android.zr2.utils.Constants

/**
 * Created by matthew on 2023/03/07
 */
class BaseApp : Application() {

    companion object {
        private var instance: BaseApp? = null

        fun getInstance(): Context {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.NOTIFICATION_WEBSOCKET_ID, "socket", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


}