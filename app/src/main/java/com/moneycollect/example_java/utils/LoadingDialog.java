package com.moneycollect.example_java.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.moneycollect.example.R;
import com.moneycollect.example_java.Constant;


/**
 * Created by wzl
 */

public class LoadingDialog extends AlertDialog {

    private static final int MIN_SHOW_TIME = 500;
    private static final int MIN_DELAY = 500;

    private View loadView;
    private static Context mContext;
    public static LoadingDialog mLoadingDialog;

    private long mStartTime = -1;
    private boolean mPostedHide = false;
    private boolean mPostedShow = false;
    private boolean mDismissed = false;

    private Handler mHandler = new Handler();

    private final Runnable mDelayedHide = new Runnable() {

        @Override
        public void run() {
            mPostedHide = false;
            mStartTime = -1;
            dismiss();
        }
    };

    private final Runnable mDelayedShow = new Runnable() {

        @Override
        public void run() {
            mPostedShow = false;
            if (!mDismissed) {
                mStartTime = System.currentTimeMillis();
                show();
            }
        }
    };
    public static LoadingDialog getInstance(Context mmContext){
        if(mmContext == null){
            return null;
        }
        mContext = mmContext;
        if (mLoadingDialog == null) {
            synchronized (LoadingDialog.class) {
                if (mLoadingDialog == null) {
                    mLoadingDialog = new LoadingDialog();
                }
            }
        }
        return mLoadingDialog;
    }

    public LoadingDialog() {
        super(mContext, R.style.Theme_AppCompat_Dialog);
        if(loadView == null) {
            try {
                loadView = LayoutInflater.from(getContext()).inflate(Constant.dialogLayoutId != 0?Constant.dialogLayoutId : R.layout.dialog_loading, null);
            }catch (Exception e){
            }
            if(loadView == null){
                loadView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
            }
        }
        setView(loadView);
    }

    public void showDialog() {
        mStartTime = -1;
        mDismissed = false;
        if(mHandler != null) {
            mHandler.removeCallbacks(mDelayedHide);
        }
        mPostedHide = false;
        if (!mPostedShow) {
            if(mHandler != null) {
                mHandler.postDelayed(mDelayedShow, MIN_DELAY);
            }
            mPostedShow = true;
        }
    }

    public void hideDialog() {
        mDismissed = true;
        if(mHandler != null) {
            mHandler.removeCallbacks(mDelayedShow);
        }
        mPostedShow = false;
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME || mStartTime == -1) {
            dismiss();
        } else {
            if (!mPostedHide) {
                mHandler.postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
                mPostedHide = true;
            }
        }
    }
    public void hideDialogNodiss() {
        mDismissed = true;
        if(mHandler != null) {
            mHandler.removeCallbacks(mDelayedShow);
        }
        mPostedShow = false;
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME || mStartTime == -1) {
            hide();
        } else {
            if (!mPostedHide) {
                mHandler.postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
                mPostedHide = true;
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if(mContext != null){
            mContext = null;
        }
        if(loadView != null){
            loadView.removeCallbacks(null);
            loadView = null;
        }
        if(mLoadingDialog != null){
            mLoadingDialog = null;
        }
    }
}