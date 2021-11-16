package com.moneycollect.example.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.Constant
import com.moneycollect.example.Constant.Companion.CURRENT_PAYMENT_BUNDLE
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityPaymentSheetDemoBinding
import com.moneycollect.example.utils.*
import com.moneycollect.example.utils.getCurrencyDecimalFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class PaymentSheetDemoActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG: String = "PaymentSheetDemoActivity_PaymentResult"

    // PaymentModel (PAY)  payment model,support save and pay
    var currentPaymentModel: MoneyCollectPaymentModel =
        MoneyCollectPaymentModel.PAY

    // Current Currency Unit
    val currencyUnit = TestRequestData.currency
    private var viewBinding: ActivityPaymentSheetDemoBinding? = null
    private var backIconIv: ImageView? = null
    private var title: TextView? = null
    private var appBarLayout: AppBarLayout? = null
    private var paymentSheetDemoAdapter: PaymentSheetDemoAdapter? = null
    private var checkedItem = ArrayList<PaymentSheetDemoAdapter.Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = ActivityPaymentSheetDemoBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        initUI()
    }

    private fun initUI() {
        appBarLayout = viewBinding!!.appBar
        title = viewBinding!!.title
        backIconIv = viewBinding!!.backIcon
        backIconIv!!.setOnClickListener(this)
        viewBinding?.paymentCancelBtn?.setOnClickListener(this)
        viewBinding?.paymentCheckoutBtn?.setOnClickListener(this)
        val linearLayoutManager = LinearLayoutManager(this)
            .apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        paymentSheetDemoAdapter = PaymentSheetDemoAdapter(this@PaymentSheetDemoActivity)
        viewBinding?.sheetExpandedMenuRl?.run {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = paymentSheetDemoAdapter
            paymentSheetDemoAdapter?.items?.let { checkedItem.addAll(it) }

        }
        paymentSheetDemoAdapter?.setOnKotlinItemClickListener(object :
            PaymentSheetDemoAdapter.IKotlinItemClickListener {
            override fun onItemClickListener(position: Int) {
                paymentSheetDemoAdapter?.getItem(position)?.checked =
                    paymentSheetDemoAdapter?.getItem(position)?.checked != true
                paymentSheetDemoAdapter?.notifyDataSetChanged()
                paymentSheetDemoAdapter?.getItem(position)?.let {
                    if (checkedItem.contains(it)) {
                        checkedItem.remove(it)
                    } else {
                        checkedItem.add(it)
                    }
                }
                reCalcuCheckoutAmount()
            }
        })
        reCalcuCheckoutAmount()
    }

    /**
     * reCalcu Checkout Amount
     */
    fun reCalcuCheckoutAmount() {
        var amount =
            getCurrencyZeroDecimalFormat(currencyUnit)
        var amountItem: BigDecimal
        for (item in checkedItem) {
            item?.amount?.let {
                amountItem = BigDecimal(it)
                amount = amount.add(amountItem)
            }
        }
        val df = currencyUnit?.let {
            getCurrencyDecimalFormat(it)
        }
        df?.setRoundingMode(RoundingMode.HALF_UP)
        viewBinding?.paymentCheckoutAmount?.setText(getCurrencyUnitTag(currencyUnit,
            this@PaymentSheetDemoActivity) + df?.format(
            amount))
    }


    /**
     * format for lineItems
     */
    private fun formatlineItems(arrayItem: ArrayList<PaymentSheetDemoAdapter.Item>): List<RequestCreatePayment.LineItems>? {
        var lineItems = ArrayList<RequestCreatePayment.LineItems>()
        for (item in arrayItem) {
            item.amount?.let {
                val numamount = BigDecimal(item.amount)
                val numUnit = getCurrencyTransnum(item.currency)
                var amount = numamount.multiply(numUnit)
                var lineItem = RequestCreatePayment.LineItems(
                    amount = amount.toBigInteger(),
                    currency = item.currency,
                    description = item.name,
                    images = listOf(item.images.toString()),
                    name = item.name,
                    quantity = item.quantity,
                )
                lineItems.add(lineItem)
            }
        }
        return lineItems
    }

    override fun onClick(view: View?) {
        if (view != null) {
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                if (view.id == R.id.back_icon || view.id == R.id.payment_cancel_btn) {
                    finish()
                } else if (view.id == R.id.payment_checkout_btn) {
                    var intent = Intent(this, PayCardActivity::class.java)
                    var bundle = Bundle()
                    var testRequestPayment = TestRequestData.testRequestPayment
                    var testConfirmPayment = TestRequestData.testConfirmPayment
                    var testRequestPaymentMethod = TestRequestData.testRequestPaymentMethod
                    var testBankIvList = TestRequestData.testBankIvList
                    testRequestPayment.lineItems = formatlineItems(checkedItem)
                    val numamount = BigDecimal(
                        (viewBinding?.paymentCheckoutAmount?.text.toString()).replace(
                            getCurrencyUnitTag(currencyUnit, this@PaymentSheetDemoActivity),
                            ""
                        )
                    )
                    val numUnit = getCurrencyTransnum(currencyUnit)
                    var amount = numamount.multiply(numUnit)
                    testRequestPayment.amount = amount.toBigInteger();
                    testConfirmPayment.amount = amount.toBigInteger();
                    //pass currentPaymentModel
                    bundle.putSerializable(
                        Constant.CURRENT_PAYMENT_MODEL,
                        currentPaymentModel
                    )
                    //pass RequestCreatePayment
                    bundle.putParcelable(
                        Constant.CREATE_PAYMENT_REQUEST_TAG,
                        testRequestPayment
                    )
                    //pass RequestConfirmPayment
                    bundle.putParcelable(
                        Constant.CONFIRM_PAYMENT_REQUEST_TAG,
                        testConfirmPayment
                    )
                    //pass currentId
                    bundle.putString(
                        Constant.CUSTOMER_ID_TAG,
                        TestRequestData.customerId
                    )
                    //pass default RequestPaymentMethod
                    bundle?.putParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG,
                        testRequestPaymentMethod)
                    //pass default supportBankList
                    bundle?.putSerializable(Constant.SUPPORT_BANK_LIST_TAG, testBankIvList)
                    intent.putExtra(CURRENT_PAYMENT_BUNDLE, bundle)
                    startActivityLauncher.launch(intent)
                }
            }
        }
    }

    private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Constant.PAYMENT_RESULT_CODE) {
                // resultPayment
                var payment =
                    it.data?.getParcelableExtra<Payment>(Constant.PAYMENT_RESULT_PAYMENT)
                if (payment != null) {
                    when (payment.status) {
                        Constant.PAYMENT_SUCCEEDED -> {
                            Log.e(TAG, Constant.PAYMENT_SUCCEEDED)
                        }
                        Constant.PAYMENT_FAILED -> {
                            payment?.errorMessage?.let { it1 ->
                                Log.e(TAG, it1)
                            }
                        }
                        Constant.PAYMENT_UN_CAPTURED -> {
                            Log.e(TAG, Constant.PAYMENT_UN_CAPTURED_MESSAGE)
                        }
                        Constant.PAYMENT_PENDING -> {
                            Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE)
                        }
                        Constant.PAYMENT_CANCELED -> {
                            Log.e(TAG, Constant.PAYMENT_CANCELED_MESSAGE)
                        }
                        else -> {
                            Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE)
                        }
                    }
                }
            }

        }


    private class PaymentSheetDemoAdapter constructor(
        private val activity: PaymentSheetDemoActivity,
    ) : RecyclerView.Adapter<PaymentSheetDemoAdapter.ExamplesViewHolder>() {

        private var itemClickListener: IKotlinItemClickListener? = null

        var items = listOf(
            Item(
                R.mipmap.icon_payment_goods_one,
                "Waterproof Smartwatch A5",
                "109.00",
                activity.currencyUnit,
                "Waterproof Smartwatch A5",
                1,
                true
            ),
            Item(
                R.mipmap.icon_payment_goods_two,
                "GPS Smartwatch T3",
                "11069.00",
                activity.currencyUnit,
                "Waterproof Smartwatch A5",
                1,
                true
            ),
            Item(
                R.mipmap.icon_payment_goods_three,
                "GPS Smartwatch T2",
                "59.00",
                activity.currencyUnit,
                "Waterproof Smartwatch A5",
                1,
                true
            ),
            Item(
                R.mipmap.icon_payment_goods_four,
                "Waterproof Smartwatch A6",
                "385.00",
                activity.currencyUnit,
                "Waterproof Smartwatch A5",
                1,
                true
            )
        )


        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ExamplesViewHolder {
            val root = activity.layoutInflater
                .inflate(R.layout.item_payment_sheet_goods, viewGroup, false)
            return ExamplesViewHolder(root)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onBindViewHolder(examplesViewHolder: ExamplesViewHolder, i: Int) {
            val itemView = examplesViewHolder.itemView
            items[i].images?.let {
                itemView.findViewById<ImageView>(R.id.item_payment_goods_iv)
                    .setImageResource(it)
            }
            itemView.findViewById<TextView>(R.id.item_payment_goods_name_tv)
                .setText(items[i].name)
            itemView.findViewById<TextView>(R.id.item_payment_goods_price_tv)
                .setText(getCurrencyUnitTag(activity.currencyUnit, activity) + items[i]?.amount)
            if (items[i].checked == true) {
                itemView.findViewById<TextView>(R.id.item_payment_goods_amount_tv)
                    .setTextColor(activity.resources.getColor(R.color.color_1A73E8))
            } else {
                itemView.findViewById<TextView>(R.id.item_payment_goods_amount_tv)
                    .setTextColor(activity.resources.getColor(R.color.color_333333))
            }

            // click event
            itemView.setOnClickListener {
                itemClickListener!!.onItemClickListener(i)
            }
        }

        fun getItem(i: Int): Item {
            if (i <= items.size) {
                return items[i]
            }
            return Item()
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun setOnKotlinItemClickListener(itemClickListener: IKotlinItemClickListener) {
            this.itemClickListener = itemClickListener
        }

        /**
         *    interface custom
         */
        interface IKotlinItemClickListener {
            fun onItemClickListener(position: Int)
        }

        data class Item constructor(
            // A list of up to 5 URLs of images for this line item, meant to be displayable to the customer.
            var images: Int? = null,
            //nameTv The product’s name, meant to be displayable to the customer
            var name: String? = null,
            //priceTv The amount to be collected per unit of the line item
            var amount: String? = null,
            //currency	Three-letter ISO currency code.
            var currency: String? = null,
            //description The description for the line item, to be displayed on the Checkout page.
            var description: String? = null,
            //quantity
            var quantity: Int? = null,
            //item is checked  /yes:true   no:false
            var checked: Boolean? = false,
        )

        private class ExamplesViewHolder constructor(
            itemView: View,
        ) : RecyclerView.ViewHolder(itemView)
    }
}