package com.android.zr.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.android.zr.base.UrlUtils
import com.android.zr.bean.BaseBean
import com.android.zr.bean.LoginBean
import com.android.zr.bean.LoginParams
import com.android.zr.databinding.ActivityLoginBinding
import com.android.zr.net.HttpRequest
import com.android.zr.net.NetResponseCallBack
import com.android.zr.utils.LogUtil
import com.android.zr.utils.SpUtils
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


        binding.btLogin.setOnClickListener {
            showLoading()
            val params = LoginParams().apply {
                username = "15836776815"
                password = "Q97eV5n4Whmt"
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
                                        SpUtils.saveString("token", data.adminToken)
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                }

                        }
                    }
                })
        }

    }


}