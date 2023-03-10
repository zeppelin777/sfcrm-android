package com.android.zr.activity

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import com.android.zr.base.UrlUtils
import com.android.zr.bean.BaseBean
import com.android.zr.bean.LoginBean
import com.android.zr.bean.LoginParams
import com.android.zr.databinding.ActivityLoginBinding
import com.android.zr.net.HttpRequest
import com.android.zr.net.NetResponseCallBack
import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil
import com.android.zr.utils.SpUtils
import com.android.zr.utils.ToastUtils
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import java.lang.reflect.Type

/**
 * Created by matthew on 2023/03/08
 */
class LoginActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inputPwd.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE


//        if (!checkOverlay()) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        }

//        Intent().apply {
//            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//            data = Uri.parse("package:$packageName")
//            startActivity(this)
//        }

        binding.btLogin.setOnClickListener {
            showLoading()
            val params = LoginParams().apply {
                username = binding.etUsername.text.toString()
                password = binding.etPassword.text.toString()
            }

            HttpRequest.getInstance().postJson(
                UrlUtils.LoginUrl, Gson().toJson(params),
                this, object : NetResponseCallBack<LoginBean>(this) {

                    override fun getType(): Type {
                        return object : TypeToken<BaseBean<LoginBean>>() {}.type
                    }

                    override fun onSuccessObject(data: LoginBean, id: Int) {
                        super.onSuccessObject(data, id)
                        LogUtil.d("%s", "token = ${data.adminToken}")

                        if (!TextUtils.isEmpty(data.adminToken)) {
                            PermissionX.init(this@LoginActivity)
                                .permissions(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE)
                                .onExplainRequestReason{scope, deniedList ->
                                    scope.showRequestReasonDialog(deniedList, "您已经拒绝过我们的申请授权，请您同意授权，否则功能无法正常使用！", "确定", "取消")
                                }
                                .onForwardToSettings {scope, deniedList ->
                                    scope.showForwardToSettingsDialog(deniedList, "我们需要的一些权限被您拒绝或者系统发生错误导致授权失败，请您到设置页面手动授权，否则功能无法正常使用！", "确定", "取消")
                                }
                                .request { allGranted, _, _ ->
                                    if (allGranted) {
                                        SpUtils.saveString(Constants.TOKEN, data.adminToken)
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        intent.putExtra("user_id", data.userId)
                                        startActivity(intent)
                                        finish()
                                    }
                                }

                        }
                    }
                })
        }

    }

//    private fun checkOverlay(): Boolean {
//        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//        val pm = packageManager
//        val ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
//        val mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, ai.uid, packageName)
//        LogUtil.d("%s", "mode = $mode")
//        return mode == AppOpsManager.MODE_ALLOWED
//    }

}