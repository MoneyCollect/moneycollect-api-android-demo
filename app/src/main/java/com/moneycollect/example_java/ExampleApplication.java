package com.moneycollect.example_java;

import android.app.Application;

import com.moneycollect.android.MoneyCollectSdk;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        MoneyCollectSdk.init(this, "test_pu_1sWrsjQP9PJiCwGsYv3risSn8YBCIEMNoVFIo8eR6s","http://192.168.2.100:9898/");
        super.onCreate();
    }
}
