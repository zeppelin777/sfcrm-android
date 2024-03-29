package com.android.shufeng.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.shufeng.R
import com.android.shufeng.utils.UiUtils
import com.android.shufeng.databinding.DialogLoadingPbBinding

/**
 * Created by matthew on 2023/03/07
 */
open class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private lateinit var loadingPbBinding: DialogLoadingPbBinding
    private lateinit var loadingDialog: AlertDialog

    open fun showLoading() {
        if (!::loadingDialog.isInitialized) {
            loadingPbBinding = DialogLoadingPbBinding.inflate(layoutInflater)
            loadingDialog = AlertDialog.Builder(this).create()

            loadingDialog.show()
            val window = loadingDialog.window
            if (window != null) {
                val attr = window.attributes
                attr.width = UiUtils.getDimens(R.dimen.x240)
                attr.height = UiUtils.getDimens(R.dimen.x240)
                window.setContentView(loadingPbBinding.root)
                window.attributes = attr
            }
        }
        loadingDialog.show()
    }

    open fun hideLoading() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }


    private lateinit var tipsDialog: AlertDialog

    open fun showTips(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            if (!::tipsDialog.isInitialized) {
                tipsDialog = AlertDialog.Builder(this)
                    .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
            }
            tipsDialog.setMessage(msg)
            tipsDialog.show()
        }
    }

    private fun hideTips() {
        if (::tipsDialog.isInitialized && tipsDialog.isShowing) {
            tipsDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
        hideTips()
    }

}