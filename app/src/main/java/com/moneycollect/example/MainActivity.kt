package com.moneycollect.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.*
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.example.TestRequestData.Companion.clientSecret
import com.moneycollect.example.TestRequestData.Companion.customerId
import com.moneycollect.example.TestRequestData.Companion.paymentId
import com.moneycollect.example.TestRequestData.Companion.paymentMethodId
import com.moneycollect.example.TestRequestData.Companion.testBilling
import com.moneycollect.example.TestRequestData.Companion.testCard
import com.moneycollect.example.TestRequestData.Companion.testConfirmPayment
import com.moneycollect.example.TestRequestData.Companion.testCustomer
import com.moneycollect.example.TestRequestData.Companion.testRequestPayment
import com.moneycollect.example.activity.*
import com.moneycollect.example.databinding.ActivityMainBinding

class MainActivity : BaseExampleActivity() {

    private val TAG: String = "MC_MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val linearLayoutManager = LinearLayoutManager(this)
            .apply {
                orientation = LinearLayoutManager.VERTICAL
            }

        viewBinding.expandedMenu.run {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = ExamplesAdapter(this@MainActivity)
        }
    }

    private fun uploadDevice() {
        moneyCollect.uploadDevice(this,
            object : ApiResultCallback<Devide> {
                override fun onSuccess(result: Devide) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun createCustomer() {
        val requestCustomer = testCustomer
        moneyCollect.createCustomer(requestCustomer,
            object : ApiResultCallback<Customer> {
                override fun onSuccess(result: Customer) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun createPaymentMethod() {
        val createPaymentMethod = RequestPaymentMethod("card", testBilling, testCard)
        moneyCollect.createPaymentMethod(this, createPaymentMethod,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun attachPaymentMethod() {
        moneyCollect.attachPaymentMethod(paymentMethodId, customerId,
            object : ApiResultCallback<Any> {
                override fun onSuccess(result: Any) {
                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {
                    Log.e(TAG, e.toString())
                }
            })
    }

    fun selectAllPaymentMethods() {
        val customerId = customerId
        moneyCollect.selectAllPaymentMethods(customerId,
            object : ApiResultCallback<Any> {
                override fun onSuccess(result: Any) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun createPayment() {
        val createPayment = testRequestPayment
        moneyCollect.createPayment(createPayment,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun confirmPayment() {
        val confirmPayment = testConfirmPayment
        val clientSecret = clientSecret
        moneyCollect.confirmPayment(confirmPayment, clientSecret,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun retrievePayment() {
        val clientSecret = clientSecret
        moneyCollect.retrievePayment(paymentId, clientSecret,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    fun retrievePaymentMethod() {
        moneyCollect.retrievePaymentMethod(paymentMethodId,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {

                    Log.e(TAG, result.toString())
                }

                override fun onError(e: Exception) {

                    Log.e(TAG, e.toString())
                }
            })
    }

    private class ExamplesAdapter constructor(
        private val activity: Activity
    ) : RecyclerView.Adapter<ExamplesAdapter.ExamplesViewHolder>() {
        private val items = listOf(
            Item(
                activity.getString(R.string.payment_demo_example),
                PaymentDemoActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_local_demo_example),
                PaymentLocalDemoActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_sheet_demo_example),
                PaymentSheetDemoActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_sheet_demo_custom_example),
                PaymentSheetCustomDemoActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_card_paymentMethod_example),
                PaymentMethodExampleActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_card_create_customer_example),
                CreateCustomerActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_card_select_pm_list_example),
                SelectCustomerPaymentMethodListActivity::class.java
            ),
            Item(
                activity.getString(R.string.payment_card_payment_example),
                PaymentExampleActivity::class.java
            ), Item(
                activity.getString(R.string.payment_select_button_type_example),
                SelectButtonTypeActivity::class.java
            ), Item(
                activity.getString(R.string.payment_select_button_type_setting),
                SettingsActivity::class.java
            )
        )

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ExamplesViewHolder {
            val root = activity.layoutInflater
                .inflate(R.layout.item_main_list_layout, viewGroup, false)
            return ExamplesViewHolder(root)
        }

        override fun onBindViewHolder(examplesViewHolder: ExamplesViewHolder, position: Int) {
            val itemView = examplesViewHolder.itemView
            (itemView as TextView).text = items[position].text
            itemView.setOnClickListener {
                activity.startActivity(Intent(activity, items[position].activityClass))
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private data class Item(val text: String, val activityClass: Class<*>?)

        private class ExamplesViewHolder constructor(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView)
    }
}
