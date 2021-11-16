package com.moneycollect.example

import android.app.Application
import com.moneycollect.android.MoneyCollectSdk

class ExampleApplication : Application() {

    override fun onCreate() {

        //init MoneyCollectSdk
        MoneyCollectSdk.init(this,
            "test_pu_1sWrsjQP9PJiCwGsYv3risSn8YBCIEMNoVFIo8eR6s",
            "http://192.168.2.100:9898/")

        super.onCreate()

    }
}
