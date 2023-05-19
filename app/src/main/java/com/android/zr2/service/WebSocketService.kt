package com.android.zr2.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.android.zr2.R
import com.android.zr2.activity.LoginActivity
import com.android.zr2.activity.MainActivity
import com.android.zr2.base.UrlUtils
import com.android.zr2.bean.*
import com.android.zr2.bean2.NewSocketBean
import com.android.zr2.net.HttpRequest
import com.android.zr2.net.NetResponseCallBack
import com.android.zr2.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhy.http.okhttp.OkHttpUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


/**
 * Created by matthew on 2023/03/08
 */
class WebSocketService : Service() {

    private lateinit var manager: TelephonyManager

    private var callType: Int = Constants.CALL_TYPE_NO_CALL //0未拨打，1拨打未接通，2接通，3呼入未接通

    private var connected = false
    private var model: String? = null

    private var userId: String? = null

    override fun onCreate() {
        super.onCreate()

        setupNotification()
        setupListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userId = intent?.getStringExtra("user_id")
        LogUtil.d("%s", "user id = $userId")
        setupSocket()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setupListener() {
        manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        manager.listen(stateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private val stateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            LogUtil.d("%s", "phone num = +++$phoneNumber---------")
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    LogUtil.d("%s", "out 响铃 $phoneNumber")
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    LogUtil.d("%s", "out 闲置 $phoneNumber")

                    if (connected) {
                        val currentTimeMillis = System.currentTimeMillis()
                        val time = DateUtils.getFormatDateTime("yyyy/MM/dd HH:mm:ss", currentTimeMillis)
                        LogUtil.d("%s", "挂断时间 = $time = $currentTimeMillis")

                        Intent(this@WebSocketService, MainActivity::class.java).apply {
                            putExtra("show_loading", false)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(this)
                        }

//                        showLoading()
                        Handler(Looper.getMainLooper()).postDelayed({
                            val callLogBean = getCallHistory(phoneNumber)
                            hideLoading()
                            if (callLogBean != null) {
                                if (!TextUtils.isEmpty(callLogBean.phoneNumber)) {
                                    sendCallTime(callLogBean, currentTimeMillis)
                                }
                            }
                        }, 3000)
                    }
                    callType = Constants.CALL_TYPE_NO_CALL

                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    callType = Constants.CALL_TYPE_CALL_NO_ANSWER
                    LogUtil.d("%s", "out 通话中 $phoneNumber")
                }
            }
        }
    }

    private fun showLoading() {
        Intent().apply {
            action = Constants.RECEIVER_ACTION
            putExtra("type", Constants.ACTION_SHOW_LOADING)
            sendBroadcast(this)
        }
    }

    private fun hideLoading() {
        Intent().apply {
            action = Constants.RECEIVER_ACTION
            putExtra("type", Constants.ACTION_HIDE_LOADING)
            sendBroadcast(this)
        }
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
            .setTicker(getString(R.string.app_name))
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        startForeground(1, builder.build())
    }

    private var socket: WebSocket? = null
    private var newClient: OkHttpClient? = null
    private var request: Request? = null

    private fun setupSocket() {
        val client = HttpRequest.getInstance().httpClient
        newClient = client.newBuilder().pingInterval(10, TimeUnit.SECONDS).build()
        request = Request.Builder().url("${UrlUtils.WebSocketUrl}/$userId").build()
        LogUtil.d("%s", "${UrlUtils.WebSocketUrl}/$userId")
        connect()
    }

    private fun connect() {
        socket = newClient!!.newWebSocket(request!!, socketListener)
        serviceHandler.removeCallbacksAndMessages(null)
        serviceHandler.sendEmptyMessageDelayed(Constants.WHAT_SEND_DELAY_MSG, 300000) //5 min
    }

    private var repeatCount = 0

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            connected = true
            LogUtil.d("%s", "response = $response")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            connected = false
            if (repeatCount < 3) {
                repeatCount++
                connect()
            } else {
                logout()
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            connected = false
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)

            LogUtil.d("%s", "text = $text")

