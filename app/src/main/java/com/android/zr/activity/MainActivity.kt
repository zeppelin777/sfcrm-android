package com.android.zr.activity

import android.content.*
import android.net.Uri
import android.os.*
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.View
import com.android.zr.R
import com.android.zr.base.UrlUtils
import com.android.zr.bean.CallLogBean
import com.android.zr.bean.CallTimeParams
import com.android.zr.bean.EmptyBean
import com.android.zr.bean.SocketBean
import com.android.zr.databinding.ActivityMainBinding
import com.android.zr.net.HttpRequest
import com.android.zr.net.NetResponseCallBack
import com.android.zr.service.WebSocketService
import com.android.zr.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.lang.reflect.Type

class MainActivity : BaseActivity(), View.OnClickListener {

    private var webSocketReceiver: WebSocketReceiver? = null
    private lateinit var manager: TelephonyManager

    private var serviceIntent: Intent? = null

    private var userId: String? = null
    private var model: String? = null
    private var callType: Int = Constants.CALL_TYPE_NO_CALL //0未拨打，1拨打未接通，2接通，3呼入未接通


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvUsername.text = SpUtils.getString(Constants.USER_NAME)
        binding.btnLogout.setOnClickListener(this)

        userId = intent.getStringExtra("user_id")

        serviceIntent = Intent(this, WebSocketService::class.java)
        serviceIntent!!.putExtra("user_id", userId)
        startService(serviceIntent)
        bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE)

        registerWebSocket()

        manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        manager.listen(stateListener, PhoneStateListener.LISTEN_CALL_STATE)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            manager.registerTelephonyCallback(mainExecutor, object : TelephonyCallback(), TelephonyCallback.CallStateListener {
//                override fun onCallStateChanged(state: Int) {
//
//                }
//            })
//        }

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

                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        startActivity(intent)

                        showLoading()
                        Handler(Looper.getMainLooper()).postDelayed({
                            val callLogBean = getCallHistory(phoneNumber)
                            hideLoading()
                            if (callLogBean != null) {
                                sendCallTime(callLogBean, currentTimeMillis)

                                val msg = Message.obtain()
                                msg.what = Constants.WHAT_CALL_TIME
                                val bundle = Bundle()
                                bundle.putLong("time", callLogBean.duration)
                                msg.data = bundle
                                remoteService?.send(msg)
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_logout -> {
                showLoading()
                HttpRequest.getInstance()
                    .post(UrlUtils.LogoutUrl, null,this, object : NetResponseCallBack<EmptyBean>(this) {
                        override fun onSuccessObject(data: EmptyBean?, id: Int) {
                            super.onSuccessObject(data, id)
                            ToastUtils.showToast("已退出")
                            logout()
                        }

                    })
            }
        }
    }

    private fun logout() {
        SpUtils.saveString(Constants.TOKEN, "")
        SpUtils.saveString(Constants.USER_ID, "")
        SpUtils.saveString(Constants.USER_NAME, "")
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }


    private var remoteService: Messenger? = null
    private var connected = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            remoteService = Messenger(service)
            connected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connected = false
        }

    }


    private fun registerWebSocket() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.RECEIVER_ACTION)
        webSocketReceiver = WebSocketReceiver(this)
        registerReceiver(webSocketReceiver, intentFilter)
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


    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private fun releaseResources() {
        if (webSocketReceiver != null) {
            unregisterReceiver(webSocketReceiver)
        }
        if (connected) {
            unbindService(conn)
        }
        if (serviceIntent != null) {
            stopService(serviceIntent)
        }
        manager.listen(stateListener, PhoneStateListener.LISTEN_NONE)
    }


    private fun sendCallTime(callLogBean: CallLogBean, hangUpTime: Long) {
        showLoading()
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


            state = if (callLogBean.duration > 0) {
                Constants.CALL_TYPE_CALL_HAS_ANSWER
            } else {
                Constants.CALL_TYPE_CALL_NO_ANSWER
            }
            model = this@MainActivity.model

            LogUtil.d("%s", "开始拨打的时间： ${callLogBean.date} , 接通的时间：$answerTimeLong , 结束的时间：$hangUpTime , 拨打到接通的时间：$dialTimeLong")
            LogUtil.d("%s", "开始拨打的时间： ${DateUtils.getFormatDateTime("HH:mm:ss", callLogBean.date)} , 接通的时间：${DateUtils.getFormatDateTime("HH:mm:ss", answerTimeLong)} , 结束的时间：${DateUtils.getFormatDateTime("HH:mm:ss", hangUpTime)}")
        }
        LogUtil.d("%s", Gson().toJson(params))
        HttpRequest.getInstance().postJson(UrlUtils.SendCallTimeUrl, Gson().toJson(params), this, object : NetResponseCallBack<EmptyBean>(this) {
            override fun onSuccessObject(data: EmptyBean?, id: Int) {
                super.onSuccessObject(data, id)
                ToastUtils.showToast("保存成功")
                resetValues()
            }

            override fun onFail(responseCode: Int, msg: String?, id: Int) {
                super.onFail(responseCode, msg, id)
                resetValues()
                if (responseCode == 302) {
                    logout()
                }
                // {"code":302,"msg":"请先登录！","data":{"extra":1,"extraTime":"2023-03-15 16:03:57"}}
            }

            override fun onError(id: Int) {
                super.onError(id)
                resetValues()
            }
        })
    }

    private fun resetValues() {
        model = ""
        callType = Constants.CALL_TYPE_NO_CALL
    }


    class WebSocketReceiver(context: Context) : BroadcastReceiver() {

        private val contextRef = WeakReference(context)

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action.equals(Constants.RECEIVER_ACTION)) {

                val bean = intent.getParcelableExtra<SocketBean>("action")!!

                val activity = contextRef.get() as MainActivity
                when (bean.action) {
                    "connect" -> {
                        ToastUtils.showToast(bean.message)
                    }
                    "message" -> {
                        activity.showTips(bean.message)
                    }
                    "phone" -> {
                        HttpRequest.getInstance().post(UrlUtils.CheckTokenUrl, null, activity, object : NetResponseCallBack<EmptyBean>(activity) {
                            override fun onSuccessObject(data: EmptyBean?, id: Int) {
                                super.onSuccessObject(data, id)
                                activity.model = bean.model
//                              val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:10086"))
                                val phoneIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${bean.message}"))
                                activity.startActivity(phoneIntent)
                            }

                            override fun onFail(code: Int, msg: String?, id: Int) {
                                super.onFail(code, msg, id)
                                activity.logout()
                            }

                        })

                    }
                }

            }
        }

    }

}