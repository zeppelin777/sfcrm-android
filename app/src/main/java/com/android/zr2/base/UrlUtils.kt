package com.android.zr2.base

/**
 * Created by matthew on 2023/03/07
 */
object UrlUtils {

    private const val BaseUrl = "http://crzz.cc:18443"

//    private const val BaseUrl = "https://zhongrong.work"
//    const val WebSocketUrl = "wss://zhongrong.work/ws/android"

    const val CheckTokenUrl = "$BaseUrl/adminUser/checkToken"
//    const val LoginUrl = "$BaseUrl/login"
    const val LogoutUrl = "$BaseUrl/logout"
    const val SendCallTimeUrl = "$BaseUrl/crmCall/save"
    const val UploadRecordUrl = "$BaseUrl/crmCall/upload"


    const val WebSocketUrl = "ws://175.24.181.162:8090/ws"
    const val UploadFileUrl = "http://175.24.181.162:8090/ws"
    const val LoginUrl = "http://crm.shufeng.vip/api/v1/system/loginApp"


}