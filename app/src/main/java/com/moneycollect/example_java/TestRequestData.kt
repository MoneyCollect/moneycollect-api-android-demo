package com.moneycollect.example_java

import android.annotation.SuppressLint
import android.webkit.WebSettings
import com.moneycollect.android.MoneyCollectSdk
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreateCustomer
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.example_java.utils.CurrencyUtils
import java.math.BigInteger

@SuppressLint("ParcelCreator")
class TestRequestData() {

    companion object {
        val testCard = RequestPaymentMethod.Card(
            cardNo = "4111111111111111",
            expMonth = "12",
            expYear = "2024",
            securityCode = "217",
        )

        var wxH5AuthorizationUrl="https://test-api.moneycollect.com"   // https://test-payment.moneycollect.com | moneycollect.com

        var amount =  BigInteger("100")

        //the fromChannel of payment(WEB, H5, APP, MINI)
        var fromChannel = CurrencyUtils.CheckoutFromChannel.APP.toString()

        //the country of payment
        var country = "US"            //KR  AT  CHN

        //Currency Unit
        var currency = CurrencyUtils.CheckoutCurrency.USD.toString()      //KRW  EUR CNY

        //select All Payments of the Customer
        var customerId = "cus_1567366884914024449"  //cus_1513719005473644545     cus_1567366884914024449

        //attach payment to the Customer
        var paymentMethodId = "pm_1459080811770830849"

        //the Id of Payment
        var paymentId = "py_1450377022261047297"

        //the firstName of Payment
        var firstName = "Jenny"

        //the lastName of Payment
        var lastName = "Rosen"

        //the phone of Payment
        var phone = "+18008675309"

        //the email of Payment
        var email = "112374@gmail.com"

        //the website of Payment
        var website = "https://baidu222.com"

        //the returnUrl of Payment
        var returnUrl = "https://sandbox-pay.asiabill.com:443/pages/PayResult.jsp"   //http://localhost:8080/return

        //the clientSecret of Payment
        var clientSecret = "py_1452465618203938817_secret_9ABA4137F0FA9479C21F7EF900F0FB27"

        //the address of Payment
        var address = Address(
            city = "Blackrock",
            country = country,
            line1 = "123 Main Street",
            line2 = "number456",
            postalCode = "T37 F8HK",
            state = "Co. Dublin"
        )

        var lineItems = listOf(
            RequestCreatePayment.LineItems(
                amount = amount,
                currency = currency,
                description = "1222211",
                images = listOf("http://localhost/item.jpg"),
                name = "test",
                quantity = 2,
            )
        )

        //"card","kakao_pay","klarna","poli","mybank","eps","przelewy24","bancontact","ideal","giropay","sofort","alipay_hk","wechat_pay"
        var paymentMethodTypes = listOf(
            "card"
        )

        var weChatPayNextActionType="wechat_pay_h5"

        //create customer
        var testCustomer = RequestCreateCustomer(
            address,
            description = "test",
            email = email,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            shipping = RequestCreateCustomer.Shipping(
                address,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
        )

        //create paymentmethod
        var testBilling = RequestPaymentMethod.BillingDetails(
            address,
            email = email,
            firstName = firstName,
            lastName = lastName,
            phone = phone
        )

        var  testRequestPaymentMethod=RequestPaymentMethod(
            "card",
            testBilling,
            testCard
        )

        //create payment
        var testRequestPayment = RequestCreatePayment(
            null,
            amount = amount,
            confirm = false,
            confirmationMethod = RequestCreatePayment.ConfirmationMethod.Automatic,
            currency = currency,
            customerId = customerId,
            description = "2333233",
            fromChannel = fromChannel,
            ip = "192.168.0.12",
            lineItems = lineItems,
            notifyUrl = "http://localhost:8080/notify",
            orderNo = "1",
            paymentMethod = paymentMethodId,
            paymentMethodTypes = paymentMethodTypes,
            preAuth = "n",
            receiptEmail = email,
            returnUrl = returnUrl,
            setupFutureUsage = "off",
            shipping = RequestCreatePayment.Shipping(
                address = address,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            ),
            statementDescriptor = "www.1 23",
            statementDescriptorSuffix = "AAAA",
            userAgent = WebSettings.getDefaultUserAgent(MoneyCollectSdk.context),
            website = website
        )

        //confirm payment
        var testConfirmPayment = RequestConfirmPayment(
            amount = amount,
            currency = currency,
            id = paymentId,
            ip = "192.168.0.12",
            notifyUrl = "http://localhost:8080/notify",
            paymentMethod = paymentMethodId,
            receiptEmail = email,
            returnUrl = returnUrl,
            setupFutureUsage = "off",
            shipping = RequestConfirmPayment.Shipping(
                address = address,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            ),
            website = website
        )

        //support payment credit card
        var testBankIvList= arrayListOf(
            R.drawable.mc_card_visa,
            R.drawable.mc_card_mastercard,
            R.drawable.mc_card_ae,
            R.drawable.mc_card_jcb,
            R.drawable.mc_card_dinner,
            R.drawable.mc_card_discover,
            R.drawable.mc_card_maestro
        )


        var  testCardPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.code,
            null,
            null
        )


        var  testTrueMoneyPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.TrueMoney.code,
            null,
            null
        )

