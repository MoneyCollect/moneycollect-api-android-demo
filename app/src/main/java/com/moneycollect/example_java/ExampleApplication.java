package com.moneycollect.example_java;

import android.app.Application;

import com.moneycollect.android.MoneyCollectSdk;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        //init MoneyCollectSdk
       MoneyCollectSdk.init(this, "live_pr_97pxAhtkXCgm8Dd08L3NNTfV23hWRno9D9XZc9KnlK4","https://api.moneycollect.com");
        super.onCreate();
    }
}
