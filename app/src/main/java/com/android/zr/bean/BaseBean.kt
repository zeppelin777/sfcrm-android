package com.android.zr.bean

/**
 * Created by matthew on 2023/03/07
 */
open class BaseBean<T> {

    var code = 0
    var msg: String? = null
    var data: T? = null

}