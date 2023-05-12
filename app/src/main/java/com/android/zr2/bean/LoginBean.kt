package com.android.zr2.bean

import com.google.gson.annotations.SerializedName

/**
 * Created by matthew on 2023/03/07
 */
class LoginBean {

    @SerializedName("token")
    var adminToken: String? = null
    @SerializedName("uid")
    var userId: String? = null

}