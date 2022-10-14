package com.moneycollect.example.utils

import android.content.Context
import com.moneycollect.android.utils.MoneyCollectCheckoutCurrency.Companion.find
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

fun getAmountTransferNum(currency: String?, amount: String?): BigDecimal? {
    val fractionDigits = getFractionDigits(currency)
    var bigDecimal = BigDecimal(amount)
    bigDecimal = bigDecimal.setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
    return bigDecimal
}


fun getCurrencyTransnum(currency: String?): BigDecimal {
    return when (getFractionDigits(currency)) {
        0 -> {
            BigDecimal("1")
        }
        1 -> {
            BigDecimal("10")
        }
        2 -> {
            BigDecimal("100")
        }
        3 -> {
            BigDecimal("1000")
        }
        else -> {
            BigDecimal("100")
        }
    }
}

fun getCurrencyZeroDecimalFormat(currency: String): BigDecimal {
    return when (getFractionDigits(currency)) {
        0 -> {
            BigDecimal("0")
        }
        1 -> {
            BigDecimal("0.0")
        }
        2 -> {
            BigDecimal("0.00")
        }
        3 -> {
            BigDecimal("0.000")
        }
        else -> {
            BigDecimal("0.00")
        }
    }
}

fun getCurrencyDecimalFormat(currency: String): DecimalFormat {
    return when (getFractionDigits(currency)) {
        0 -> {
            DecimalFormat("0")
        }
        1 -> {
            DecimalFormat("0.0")
        }
        2 -> {
            DecimalFormat("0.00")
        }
        3 -> {
            DecimalFormat("0.000")
        }
        else -> {
            DecimalFormat("0.00")
        }
    }
}


fun getFractionDigits(currencyCode: String?): Int {
    val normalizedCurrencyCode = currencyCode?.uppercase(Locale.getDefault())
    try {
        val checkoutCurrency = find(normalizedCurrencyCode)
        return checkoutCurrency.fractionDigits
    } catch (e: Exception) {
    }
    return try {
        val currency = Currency.getInstance(normalizedCurrencyCode)
        Math.max(currency.defaultFractionDigits, 0)
    } catch (e: Exception) {
        2
    }
}


fun getCurrencyUnitTag(currency: String, context: Context): String {
    return getCurrencyStr(currency)
}

/**
 * Gets the current currency symbol
 */
fun getCurrencyStr(currency: String): String {
    var newC = currency.uppercase()
    when (currency.uppercase()) {
        "USD" -> newC = "US$"
        "EUR" -> newC = "€"
        "JPY" -> newC = "JP¥"
        "GBP" -> newC = "£"
        "AUD" -> newC = "A$"
        "CAD" -> newC = "C$"
        "CHF" -> newC = "CHF"
        "CNY" -> newC = "CN¥"
        "HKD" -> newC = "HK$"
        "NZD" -> newC = "NZ$"
        "SEK" -> newC = "kr"
        "KRW" -> newC = "₩"
        "SGD" -> newC = "S$"
        "NOK" -> newC = "kr"
        "MXN" -> newC = "MX$"
        "INR" -> newC = "₹"
        "RUB" -> newC = "₽"
        "ZAR" -> newC = "R"
        "TRY" -> newC = "₺"
        "BRL" -> newC = "R$"
        "TWD" -> newC = "NT$"
        "DKK" -> newC = "kr"
        "PLN" -> newC = "zł"
        "THB" -> newC = "฿"
        "IDR" -> newC = "Rp"
        "IQD" -> newC = "ID"
        "HUF" -> newC = "Ft"
        "CZK" -> newC = "Kč"
        "ILS" -> newC = "₪"
        "CLP" -> newC = "CLP$"
        "PHP" -> newC = "₱"
        "AED" -> newC = "د.إ"
        "COP" -> newC = "COL$"
        "SAR" -> newC = "﷼"
        "MYR" -> newC = "RM"
        "RON" -> newC = "L"
    }
    return newC
}

enum class CheckoutCurrency {
    USD,
    JPY,
    KRW,
    IQD,
    CNY,
    EUR
}

enum class CheckoutCreditCardCurrency(val code: String){
    CREDIT_CARD("Credit Card"),
}

enum class CheckoutCardPayCurrency(val code: String){
    VISA("Visa"),
    MASTER("Master"),
    AME("Ame"),
    JCB("JCB"),
    DINNE("Dinne"),
    DISCOVER("Discover"),
    MAESTRO("Maestro")
}

enum class CheckoutLocalCurrency(val code: String){
    TrueMoney("truemoney"),
    DANA("dana"),
    GCash("gcash"),
    TNG("tng"),
    Atome("atome"),
    KAKAO_PAY("kakao_pay"),
    Klarna("klarna"),
    POLi("poli"),
    MyBank("myBank"),
    EPS("eps"),
    Przelewy24("przelewy24"),
    Bancontact("bancontact"),
    Ideal("ideal"),
    Giropay("giropay"),
    Sofort("sofort"),
    AlipayHK("alipay_hk"),
    Alipay("alipay"),
    WECHAT_PAY("wechat_pay")
}

enum class CheckoutWebPage(val code: String){
    WebView("WebView"),
    Browser("Browser"),
}

enum class CheckoutFromChannel{
    WEB, H5, APP, MINI
}

var kakaoPayName:String="Kakao Pay"
var wechatPayName:String="WeChat Pay"