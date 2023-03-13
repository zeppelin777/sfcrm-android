package com.android.zr.net

import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil
import com.android.zr.utils.SpUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by matthew on 2023/03/08
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val request = oldRequest.newBuilder().addHeader("Admin-Token", SpUtils.getString(Constants.TOKEN)).build()
        LogUtil.d("%s", "添加token到header")
        return chain.proceed(request)
    }

}