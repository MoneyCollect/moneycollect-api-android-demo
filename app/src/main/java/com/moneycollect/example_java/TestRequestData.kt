package com.moneycollect.example_java

import com.moneycollect.example.R
import android.annotation.SuppressLint
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreateCustomer
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
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

        //Currency Unit
        var currency = CurrencyUtils.CheckoutCurrency.USD.toString()

        //select All Payments of the Customer
        // val customerId = "cus_1450372824458997761"
        //val customerId = "cus_1452476724628656130"
        //var customerId = "cus_1452880617225281538"
        //var customerId = "cus_1456104806547611649"
        var customerId = "cus_1459080620409905154"

        //attach payment to the Customer
        // val paymentMethodId = "pm_1450373464224575490"
        //val paymentMethodId = "pm_1452460803243610114"
        //var paymentMethodId = "pm_1452880616088625154"
        //var paymentMethodId = "pm_1456105345251434497"
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

        //the clientSecret of Payment
        var clientSecret = "py_1452465618203938817_secret_9ABA4137F0FA9479C21F7EF900F0FB27"

        //the address of Payment
        var address = Address(
            line1 = "123 Main Street",
            line2 = "number456",
            city = "Blackrock",
            state = "Co. Dublin",
            postalCode = "T37 F8HK",
            country = "US",
        )

        var lineItems = listOf(
            RequestCreatePayment.LineItems(
                amount = BigInteger("1000"),
                currency = currency,
                description = "1222211",
                images = listOf("http://localhost/item.jpg"),
                name = "test",
                quantity = 2,
            )
        )


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

        var testRequestPaymentMethod = RequestPaymentMethod(
            "card",
            testBilling,
            testCard
        )

        //create payment
        var testRequestPayment = RequestCreatePayment(
            amount = BigInteger("1000"),
            confirmationMethod = RequestCreatePayment.ConfirmationMethod.Manual,
            currency = currency,
            customerId = customerId,
            description = "2333233",
            ip = "192.168.0.12",
            lineItems = lineItems,
            notifyUrl = "http://localhost:8080/notify",
            orderNo = "1",
            paymentMethod = paymentMethodId,
            preAuth = "n",
            receiptEmail = email,
            returnUrl = "http://localhost:8080/return",
            setupFutureUsage = "off",
            shipping = RequestCreatePayment.Shipping(
                address = address,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            ),
            statementDescriptor = "www.1 23",
            statementDescriptorSuffix = "AAAA",
            website = website
        )

        //confirm payment
        var testConfirmPayment = RequestConfirmPayment(
            amount = BigInteger("1000"),
            currency = currency,
            id = paymentId,
            ip = "192.168.0.12",
            notifyUrl = "http://localhost:8080/notify",
            paymentMethod = paymentMethodId,
            receiptEmail = email,
            returnUrl = "http://localhost:8080/return",
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
        var testBankIvList = arrayListOf(
            R.drawable.mc_card_visa,
            R.drawable.mc_card_mastercard,
            R.drawable.mc_card_ae,
            R.drawable.mc_card_jcb,
            R.drawable.mc_card_dinner,
            R.drawable.mc_card_discover,
            R.drawable.mc_card_maestro
        )
    }
}

