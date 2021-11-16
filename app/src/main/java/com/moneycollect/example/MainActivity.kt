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

    private val TAG: String = "MoneyCollect_MainActivity"

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

    private class ExamplesAdapter constructor(
        private val activity: Activity
    ) : RecyclerView.Adapter<ExamplesAdapter.ExamplesViewHolder>() {
        private val items = listOf(
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

        override fun onBindViewHolder(examplesViewHolder: ExamplesViewHolder, i: Int) {
            val itemView = examplesViewHolder.itemView
            (itemView as TextView).text = items[i].text
            itemView.setOnClickListener {
                activity.startActivity(Intent(activity, items[i].activityClass))
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        private data class Item constructor(val text: String, val activityClass: Class<*>)

        private class ExamplesViewHolder constructor(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView)
    }
}