//            {"code":0,"msg":"成功","data":"13901012345","actioncode":""}

            // TODO:  跳转电话app界面
            val g = Gson()
            val newSocketBean = g.fromJson(text, NewSocketBean::class.java)
            val phoneIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${newSocketBean.data}"))
            phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(phoneIntent)


            val gson = Gson()
            val bean = gson.fromJson(text, SocketBean::class.java)
            Intent().apply {
                action = Constants.RECEIVER_ACTION
                putExtra("type", Constants.ACTION_PHONE)
                putExtra("action", bean)
                sendBroadcast(this)
            }


            if ("sendPhone" == bean.action) {
                HttpRequest.getInstance().post(UrlUtils.CheckTokenUrl, null, null,
                    object : NetResponseCallBack<EmptyBean>(this@WebSocketService) {

                        override fun onSuccessObject(data: EmptyBean?, id: Int) {
                            super.onSuccessObject(data, id)

                            this@WebSocketService.model = bean.model
//                            val phoneIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:10086"))
                            val phoneIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${bean.message}"))
                            phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(phoneIntent)

                        }

                        override fun onFail(code: Int, msg: String?, id: Int) {
                            super.onFail(code, msg, id)
                            logout()
                        }
                    })
            }
        }
    }

    private fun getCallHistory(phoneNum: String?): CallLogBean? {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            ),
            "${CallLog.Calls.NUMBER} == ?", arrayOf(phoneNum),
            CallLog.Calls.DEFAULT_SORT_ORDER
        )
        cursor?.use {
            while (it.moveToNext()) {
//                val cachedNameColumn = it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
//                val cachedName = it.getString(cachedNameColumn)

                val numberColumn = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val phoneNumber = it.getString(numberColumn)

                val typeColumn = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val type = it.getInt(typeColumn)

                val dateColumn = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val date = it.getLong(dateColumn)
                val dateString = DateUtils.getFormatDateTime("yyyy/MM/dd HH:mm:ss", date)

                val durationColumn = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val duration = it.getLong(durationColumn)

                //                LogUtil.d("%s", "查询到的cached name = $cachedName")
                LogUtil.d("%s", "查询到的phone number = $phoneNumber")
                LogUtil.d("%s", "查询到的type = $type")
                LogUtil.d("%s", "查询到的date = $date") //开始响铃的时间
                LogUtil.d("%s", "查询到的date string = $dateString")
                LogUtil.d("%s", "查询到的时间 = $duration")

                return CallLogBean(phoneNumber, type, date, duration)
            }
        }
        return null
    }

    private fun sendCallTime(callLogBean: CallLogBean, hangUpTime: Long) {
//        showLoading()
        val params = CallTimeParams().apply {
            createUserId = userId
            talkTime = callLogBean.duration
            number = callLogBean.phoneNumber

            startTime = callLogBean.date

            val answerTimeLong = hangUpTime - callLogBean.duration* 1000
            answerTime = answerTimeLong

            endTime = hangUpTime

            val dialTimeLong = answerTimeLong - callLogBean.date
            dialTime = dialTimeLong

            type = if (callLogBean.type == CallLog.Calls.OUTGOING_TYPE) 0 else 1 //0呼出，1呼入

            state = if (callLogBean.duration > 0) {
                Constants.CALL_TYPE_CALL_HAS_ANSWER
            } else {
                Constants.CALL_TYPE_CALL_NO_ANSWER
            }
            model = this@WebSocketService.model

            LogUtil.d("%s", "开始拨打的时间： ${callLogBean.date} , 接通的时间：$answerTimeLong , 结束的时间：$hangUpTime , 拨打到接通的时间：$dialTimeLong")
            LogUtil.d("%s", "开始拨打的时间： ${DateUtils.getFormatDateTime("HH:mm:ss", callLogBean.date)} , 接通的时间：${DateUtils.getFormatDateTime("HH:mm:ss", answerTimeLong)} , 结束的时间：${DateUtils.getFormatDateTime("HH:mm:ss", hangUpTime)}")

        }
        LogUtil.d("%s", Gson().toJson(params))
        LogUtil.d("%s", UrlUtils.SendCallTimeUrl)

        HttpRequest.getInstance().postJson(UrlUtils.SendCallTimeUrl, Gson().toJson(params), this, object : NetResponseCallBack<SaveRecordBean>(this) {
            override fun onSuccessObject(data: SaveRecordBean?, id: Int) {
                super.onSuccessObject(data, id)
                ToastUtils.showToast("通话记录保存成功")
                resetValues()
                hideLoading()
                uploadRecord(data!!.callRecordId, callLogBean.phoneNumber, params.answerTime)
            }

            override fun onFail(responseCode: Int, msg: String?, id: Int) {
                super.onFail(responseCode, msg, id)
                resetValues()
                hideLoading()

                if (responseCode == 302) {
                    logout()
                }
                // {"code":302,"msg":"请先登录！","data":{"extra":1,"extraTime":"2023-03-15 16:03:57"}}
            }

            override fun onError(id: Int) {
                super.onError(id)
                resetValues()
                hideLoading()
            }

            override fun getType(): Type {
                return object : TypeToken<BaseBean<SaveRecordBean>>() {}.type
            }
        })
    }

    private fun uploadRecord(id: Long, phoneNum: String?, answerTime: Long) {
//        showLoading()
        val file = getRecordFile(phoneNum, answerTime)
        if (file != null) {
            LogUtil.d("%s", "找到的file文件 = ${file.absolutePath}")

            val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            val requestBody: RequestBody = file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
            builder.addFormDataPart("file", file.name, requestBody)

            OkHttpUtils.put().url("${UrlUtils.UploadFileUrl}/66")
                .requestBody(builder.build())
                .build()
                .execute(object : NetResponseCallBack<EmptyBean>(this) {
                    override fun onSuccessObject(data: EmptyBean?, id: Int) {
                        super.onSuccessObject(data, id)
                    }

                    override fun onFail(code: Int, msg: String?, id: Int) {
                        super.onFail(code, msg, id)
                    }

                    override fun onError(id: Int) {
                        super.onError(id)
                    }
                })

//            HttpRequest.getInstance().upload("${UrlUtils.UploadRecordUrl}?id=$id", "file", null, file, this,
//                object : NetResponseCallBack<EmptyBean>(this) {
//                    override fun onSuccessObject(data: EmptyBean?, id: Int) {
//                        super.onSuccessObject(data, id)
//                        hideLoading()
//                        ToastUtils.showToast("录音保存成功")
//                    }
//
//                    override fun onFail(code: Int, msg: String?, id: Int) {
//                        super.onFail(code, msg, id)
//                        hideLoading()
//                    }
//
//                    override fun onError(id: Int) {
//                        super.onError(id)
//                        hideLoading()
//                    }
//                })
        } else {
            hideLoading()
            ToastUtils.showToast("错误，没有找到录音文件")
        }
    }

    private fun getRecordFile(phoneNum: String?, answerTime: Long): File? {
        val file = File("sdcard/MIUI/sound_recorder/call_rec")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            if (files != null && files.isNotEmpty()) {
                val answerFormatTime = DateUtils.getFormatDateTime("yyyyMMddHHmmss", answerTime)
                val answerLong = answerFormatTime.toLong()
                for (f in files) {
                    LogUtil.d("%s", f.absolutePath)
                    LogUtil.d("%s", f.length())
                    LogUtil.d("%s", "----------------")
                    val fileName = f.name  //10086(10086)_20230410131117.mp3

                    val split = fileName.split("_")
                    if (split.size > 1) {
                        val fileDate = split[1].split(".") //20230410131117.mp3
                        val fileDateString = fileDate[0] // 20230410131117

                        val year = fileDateString.substring(0, 4)
                        val month = fileDateString.substring(4, 6)
                        val day = fileDateString.substring(6, 8)
                        val hour = fileDateString.substring(8, 10)
                        val minute = fileDateString.substring(10, 12)
                        val seconds = fileDateString.substring(12, 14)
                        val calendar = Calendar.getInstance()
                        calendar.set(year.toInt(), month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt(), seconds.toInt())
                        val fileTimeMillis = calendar.timeInMillis

                        if (fileTimeMillis < answerTime - 14 * 24 * 3600 * 1000) {
                            f.delete()
                        }
                    }

                    if (fileName.contains(phoneNum!!)) {
                        val split2 = fileName.split("_")
                        if (split2.size > 1) {
                            val fileDate = split2[1].split(".") //20230410131117.mp3
                            val fileDateString = fileDate[0] // 20230410131117
                            val fileDateLong = fileDateString.toLong()
                            if (abs(answerLong - fileDateLong) <= 3) {
                                return f
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun resetValues() {
        model = ""
        callType = Constants.CALL_TYPE_NO_CALL
    }

    private fun logout() {
        SpUtils.saveString(Constants.TOKEN, "")
        SpUtils.saveString(Constants.USER_ID, "")

        val intent = Intent(this@WebSocketService, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        stopSelf()
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
                Constants.WHAT_SEND_DELAY_MSG -> {
                    service.get()?.socket?.send("1")
                    sendEmptyMessageDelayed(Constants.WHAT_SEND_DELAY_MSG, 300000) //5 min
                }
            }
        }
    }

    private val serviceHandler = ServiceHandler(this)
    private val messenger = Messenger(serviceHandler)

    override fun onDestroy() {
        super.onDestroy()
        socket?.close(1000, "用户退出")
        repeatCount = 0
        manager.listen(stateListener, PhoneStateListener.LISTEN_NONE)
        serviceHandler.removeCallbacksAndMessages(null)
    }

}