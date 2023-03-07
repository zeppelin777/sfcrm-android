package com.android.zr.activity

import android.os.Bundle
import android.util.Log
import com.android.zr.base.UrlUtils
import com.android.zr.bean.BaseBean
import com.android.zr.bean.LoginBean
import com.android.zr.bean.LoginParams
import com.android.zr.databinding.ActivityMainBinding
import com.android.zr.net.NetResponseCallBack
import com.android.zr.utils.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bt.setOnClickListener {
            showLoading()
            val params = LoginParams().apply {
                username = ""
                password = ""
            }

            HttpRequest.getInstance().postJson(UrlUtils.LoginUrl, Gson().toJson(params),
                this, object : NetResponseCallBack<LoginBean>(this) {

                    override fun getType(): Type {
                        return object : TypeToken<BaseBean<LoginBean>>(){}.type
                    }

                    override fun onSuccessObject(data: LoginBean, id: Int) {
                        super.onSuccessObject(data, id)
                        Log.d("dynadot", "token = ${data.adminToken}")

                    }
                })
        }
    }


}