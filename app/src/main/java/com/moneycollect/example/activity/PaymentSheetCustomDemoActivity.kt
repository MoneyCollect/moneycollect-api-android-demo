package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.moneycollect.android.MoneyCollect
import com.moneycollect.android.MoneyCollectFactory
import com.moneycollect.android.model.MoneyCollectButtonViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityPaymentSheetCustomDemoBinding
import com.moneycollect.example.utils.*
import java.math.BigDecimal
import java.math.RoundingMode

class PaymentSheetCustomDemoActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG: String = "PaymentSheetCustomDemoActivity_PaymentResult"
    // PaymentModel (PAY)  payment model,support save and pay
    var moneyCollectPaymentModel: MoneyCollectPaymentModel =
        MoneyCollectPaymentModel.PAY

    // Current Currency Unit
    val currencyUnit = TestRequestData.currency

    val moneyCollect: MoneyCollect by lazy {
        MoneyCollectFactory(application).create()
    }
    private var viewBinding: ActivityPaymentSheetCustomDemoBinding? = null
    private var backIconIv: ImageView? = null
    private var title: TextView? = null
    private var appBarLayout: AppBarLayout? = null

    private var paymentSheetCustomDemoAdapter: PaymentSheetCustomDemoAdapter? = null

    private var checkedItem = ArrayList<PaymentSheetCustomDemoAdapter.Item>()

    //loading active
    private var isLoadingAnimStatus = false

    //paymentMethod for pay
    private var paymentMethod: PaymentMethod? = null

    //currentRequestCreatePayment for pay
    private var currentRequestCreatePayment: RequestCreatePayment? = null

    //currentRequestConfirmPayment for pay
    private var currentRequestConfirmPayment: RequestConfirmPayment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = ActivityPaymentSheetCustomDemoBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        initUI()
    }

    private fun initUI() {
        appBarLayout = viewBinding!!.appBar
        title = viewBinding!!.title
        backIconIv = viewBinding!!.backIcon
        backIconIv!!.setOnClickListener(this)
        viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.setOnClickListener(this)
        val params = MoneyCollectButtonViewParams.Builder()
            .activity(this)
            .moneyCollectPaymentModel(moneyCollectPaymentModel)
            .build()
        viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewParams(params)
        viewBinding?.sheetPaymentMethodSelectCl?.setOnClickListener(this)

        val linearLayoutManager = LinearLayoutManager(this)
            .apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        paymentSheetCustomDemoAdapter =
            PaymentSheetCustomDemoAdapter(this@PaymentSheetCustomDemoActivity)
        viewBinding?.sheetExpandedMenuRl?.run {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = paymentSheetCustomDemoAdapter
            paymentSheetCustomDemoAdapter?.items?.let { checkedItem.addAll(it) }
        }
        paymentSheetCustomDemoAdapter?.setOnKotlinItemClickListener(object :
            PaymentSheetCustomDemoAdapter.IKotlinCustomItemClickListener {
            override fun onItemClickListener(position: Int) {
                paymentSheetCustomDemoAdapter?.getItem(position)?.checked =
                    paymentSheetCustomDemoAdapter?.getItem(position)?.checked != true
                paymentSheetCustomDemoAdapter?.notifyDataSetChanged()
                paymentSheetCustomDemoAdapter?.getItem(position)?.let {
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

    override fun onClick(view: View?) {
        if (view != null) {
            //clear status
            viewBinding?.paymentErrorMessageTv?.setText("")
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                if (view.id == R.id.back_icon) {
                    finish()
                } else if (view.id == viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.id) {

                    if (isLoadingAnimStatus) {
                        viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                    }
                    isLoadingAnimStatus = true
                    viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(false)
                    viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewContext(null)
                    viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewModel(
                        moneyCollectPaymentModel)
                    viewBinding?.paymentCheckoutBtn?.showAnimByPaymentHolding()

                    //build the data of RequestCreatePayment
                    currentRequestCreatePayment = TestRequestData.testRequestPayment
                    currentRequestConfirmPayment = TestRequestData.testConfirmPayment
                    currentRequestCreatePayment?.lineItems = formatlineItems(checkedItem)
                    val numamount = BigDecimal(
                        (viewBinding?.paymentCheckoutAmount?.text.toString()).replace(
                            getCurrencyUnitTag(currencyUnit, this@PaymentSheetCustomDemoActivity),
                            ""
                        )
                    )
                    val numUnit = getCurrencyTransnum(currencyUnit)
                    var amount = numamount.multiply(numUnit)
                    currentRequestCreatePayment?.amount = amount.toBigInteger();
                    currentRequestConfirmPayment?.amount = amount.toBigInteger();
                    // pay bill
                    paymentMethod?.let { createPayment(it) }
                } else if (view.id == R.id.sheet_payment_method_select_cl) {
                    // jump the list of card
                    var intent = Intent(this, SaveCardActivity::class.java)
                    var bundle = Bundle()
                    var customerId = TestRequestData.customerId
                    var testRequestPaymentMethod = TestRequestData.testRequestPaymentMethod
                    var testBankIvList = TestRequestData.testBankIvList
                    //pass currentPaymentModel
                    bundle.putSerializable(
                        Constant.CURRENT_PAYMENT_MODEL,
                        MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD
                    )
                    //pass customerId
                    bundle.putString(
                        Constant.CUSTOMER_ID_TAG,
                        customerId
                    )
                    //pass RequestPaymentMethod
                    bundle?.putParcelable(
                        Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG,
                        testRequestPaymentMethod
                    )
                    //pass supportBankList
                    bundle?.putSerializable(
                        Constant.SUPPORT_BANK_LIST_TAG,
                        testBankIvList
                    )
                    intent.putExtra(Constant.CURRENT_PAYMENT_BUNDLE, bundle)
                    startActivityLauncher.launch(intent)
                }
            }
        }
    }


    /**
     * reCalcu Checkout Amount
     */
    fun reCalcuCheckoutAmount() {
        var amount =
            getCurrencyZeroDecimalFormat(currencyUnit)
        var amountItem: BigDecimal;
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
            this@PaymentSheetCustomDemoActivity) + df?.format(
            amount))
    }


    /**
     * format for lineItems
     */
    private fun formatlineItems(arrayItem: ArrayList<PaymentSheetCustomDemoAdapter.Item>): List<RequestCreatePayment.LineItems>? {
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

    private fun createPayment(
        paymentMethod: PaymentMethod,
    ) {
        currentRequestCreatePayment?.let {
            val requestCreatePayment = RequestCreatePayment(
                amount = it.amount,
                confirmationMethod = it.confirmationMethod,
                currency = it.currency,
                customerId = it.customerId,
                description = it.description,
                fromChannel = it.fromChannel,
                ip = it.ip,
                lineItems = it.lineItems,
                notifyUrl = it.notifyUrl,
                orderNo = it.orderNo,
                paymentMethod = paymentMethod.id,
                preAuth = it.preAuth,
                receiptEmail = it.receiptEmail,
                returnUrl = it.returnUrl,
//                setupFutureUsage = "",
                shipping = RequestCreatePayment.Shipping(
                    it.shipping?.address,
                    firstName = it.shipping?.firstName,
                    lastName = it.shipping?.lastName,
                    phone = it.shipping?.phone
                ),
                statementDescriptor = it.statementDescriptor,
                statementDescriptorSuffix = it.statementDescriptorSuffix,
                userAgent = it.userAgent,
                website = it.website
            )

            moneyCollect.createPayment(requestCreatePayment,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        confirmPayment(result, paymentMethod)
                    }

                    override fun onError(e: Exception) {
                        isLoadingAnimStatus = false
                        viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
                        viewBinding?.paymentErrorMessageTv?.visibility == View.VISIBLE
                        viewBinding?.paymentErrorMessageTv?.setText(e.message)
                    }
                })
        }
    }


    private fun confirmPayment(payment: Payment, paymentMethod: PaymentMethod) {
        currentRequestConfirmPayment?.let {
            val requestConfirmPayment = RequestConfirmPayment(
                amount = it.amount,
                currency = payment.currency,
                id = payment.id,
                ip = payment.ip,
                notifyUrl = payment.notifyUrl,
                paymentMethod = payment.paymentMethod,
                receiptEmail = payment.receiptEmail,
                returnUrl = payment.returnUrl,
                setupFutureUsage = payment.setupFutureUsage,
                shipping = RequestConfirmPayment.Shipping(
                    it.shipping?.address,
                    firstName = it.shipping?.firstName,
                    lastName = it.shipping?.lastName,
                    phone = it.shipping?.phone
                ),
                website = it.website
            )

            moneyCollect.confirmPayment(requestConfirmPayment, payment.clientSecret,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        isLoadingAnimStatus = false
                        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
                        //	Status: succeeded,uncaptured,pending,failed,canceled
                        if (result.nextAction != null) {
                            if (!TextUtils.isEmpty(result.nextAction?.redirectToUrl)) {
                                val intent = Intent(
                                    this@PaymentSheetCustomDemoActivity,
                                    ValidationWebActivity::class.java
                                )
                                intent.putExtra(
                                    Constant.VALIDATION_PARAM_URL,
                                    result.nextAction?.redirectToUrl
                                )
                                intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.id)
                                intent.putExtra(
                                    Constant.VALIDATION_PAYMENT_CLIENTSECRET,
                                    result.clientSecret
                                )
                                startActivityLauncher.launch(intent)
                            } else {
                                viewBinding?.paymentErrorMessageTv?.setText(Constant.PAYMENT_PENDING_MESSAGE)
                            }
                        } else {
                            if (result.status.equals(Constant.PAYMENT_SUCCEEDED)) {
                                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewContext(
                                    null)
                                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewModel(
                                    moneyCollectPaymentModel)
                                viewBinding?.paymentCheckoutBtn?.showAnimByPaymentCompleteAndRefresh()
                            } else {
                                viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                            }
                            when (result.status) {

                                Constant.PAYMENT_SUCCEEDED -> {
                                }
                                Constant.PAYMENT_FAILED -> {
                                    viewBinding?.paymentErrorMessageTv?.setText(result.errorMessage)
                                }
                                Constant.PAYMENT_UN_CAPTURED -> {
                                    viewBinding?.paymentErrorMessageTv?.setText(Constant.PAYMENT_UN_CAPTURED_MESSAGE)
                                }
                                Constant.PAYMENT_PENDING -> {
                                    viewBinding?.paymentErrorMessageTv?.setText(Constant.PAYMENT_PENDING_MESSAGE)
                                }
                                Constant.PAYMENT_CANCELED -> {
                                    viewBinding?.paymentErrorMessageTv?.setText(Constant.PAYMENT_CANCELED_MESSAGE)
                                }
                                else -> {
                                    viewBinding?.paymentErrorMessageTv?.setText(Constant.PAYMENT_PENDING_MESSAGE)
                                }
                            }
                        }
                    }

                    override fun onError(e: Exception) {
                        isLoadingAnimStatus = false
                        viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
                        viewBinding?.paymentErrorMessageTv?.visibility == View.VISIBLE
                        viewBinding?.paymentErrorMessageTv?.setText(e.message)
                    }
                })
        }
    }

    private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //selected paymentmethods's list
            if (it.resultCode == Constant.SAVE_RESULT_CODE) {
                paymentMethod =
                    it.data?.getParcelableExtra<PaymentMethod>(Constant.SAVE_PAYMENT_METHOD)
                if (paymentMethod != null) {
                    viewBinding?.sheetPaymentMethodSelectTv?.setTextColor(
                        this@PaymentSheetCustomDemoActivity.resources.getColor(
                            R.color.color_333333
                        )
                    )
                    viewBinding?.sheetPaymentMethodSelectIv?.visibility = View.VISIBLE
                    setCardImg(
                        this,
                        viewBinding?.sheetPaymentMethodSelectIv,
                        paymentMethod?.card?.brand
                    )
                    viewBinding?.sheetPaymentMethodSelectTv?.setText(
                        String.format(
                            " ···· %s",
                            paymentMethod?.card?.last4
                        )
                    )
                }
                //add a paymentmethod
            } else if (it.resultCode == Constant.ADD_RESULT_CODE) {
                paymentMethod =
                    it.data?.getParcelableExtra<PaymentMethod>(Constant.ADD_PAYMENT_METHOD)
                if (paymentMethod != null) {
                    viewBinding?.sheetPaymentMethodSelectTv?.setTextColor(
                        this@PaymentSheetCustomDemoActivity.resources.getColor(
                            R.color.color_333333
                        )
                    )
                    viewBinding?.sheetPaymentMethodSelectIv?.visibility = View.VISIBLE
                    setCardImg(
                        this,
                        viewBinding?.sheetPaymentMethodSelectIv,
                        paymentMethod?.card?.brand
                    )
                    viewBinding?.sheetPaymentMethodSelectTv?.setText(
                        String.format(
                            " ···· %s",
                            paymentMethod?.card?.last4
                        )
                    )
                }
            } else if (it.resultCode == Constant.WEB_RESULT_CODE) {
                //3D Secure authentication result
                var resultStr = it.data?.getStringExtra(Constant.WEB_RESULT_TAG)
                if (resultStr != null) {
                    if (TextUtils.isEmpty(resultStr)) {
                        viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewContext(
                            null)
                        viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewModel(
                            moneyCollectPaymentModel)
                        viewBinding?.paymentCheckoutBtn?.showAnimByPaymentCompleteAndRefresh()
                    } else {
                        isLoadingAnimStatus = false
                        viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
                        viewBinding?.paymentErrorMessageTv?.visibility == View.VISIBLE
                        viewBinding?.paymentErrorMessageTv?.setText(resultStr)
                    }
                }
                //3D Secure authentication resultPayment
                var payment =
                    it.data?.getParcelableExtra<Payment>(Constant.PAYMENT_RESULT_PAYMENT)
                if (payment != null) {
                    when (payment.status) {
                        Constant.PAYMENT_SUCCEEDED -> {
                            Log.e(TAG, Constant.PAYMENT_SUCCESSFUL_MESSAGE)
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
            if (paymentMethod != null) {
                viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
            }
        }


    private class PaymentSheetCustomDemoAdapter constructor(
        private val activity: PaymentSheetCustomDemoActivity,
    ) : RecyclerView.Adapter<PaymentSheetCustomDemoAdapter.ExamplesViewHolder>() {

        private var itemClickListener: IKotlinCustomItemClickListener? = null

        var items = listOf(Item(
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
                "110.00",
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
                "38.00",
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

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(examplesViewHolder: ExamplesViewHolder, i: Int) {
            val itemView = examplesViewHolder.itemView
            items[i].images?.let {
                itemView.findViewById<ImageView>(R.id.item_payment_goods_iv)
                    .setImageResource(it)
            }
            itemView.findViewById<TextView>(R.id.item_payment_goods_name_tv).text = items[i].name
            itemView.findViewById<TextView>(R.id.item_payment_goods_price_tv).text =
                getCurrencyUnitTag(activity.currencyUnit, activity) + getAmountTransferNum(activity.currencyUnit,items[i]?.amount)
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

        // set
        fun setOnKotlinItemClickListener(itemClickListener: IKotlinCustomItemClickListener) {
            this.itemClickListener = itemClickListener
        }

        /**
         *    interface custom
         */
        interface IKotlinCustomItemClickListener {
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