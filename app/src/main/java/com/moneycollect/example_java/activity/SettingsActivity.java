package com.moneycollect.example_java.activity;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.moneycollect.example.databinding.SettingsActivityBinding;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.CurrencyUtils;


public class SettingsActivity extends AppCompatActivity {

    private SettingsActivityBinding viewBinding = null;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUI();
    }

    private void initUI() {
        switch (CurrencyUtils.CheckoutCurrency.valueOf(TestRequestData.Companion.getCurrency())) {
            case USD:
                viewBinding.settingRg.check(viewBinding.settingUsdRb.getId());
                break;
            case KRW:
                viewBinding.settingRg.check(viewBinding.settingKrwRb.getId());
                break;
            case IQD:
                viewBinding.settingRg.check(viewBinding.settingIqdRb.getId());
                break;
        }
        viewBinding.settingRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == viewBinding.settingUsdRb.getId()) {
                    TestRequestData.Companion.setCurrency(CurrencyUtils.CheckoutCurrency.USD.toString());
                    TestRequestData.Companion.getTestRequestPayment().setCurrency(CurrencyUtils.CheckoutCurrency.USD.toString());
                    TestRequestData.Companion.getTestConfirmPayment().setCurrency(CurrencyUtils.CheckoutCurrency.USD.toString());
                } else if (checkedId == viewBinding.settingKrwRb.getId()) {
                    TestRequestData.Companion.setCurrency(CurrencyUtils.CheckoutCurrency.KRW.toString());
                    TestRequestData.Companion.getTestRequestPayment().setCurrency(CurrencyUtils.CheckoutCurrency.KRW.toString());
                    TestRequestData.Companion.getTestConfirmPayment().setCurrency(CurrencyUtils.CheckoutCurrency.KRW.toString());
                } else if (checkedId == viewBinding.settingIqdRb.getId()) {
                    TestRequestData.Companion.setCurrency(CurrencyUtils.CheckoutCurrency.IQD.toString());
                    TestRequestData.Companion.getTestRequestPayment().setCurrency(CurrencyUtils.CheckoutCurrency.IQD.toString());
                    TestRequestData.Companion.getTestConfirmPayment().setCurrency(CurrencyUtils.CheckoutCurrency.IQD.toString());
                }
            }
        });

    }
}
