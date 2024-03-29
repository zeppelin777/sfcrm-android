package com.android.shufeng.activity

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import com.android.shufeng.R
import com.android.shufeng.base.UrlUtils
import com.android.shufeng.bean.BaseBean
import com.android.shufeng.bean.LoginBean
import com.android.shufeng.bean.LoginParams
import com.android.shufeng.databinding.ActivityLoginBinding
import com.android.shufeng.net.HttpRequest
import com.android.shufeng.net.NetResponseCallBack
import com.android.shufeng.utils.Constants
import com.android.shufeng.utils.LogUtil
import com.android.shufeng.utils.SpUtils
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX
import java.lang.reflect.Type

/**
 * Created by matthew on 2023/03/08
 */
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.inputPwd.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        val username = SpUtils.getString(Constants.USER_NAME)
        val pwd = SpUtils.getString(Constants.PWD)
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            binding.etUsername.setText(username)
            binding.etPassword.setText(pwd)
        }


        if (!TextUtils.isEmpty(SpUtils.getString(Constants.TOKEN)) &&
            !TextUtils.isEmpty(SpUtils.getString(Constants.USER_ID))
        ) {
            jumpToMain(SpUtils.getString(Constants.USER_ID))
        } else {
            initViews()
            if (!checkOverlay()) {
                showPermissionDialog()
            }
        }

        checkSdCardPermission()

    }

    private fun showPermissionDialog() {
        val tipsDialog = AlertDialog.Builder(this)
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:$packageName")
                    startActivity(this)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .create()
        tipsDialog.setMessage(getString(R.string.permission_desc))
        tipsDialog.show()
    }

    private fun initViews() {
        binding.btLogin.setOnClickListener {

            val name = binding.etUsername.text.toString()
            val pwd = binding.etPassword.text.toString()
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                return@setOnClickListener
            }

            showLoading()
            val params = LoginParams().apply {
                username = name
                password = pwd
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
                                .permissions(
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.CALL_PHONE
                                )
                                .onExplainRequestReason { scope, deniedList ->
                                    scope.showRequestReasonDialog(
                                        deniedList,
                                        "您已经拒绝过我们的申请授权，请您同意授权，否则功能无法正常使用！",
                                        "确定",
                                        "取消"
                                    )
                                }
                                .onForwardToSettings { scope, deniedList ->
                                    scope.showForwardToSettingsDialog(
                                        deniedList,
                                        "我们需要的一些权限被您拒绝或者系统发生错误导致授权失败，请您到设置页面手动授权，否则功能无法正常使用！",
                                        "确定",
                                        "取消"
                                    )
                                }
                                .request { allGranted, _, _ ->
                                    if (allGranted) {
                                        SpUtils.saveString(Constants.TOKEN, data.adminToken)
                                        SpUtils.saveString(Constants.USER_ID, data.userId)
                                        SpUtils.saveString(Constants.USER_NAME, binding.etUsername.text.toString())
                                        SpUtils.saveString(Constants.PWD, binding.etPassword.text.toString())
                                        jumpToMain(data.userId!!)
                                    }
                                }

                        }
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkSdCardPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setMessage("我们需要访问sd卡的权限，请授权，否则功能无法正常使用")
                    .setPositiveButton("确认") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    }.show()
            }
        } else {
            PermissionX.init(this@LoginActivity)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "您已经拒绝过我们的申请授权，请您同意授权，否则功能无法正常使用！",
                        "确定",
                        "取消"
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "我们需要的一些权限被您拒绝或者系统发生错误导致授权失败，请您到设置页面手动授权，否则功能无法正常使用！",
                        "确定",
                        "取消"
                    )
                }
                .request { allGranted, _, _ ->

                }
        }
    }


    private fun jumpToMain(userId: String) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
        finish()
    }

    private fun checkOverlay(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val pm = packageManager
        val ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            ai.uid,
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

}