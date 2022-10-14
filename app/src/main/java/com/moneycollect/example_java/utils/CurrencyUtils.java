package com.moneycollect.example_java.utils;

import android.content.Context;


import com.moneycollect.android.utils.MoneyCollectCheckoutCurrency;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Currency;

public class CurrencyUtils {

    public static BigDecimal getAmountTransferNum(String currency,String amount) {
        int fractionDigits = getFractionDigits(currency);
        BigDecimal bigDecimal = new BigDecimal(amount);
        bigDecimal=bigDecimal.setScale(fractionDigits,BigDecimal.ROUND_HALF_UP);
        return bigDecimal;
    }

    public static BigDecimal getCurrencyTransnum(String currency) {
        int fractionDigits = getFractionDigits(currency);
        if (fractionDigits == 0){
            return new BigDecimal("1");
        }else if (fractionDigits == 1){
            return new BigDecimal("10");
        }else if (fractionDigits == 2){
            return new BigDecimal("100");
        }else if (fractionDigits == 3){
            return new BigDecimal("1000");
        }else {
            return new BigDecimal("100");
        }
    }

    public static BigDecimal getCurrencyZeroDecimalFormat(String currency) {
        int fractionDigits = getFractionDigits(currency);
        if (fractionDigits == 0){
            return new BigDecimal("0");
        }else if (fractionDigits == 1){
            return new BigDecimal("0.0");
        }else if (fractionDigits == 2){
            return new BigDecimal("0.00");
        }else if (fractionDigits == 3){
            return new BigDecimal("0.000");
        }else {
            return new BigDecimal("0.00");
        }
    }

    public static DecimalFormat getCurrencyDecimalFormat(String currency) {
        int fractionDigits = getFractionDigits(currency);
        if (fractionDigits == 0){
            return new DecimalFormat("0");
        }else if (fractionDigits == 1){
            return new DecimalFormat("0.0");
        }else if (fractionDigits == 2){
            return new DecimalFormat("0.00");
        }else if (fractionDigits == 3){
            return new DecimalFormat("0.000");
        }else {
            return new DecimalFormat("0.00");
        }
    }


    public static int getFractionDigits(String currencyCode) {
        String normalizedCurrencyCode = currencyCode.toUpperCase();
        try {
            MoneyCollectCheckoutCurrency checkoutCurrency =
                    MoneyCollectCheckoutCurrency.Companion.find(normalizedCurrencyCode);
            return checkoutCurrency.getFractionDigits();
        } catch (Exception e) {

        }
         try {
            Currency currency = Currency.getInstance(normalizedCurrencyCode);
            return Math.max(currency.getDefaultFractionDigits(), 0);
        } catch (Exception e) {
            return 2;
        }
    }

    public static String getCurrencyUnitTag(String currency, Context context) {
        return getCurrencyStr(currency);
    }


    /**
     * Gets the current currency symbol
     */
    public static String getCurrencyStr(String currency){
        String newC = currency.toUpperCase();
        switch (currency.toUpperCase()) {
            case "USD":
                newC = "US$";
                break;
            case "EUR":
                newC = "€";
                break;
            case "JPY":
                newC = "JP¥";
                break;
            case "GBP":
                newC = "£";
                break;
            case "AUD":
                newC = "A$";
                break;
            case "CAD":
                newC = "C$";
                break;
            case "CHF":
                newC = "CHF";
                break;
            case "CNY":
                newC = "CN¥";
                break;
            case "HKD":
                newC = "HK$";
                break;
            case "NZD":
                newC = "NZ$";
                break;
            case "SEK":
                newC = "kr";
                break;
            case "KRW":
                newC = "₩";
                break;
            case "SGD":
                newC = "S$";
                break;
            case "NOK":
                newC = "kr";
                break;
            case "MXN":
                newC = "MX$";
                break;
            case "INR":
                newC = "₹";
                break;
            case "RUB":
                newC = "₽";
                break;
            case "ZAR":
                newC = "R";
                break;
            case "TRY":
                newC = "₺";
                break;
            case "BRL":
                newC = "R$";
                break;
            case "TWD":
                newC = "NT$";
                break;
            case "DKK":
                newC = "kr";
                break;
            case "PLN":
                newC = "zł";
                break;
            case "THB":
                newC = "฿";
                break;
            case "IDR":
                newC = "Rp";
                break;
            case "IQD":
                newC = "ID";
                break;
            case "HUF":
                newC = "Ft";
                break;
            case "CZK":
                newC = "Kč";
                break;
            case "ILS":
                newC = "₪";
                break;
            case "CLP":
                newC = "CLP$";
                break;
            case "PHP":
                newC = "₱";
                break;
            case "AED":
                newC = "د.إ";
                break;
            case "COP":
                newC = "COL$";
                break;
            case "SAR":
                newC = "﷼";
                break;
            case "MYR":
                newC = "RM";
                break;
            case "RON":
                newC = "L";
                break;
        }
        return newC;
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

    public enum CheckoutCreditCardCurrency {
        CREDIT_CARD("Credit Card");
        String code;

        CheckoutCreditCardCurrency(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public enum  CheckoutCardPayCurrency{
        VISA("Visa"),
        MASTER("Master"),
        AME("Ame"),
        JCB("JCB"),
        DINNE("Dinne"),
        DISCOVER("Discover"),
        MAESTRO("Maestro");

        String code;
        CheckoutCardPayCurrency(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public enum CheckoutLocalCurrency {
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
        WECHAT_PAY("wechat_pay");

        String code;

        CheckoutLocalCurrency(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public enum CheckoutWebPage {
        WebView("WebView"),
        Browser("Browser");
        String code;

        CheckoutWebPage(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public enum CheckoutFromChannel {
        WEB("WEB"),
        H5("H5"),
        APP("APP"),
        MINI("MINI");
        String code;

        CheckoutFromChannel(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public static String kakaoPayName="Kakao Pay";
    public static String wechatPayName="WeChat Pay";
}

