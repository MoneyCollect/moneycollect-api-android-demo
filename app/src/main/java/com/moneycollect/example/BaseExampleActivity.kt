package com.moneycollect.example

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moneycollect.android.MoneyCollect
import com.moneycollect.android.MoneyCollectFactory
import com.moneycollect.example.utils.LoadingDialog

/**
 * The parent class Activity, the other Activity to inherit it
 */
abstract class BaseExampleActivity : AppCompatActivity() {

    private var requestLoadingDialog: LoadingDialog? = null

    val moneyCollect: MoneyCollect by lazy {
        MoneyCollectFactory(application).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * display tip
     */
    fun showToast(description: String?){
        Toast.makeText(applicationContext, description, Toast.LENGTH_SHORT).show()
    }

    /**
     * Is requesting interface display dialog
     */
    fun showLoadingDialog(){
        if (requestLoadingDialog == null) {
            requestLoadingDialog = LoadingDialog.getInstance(this)
            if (requestLoadingDialog != null) {
                requestLoadingDialog?.showDialog()
                requestLoadingDialog?.setCanceledOnTouchOutside(false)
                requestLoadingDialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                    event.keyCode == KeyEvent.KEYCODE_BACK
                })
            }
        } else {
            requestLoadingDialog?.show()
        }
    }

    /**
     * The hidden dialog when request interface
     */
    fun dismissLoadingDialog() {
        if (requestLoadingDialog != null) {
            requestLoadingDialog?.hideDialog()
        }
    }

    override fun onDestroy() {
        if (requestLoadingDialog != null) {
            requestLoadingDialog?.dismiss()
            requestLoadingDialog?.onDetachedFromWindow()
            requestLoadingDialog = null
        }
        super.onDestroy()
    }
}