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
import com.android.zr.base.UrlUtils
import com.android.zr.bean.SocketBean
import com.android.zr.net.HttpRequest
import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil
import com.android.zr.utils.ToastUtils
import com.google.gson.Gson
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Created by matthew on 2023/03/08
 */
class WebSocketService : Service() {


    override fun onCreate() {
        super.onCreate()

        setupNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra("user_id")
        LogUtil.d("%s", "user id = $userId")
        setupSocket(userId)
        return super.onStartCommand(intent, flags, startId)
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

    private fun setupSocket(userId: String?) {
        val client = HttpRequest.getInstance().httpClient
        val newClient = client.newBuilder().pingInterval(10, TimeUnit.SECONDS).build()
        val request = Request.Builder().url("${UrlUtils.WebSocketUrl}/$userId").build()
        socket = newClient.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                LogUtil.d("%s", "response = $response")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)

                LogUtil.d("%s", "text = $text")

                val gson = Gson()
                val bean = gson.fromJson(text, SocketBean::class.java)
                val intent = Intent()
                intent.action = Constants.RECEIVER_ACTION
                intent.putExtra("action", bean)
                sendBroadcast(intent)
            }


        })


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