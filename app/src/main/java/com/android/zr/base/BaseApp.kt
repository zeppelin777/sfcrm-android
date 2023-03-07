package com.android.zr.base

import android.app.Application
import android.content.Context

/**
 * Created by matthew on 2023/03/07
 */
class BaseApp : Application() {

    companion object {
        private var instance: BaseApp? = null

        fun getInstance(): Context {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


}