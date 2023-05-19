package com.android.zr2.net

import com.android.zr2.utils.Constants
import com.android.zr2.utils.LogUtil
import com.android.zr2.utils.SpUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by matthew on 2023/03/08
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val request = oldRequest.newBuilder().addHeader("Authorization", "Bearer ${SpUtils.getString(Constants.TOKEN)}").build()
        return chain.proceed(request)
    }

}