        var  testDANAPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.DANA.code,
            null,
            null
        )

        var  testGCashPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.GCash.code,
            null,
            null
        )


        var  testTNGPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.TNG.code,
            null,
            null
        )

        var  testAtomePaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Atome.code,
            null,
            null
        )
        var  testKakaoPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.KAKAO_PAY.code,
            null,
            null
        )
        var  testKlarnaPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Klarna.code,
            null,
            null
        )
        var  testPOLiPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.POLi.code,
            null,
            null
        )
        var  testMyBankPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.MyBank.code,
            null,
            null
        )
        var  testEPSPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.EPS.code,
            null,
            null
        )
        var  testPrzelewy24PaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Przelewy24.code,
            null,
            null
        )
        var  testBancontactPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Bancontact.code,
            null,
            null
        )
        var  testIdealPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Ideal.code,
            null,
            null
        )
        var  testGiropayPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Giropay.code,
            null,
            null
        )
        var  testSofortPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Sofort.code,
            null,
            null
        )
        var  testAlipayHkPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.AlipayHK.code,
            null,
            null
        )
        var  testAlipayPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.Alipay.code,
            null,
            null
        )
        var  testWeChatPaymentMethod = PaymentMethod(
            "",
            "",
            "",
            CurrencyUtils.CheckoutLocalCurrency.WECHAT_PAY.code,
            null,
            null
        )

        //support payment local card
        var testLocalBankList: ArrayList<PaymentMethod> = arrayListOf(
            testAlipayHkPaymentMethod,
            testWeChatPaymentMethod,
            testAlipayPaymentMethod,
            testKlarnaPaymentMethod,
            testGCashPaymentMethod,
            testTrueMoneyPaymentMethod,
            testDANAPaymentMethod,
            testTNGPaymentMethod,
            testAtomePaymentMethod,
            testKakaoPaymentMethod,
            testPOLiPaymentMethod,
            testMyBankPaymentMethod,
            testEPSPaymentMethod,
            testPrzelewy24PaymentMethod,
            testBancontactPaymentMethod,
            testIdealPaymentMethod,
            testGiropayPaymentMethod,
            testSofortPaymentMethod
        )
            get() = field


        //support payment All card
        var testAllBankList: ArrayList<PaymentMethod> = arrayListOf(
            testCardPaymentMethod,
            testAlipayHkPaymentMethod,
            testWeChatPaymentMethod,
            testAlipayPaymentMethod,
            testKlarnaPaymentMethod,
            testGCashPaymentMethod,
            testTrueMoneyPaymentMethod,
            testDANAPaymentMethod,
            testTNGPaymentMethod,
            testAtomePaymentMethod,
            testKakaoPaymentMethod,
            testPOLiPaymentMethod,
            testMyBankPaymentMethod,
            testEPSPaymentMethod,
            testPrzelewy24PaymentMethod,
            testBancontactPaymentMethod,
            testIdealPaymentMethod,
            testGiropayPaymentMethod,
            testSofortPaymentMethod
        )
            get() = field
    }
}

