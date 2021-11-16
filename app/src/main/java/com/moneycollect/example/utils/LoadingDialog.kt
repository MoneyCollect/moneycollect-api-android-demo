package com.moneycollect.example.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import com.moneycollect.example.Constant
import com.moneycollect.example.R

/**
 * loading dialog
 */
class LoadingDialog : AlertDialog(mContext, R.style.Theme_AppCompat_Dialog) {
    private var loadView: View? = null
    private var mStartTime: Long = -1
    private var mPostedHide = false
    private var mPostedShow = false
    private var mDismissed = false
    private var mHandler: Handler? = Handler()
    private val mDelayedHide = Runnable {
        mPostedHide = false
        mStartTime = -1
        dismiss()
    }
    private val mDelayedShow = Runnable {
        mPostedShow = false
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis()
            show()
        }
    }

    fun showDialog() {
        mStartTime = -1
        mDismissed = false
        if (mHandler != null) {
            mHandler!!.removeCallbacks(mDelayedHide)
        }
        mPostedHide = false
        if (!mPostedShow) {
            if (mHandler != null) {
                mHandler!!.postDelayed(mDelayedShow, MIN_DELAY.toLong())
            }
            mPostedShow = true
        }
    }

    fun hideDialog() {
        mDismissed = true
        if (mHandler != null) {
            mHandler!!.removeCallbacks(mDelayedShow)
        }
        mPostedShow = false
        val diff = System.currentTimeMillis() - mStartTime
        if (diff >= MIN_SHOW_TIME || mStartTime == -1L) {
            hide()
        } else {
            if (!mPostedHide) {
                mHandler!!.postDelayed(mDelayedHide, MIN_SHOW_TIME - diff)
                mPostedHide = true
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mHandler != null) {
            mHandler!!.removeCallbacksAndMessages(null)
            mHandler = null
        }
        if (mContext != null) {
            mContext = null
        }
        if (loadView != null) {
            loadView!!.removeCallbacks(null)
            loadView = null
        }
        if (mLoadingDialog != null) {
            mLoadingDialog = null
        }
    }

    companion object {
        private const val MIN_SHOW_TIME = 100
        private const val MIN_DELAY = 200
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        @SuppressLint("StaticFieldLeak")
        var mLoadingDialog: LoadingDialog? = null
        fun getInstance(mmContext: Context?): LoadingDialog? {
            if (mmContext == null) {
                return null
            }
            mContext = mmContext
            if (mLoadingDialog == null) {
                synchronized(LoadingDialog::class.java) {
                    if (mLoadingDialog == null) {
                        mLoadingDialog = LoadingDialog()
                    }
                }
            }
            return mLoadingDialog
        }
    }

    init {
        if (loadView == null) {
            try {
                loadView = LayoutInflater.from(context)
                    .inflate(if (Constant.dialogLayoutId != 0) Constant.dialogLayoutId else R.layout.dialog_loading,
                        null)
            } catch (e: Exception) {
            }
            if (loadView == null) {
                loadView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
            }
        }
        setView(loadView)
    }
}