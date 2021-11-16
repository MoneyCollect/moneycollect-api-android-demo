package com.moneycollect.example.utils

import android.content.Context
import com.moneycollect.example.R
import java.math.BigDecimal
import java.text.DecimalFormat

fun getCurrencyTransnum(currency: String?): BigDecimal {
    when (currency) {
        CheckoutCurrency.USD.toString() -> {
            return BigDecimal("100")
        }
        CheckoutCurrency.KRW.toString() -> {
            return BigDecimal("1")
        }
        CheckoutCurrency.IQD.toString() -> {
            return BigDecimal("1000")
        }
    }
    return BigDecimal("1")
}
fun getCurrencyZeroDecimalFormat(currency: String): BigDecimal {

    when (currency) {
        CheckoutCurrency.USD.toString() -> {
            return BigDecimal("0.00")
        }
        CheckoutCurrency.KRW.toString() -> {
            return BigDecimal("0")
        }
        CheckoutCurrency.IQD.toString() -> {
            return BigDecimal("0.000")
        }
    }
    return BigDecimal("0")
}

fun getCurrencyDecimalFormat(currency: String): DecimalFormat {

    when (currency) {
        CheckoutCurrency.USD.toString() -> {
            return DecimalFormat("0.00")
        }
        CheckoutCurrency.KRW.toString() -> {
            return DecimalFormat("0")
        }
        CheckoutCurrency.IQD.toString() -> {
            return DecimalFormat("0.000")
        }
    }
    return DecimalFormat("0")
}


fun getCurrencyUnitTag(currency: String, context: Context): String {
    when (currency) {
        CheckoutCurrency.USD.toString() -> {
            return context.getString(R.string.setting_currency_usd_tag)
        }
        CheckoutCurrency.KRW.toString() -> {
            return context.getString(R.string.setting_currency_krw_tag)
        }
        CheckoutCurrency.IQD.toString() -> {
            return context.getString(R.string.setting_currency_iqd_tag)
        }
    }

    return context.getString(R.string.setting_currency_usd_tag)
}

enum class CheckoutCurrency {
    USD,
    KRW,
    IQD
}