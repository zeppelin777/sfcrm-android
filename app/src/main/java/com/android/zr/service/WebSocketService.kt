package com.android.zr.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.app.NotificationCompat
import com.android.zr.activity.MainActivity
import com.android.zr.net.HttpRequest
import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil
import okhttp3.WebSocket
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Created by matthew on 2023/03/08
 */
class WebSocketService : Service() {


    override fun onCreate() {
        super.onCreate()

        setupNotification()
        setupSocket()
    }

    private fun setupNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_WEBSOCKET_ID)
            .setTicker("zhongrong")
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)
            .setContentTitle("zhongrong")
            .setContentText("zhognrong")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        startForeground(1, builder.build())
    }

    private var socket: WebSocket? = null

    private fun setupSocket() {
        val client = HttpRequest.getInstance().httpClient
        val newClient = client.newBuilder().pingInterval(10, TimeUnit.SECONDS).build()
//        val request = Request.Builder().url("").build()
//        socket = newClient.newWebSocket(request, object : WebSocketListener() {
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                super.onMessage(webSocket, text)
//
//
//            }
//        })

        val intent = Intent()
        intent.action = Constants.RECEIVER_ACTION
        intent.putExtra("phone", "10086")
        sendBroadcast(intent)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return messenger.binder
    }


    class ServiceHandler(webSocketService: WebSocketService) : Handler(Looper.getMainLooper()) {

        private val service = WeakReference(webSocketService)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.WHAT_CALL_TIME -> {
                    val bundle = msg.data
                    val time = bundle.getLong("time")
                    LogUtil.d("%s", "service收到并发送websocket time = $time")
                    service.get()?.socket?.send(time.toString())
                }
            }
        }
    }

    private val messenger = Messenger(ServiceHandler(this))



}