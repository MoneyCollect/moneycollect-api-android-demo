package com.moneycollect.example_java.utils;

import android.content.Context;

import com.moneycollect.example.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CurrencyUtils {

    public static BigDecimal getCurrencyTransnum(String currency) {
        switch (CheckoutCurrency.valueOf(currency)) {
            case USD:
                return new BigDecimal("100");
            case KRW:
                return new BigDecimal("1");
            case IQD:
                return new BigDecimal("1000");
        }
        return new BigDecimal("1");

    }

    public static BigDecimal getCurrencyZeroDecimalFormat(String currency) {

        switch (CheckoutCurrency.valueOf(currency)) {
            case USD:
                return new BigDecimal("0.00");
            case KRW:
                return new BigDecimal("0");
            case IQD:
                return new BigDecimal("0.000");
        }
        return new BigDecimal("0");

    }

    public static DecimalFormat getCurrencyDecimalFormat(String currency) {

        switch (CheckoutCurrency.valueOf(currency)) {
            case USD:
                return new DecimalFormat("0.00");
            case KRW:
                return new DecimalFormat("0");
            case IQD:
                return new DecimalFormat("0.000");
        }
        return new DecimalFormat("0");

    }


    public static String getCurrencyUnitTag(String currency, Context context) {
        switch (CheckoutCurrency.valueOf(currency)) {
            case USD:
                return context.getString(R.string.setting_currency_usd_tag);
            case KRW:
                return context.getString(R.string.setting_currency_krw_tag);
            case IQD:
                return context.getString(R.string.setting_currency_iqd_tag);
        }
        return context.getString(R.string.setting_currency_usd_tag);
    }
    public enum CheckoutCurrency {
        USD("USD"),
        KRW("KRW"),
        IQD("IQD");
        private String currency;

        CheckoutCurrency(String currency) {
            this.currency = currency;
        }

        public String getCurrency() {
            return currency;
        }
        public static CheckoutCurrency getCheckoutCurrency(String currency){
            for(CheckoutCurrency checkoutCurrency : values()){
                if(checkoutCurrency.getCurrency().equals(currency)){
                    return checkoutCurrency;
                }
            }
            return CheckoutCurrency.USD;
        }
    }
}

