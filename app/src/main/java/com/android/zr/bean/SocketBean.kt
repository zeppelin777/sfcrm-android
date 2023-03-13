package com.android.zr.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by matthew on 2023/3/11
 */
class SocketBean() : Parcelable {

    var action: String? = null
    var from: String? = null
    var model: String? = null
    var to: String? = null
    var message: String? = null

    constructor(parcel: Parcel) : this() {
        action = parcel.readString()
        from = parcel.readString()
        model = parcel.readString()
        to = parcel.readString()
        message = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(action)
        parcel.writeString(from)
        parcel.writeString(model)
        parcel.writeString(to)
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SocketBean> {
        override fun createFromParcel(parcel: Parcel): SocketBean {
            return SocketBean(parcel)
        }

        override fun newArray(size: Int): Array<SocketBean?> {
            return arrayOfNulls(size)
        }
    }

}