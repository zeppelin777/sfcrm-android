package com.android.zr.activity

import android.content.*
import android.os.*
import android.provider.CallLog
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.android.zr.databinding.ActivityMainBinding
import com.android.zr.receiver.WebSocketReceiver
import com.android.zr.service.WebSocketService
import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil
import okhttp3.internal.wait

class MainActivity : BaseActivity() {

    private var webSocketReceiver: WebSocketReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, WebSocketService::class.java)
        startService(intent)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)

        registerWebSocket()

        val manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val listener = object : PhoneStateListener() {
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
                            Handler(Looper.getMainLooper()).postDelayed({
                                val callTime = getCallHistory(phoneNumber)
                                val msg = Message.obtain()
                                msg.what = Constants.WHAT_CALL_TIME
                                val bundle = Bundle()
                                bundle.putLong("time", callTime)
                                msg.data = bundle
                                remoteService?.send(msg)
                            }, 3000)

                        }

                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        LogUtil.d("%s", "out 通话中 $phoneNumber")
                    }
                }
            }
        }

        manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            manager.registerTelephonyCallback(mainExecutor, object : TelephonyCallback() {
//
//
//            })
//        }


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
        webSocketReceiver = WebSocketReceiver()
        registerReceiver(webSocketReceiver, intentFilter)
    }


    private fun getCallHistory(phoneNum: String?): Long {
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
                val durationColumn = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val long = it.getLong(durationColumn)
                LogUtil.d("%s", "查询到的时间 = $long")
                return long
            }
        }
        return 0
    }


    override fun onDestroy() {
        super.onDestroy()
        if (webSocketReceiver != null) {
            unregisterReceiver(webSocketReceiver)
        }
        if (connected) {
            unbindService(conn)
        }
    }

}