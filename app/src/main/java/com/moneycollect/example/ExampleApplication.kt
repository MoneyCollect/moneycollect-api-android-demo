package com.moneycollect.example

import android.app.Application
import com.moneycollect.android.MoneyCollectSdk

class ExampleApplication : Application() {

    override fun onCreate() {
        //init MoneyCollectSdk
        MoneyCollectSdk.init(this, "live_pr_97pxAhtkXCgm8Dd08L3NNTfV23hWRno9D9XZc9KnlK4","https://api.moneycollect.com")
        super.onCreate()
    }
}
