package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.Constant
import com.moneycollect.example.Constant.Companion.PAYMENT_LOCAL_SCHEME_URL
import com.moneycollect.example.databinding.ActivityValidationBrowserBinding


/**
 * local payment scheme verification, done through [SchemeReceiveActivity]
 */
@Suppress("DEPRECATION")
class SchemeReceiveActivity : BaseExampleActivity(){

    private var viewBinding: ActivityValidationBrowserBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        initUi()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        //Receiving a Scheme to jump, can also be a new activity to receive data
        val intent = intent
        val scheme = intent.scheme
        val dataString = intent.dataString
        val uri = intent.data
        if (scheme != null && dataString != null && uri != null) {
            val queryString = uri.query.toString()
            if (queryString.contains("paymentMethod") && queryString.contains(Constant.PAYMENT_ID_STR)
                && queryString.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                && queryString.contains(Constant.SOURCE_REDIRECT_SLUG_STR)
            ) {
                PAYMENT_LOCAL_SCHEME_URL = ""
                finish()
            } else {
                val systemInfo = queryString.replace("returnurl=", "")
                if (systemInfo != null && !TextUtils.isEmpty(systemInfo)) {
                    PAYMENT_LOCAL_SCHEME_URL = systemInfo
                    finish()
                }
            }
        }
    }
}