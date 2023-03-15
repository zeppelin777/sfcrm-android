package com.android.zr.base

/**
 * Created by matthew on 2023/03/07
 */
object UrlUtils {

//    private const val BaseUrl = "http://crzz.cc:18000"
    private const val BaseUrl = "https://zhongrong.work"
    const val LoginUrl = "$BaseUrl/login"
    const val LogoutUrl = "$BaseUrl/logout"
    const val SendCallTimeUrl = "$BaseUrl/crmCall/save"
    const val CheckTokenUrl = "$BaseUrl/adminUser/checkToken"

    const val WebSocketUrl = "ws://zhongrong.work/ws/android"
//    const val WebSocketUrl = "ws://crzz.cc:18000/ws/android"

}