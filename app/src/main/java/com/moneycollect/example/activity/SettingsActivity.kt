package com.moneycollect.example.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.SettingsActivityBinding
import com.moneycollect.example.utils.CheckoutCurrency

class SettingsActivity : AppCompatActivity() {
    private var viewBinding: SettingsActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUI()
    }

    private fun initUI() {
        when( TestRequestData.currency){
            CheckoutCurrency.USD.toString() -> {
                viewBinding?.settingUsdRb?.id?.let { viewBinding?.settingRg?.check(it) }
            }
            CheckoutCurrency.KRW.toString()-> {
                viewBinding?.settingKrwRb?.id?.let { viewBinding?.settingRg?.check(it) }
            }
            CheckoutCurrency.IQD.toString() -> {
                viewBinding?.settingIqdRb?.id?.let { viewBinding?.settingRg?.check(it) }
            }
        }
        viewBinding?.settingRg?.setOnCheckedChangeListener { group, checkedId ->
            run {
                when (checkedId) {
                    viewBinding?.settingUsdRb?.id -> {
                        TestRequestData.currency = CheckoutCurrency.USD.toString()
                        TestRequestData.testRequestPayment.currency = CheckoutCurrency.USD.toString()
                        TestRequestData.testConfirmPayment.currency = CheckoutCurrency.USD.toString()
                    }
                    viewBinding?.settingKrwRb?.id -> {
                        TestRequestData.currency = CheckoutCurrency.KRW.toString()
                        TestRequestData.testRequestPayment.currency = CheckoutCurrency.KRW.toString()
                        TestRequestData.testConfirmPayment.currency = CheckoutCurrency.KRW.toString()
                    }
                    viewBinding?.settingIqdRb?.id -> {
                        TestRequestData.currency = CheckoutCurrency.IQD.toString()
                        TestRequestData.testRequestPayment.currency = CheckoutCurrency.IQD.toString()
                        TestRequestData.testConfirmPayment.currency = CheckoutCurrency.IQD.toString()
                    }
                }
            }
        }
    }

}