package com.moneycollect.example

import android.annotation.SuppressLint
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreateCustomer
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.example.utils.CheckoutCurrency
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
        var currency = CheckoutCurrency.USD.toString()

        //select All Payments of the Customer
        var customerId = "cus_1459080620409905154"

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

        var  testRequestPaymentMethod=RequestPaymentMethod(
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
            setupFutureUsage = "on",
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
            setupFutureUsage = "on",
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
    }
}
