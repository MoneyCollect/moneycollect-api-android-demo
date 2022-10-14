package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.moneycollect.example_java.databinding.ActivityValidationBrowserBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;


/**
 * local payment scheme verification, done through {@link SchemeReceiveActivity}
 */
public class SchemeReceiveActivity extends BaseExampleActivity {

    private ActivityValidationBrowserBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        initUi();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        //Receiving a Scheme to jump, can also be a new activity to receive data
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        String dataString = intent.getDataString();
        Uri uri = intent.getData();
        if (scheme != null && dataString != null && uri != null) {
            String queryString = uri.getQuery();
            if (queryString.contains("paymentMethod") && queryString.contains(Constant.PAYMENT_ID_STR)
                    && queryString.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                    && queryString.contains(Constant.SOURCE_REDIRECT_SLUG_STR)){
                Constant.PAYMENT_LOCAL_SCHEME_URL = "";
                finish();
            }else {
                String systemInfo = queryString.replace("returnurl=", "");
                if (systemInfo != null && !TextUtils.isEmpty(systemInfo)) {
                    Constant.PAYMENT_LOCAL_SCHEME_URL = systemInfo;
                    finish();
                }
            }
        }
    }
}
