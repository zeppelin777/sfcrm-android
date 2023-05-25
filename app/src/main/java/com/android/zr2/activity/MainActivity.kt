package com.android.zr2.activity

import android.app.ActivityManager
import android.content.*
import android.os.*
import android.view.View
import com.android.zr2.R
import com.android.zr2.base.UrlUtils
import com.android.zr2.bean.EmptyBean
import com.android.zr2.bean.SocketBean
import com.android.zr2.databinding.ActivityMainBinding
import com.android.zr2.net.HttpRequest
import com.android.zr2.net.NetResponseCallBack
import com.android.zr2.service.WebSocketService
import com.android.zr2.utils.*
import java.lang.ref.WeakReference

class MainActivity : BaseActivity(), View.OnClickListener {

    private var webSocketReceiver: WebSocketReceiver? = null
    private var serviceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvUsername.text = SpUtils.getString(Constants.USER_NAME)
        binding.btnLogout.setOnClickListener(this)

        registerWebSocket()

        val showLoading = intent.getBooleanExtra("show_loading", false)
        if (showLoading) {
            showLoading()
        }

        if (!isServiceRunning()) {
            val userId = intent.getStringExtra("user_id")
            serviceIntent = Intent(this, WebSocketService::class.java)
            serviceIntent!!.putExtra("user_id", userId)
            startService(serviceIntent)
        }

    }

    private fun isServiceRunning(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningServices(10)
        if (list.size <= 0) {
            return false
        }
        for (info in list) {
            if (info.service.className == "com.android.zr.service.WebSocketService") {
                serviceIntent = Intent()
                serviceIntent!!.component = info.service
                return true
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_logout -> {
                showLoading()
                HttpRequest.getInstance()
                    .delete(UrlUtils.LogoutUrl, this, object : NetResponseCallBack<EmptyBean>(this) {
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
//        SpUtils.saveString(Constants.USER_NAME, "")
//        SpUtils.saveString(Constants.PWD, "")
        if (serviceIntent != null) {
            stopService(serviceIntent)
        }
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }


    private fun registerWebSocket() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.RECEIVER_ACTION)
        webSocketReceiver = WebSocketReceiver(this)
        registerReceiver(webSocketReceiver, intentFilter)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (webSocketReceiver != null) {
            unregisterReceiver(webSocketReceiver)
        }
    }


    class WebSocketReceiver(context: Context) : BroadcastReceiver() {

        private val contextRef = WeakReference(context)

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != null && intent.action.equals(Constants.RECEIVER_ACTION)) {
                val activity = contextRef.get() as MainActivity
                when (intent.getIntExtra("type", 2)) {
                    Constants.ACTION_PHONE -> {
                        val bean = intent.getParcelableExtra<SocketBean>("action")!!

                        when (bean.action) {
                            "connect" -> {
                                ToastUtils.showToast(bean.message)
                            }
                            "message" -> {
                                activity.showTips(bean.message)
                            }
//                    "phone" -> {
//                        HttpRequest.getInstance().post(UrlUtils.CheckTokenUrl, null, activity, object : NetResponseCallBack<EmptyBean>(activity) {
//                            override fun onSuccessObject(data: EmptyBean?, id: Int) {
//                                super.onSuccessObject(data, id)
//                                activity.model = bean.model
////                              val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:10086"))
//                                val phoneIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${bean.message}"))
//                                activity.startActivity(phoneIntent)
//                            }
//
//                            override fun onFail(code: Int, msg: String?, id: Int) {
//                                super.onFail(code, msg, id)
//                                activity.logout()
//                            }
//
//                        })
//
//                    }
                        }
                    }
                    Constants.ACTION_SHOW_LOADING -> {
                        activity.showLoading()
                    }
                    Constants.ACTION_HIDE_LOADING -> {
                        activity.hideLoading()
                    }
                }

            }
        }

    }

}