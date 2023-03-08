package com.android.zr.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.zr.utils.Constants
import com.android.zr.utils.LogUtil

/**
 * Created by matthew on 2023/03/08
 */
class WebSocketReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != null && intent.action.equals(Constants.RECEIVER_ACTION)) {
            val phoneNum = intent.getStringExtra("phone")

            LogUtil.d("%s", "phone = $phoneNum")
            val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
            phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(phoneIntent)

        }
    }

}