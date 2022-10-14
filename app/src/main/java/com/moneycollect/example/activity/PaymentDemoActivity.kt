package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.content.Intent
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
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.MoneyCollectButtonViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.adapter.PaymentSelectAdapter
import com.moneycollect.example.databinding.ActivityPaymentDemoBinding
import com.moneycollect.example.utils.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class PaymentDemoActivity : BaseExampleActivity(), View.OnClickListener {

    private val TAG: String = "PaymentDemoActivity"
    // PaymentModel (PAY_LOCAL)  payment model,support save and pay
    var moneyCollectPaymentModel: MoneyCollectPaymentModel =
        MoneyCollectPaymentModel.PAY_LOCAL

    private var viewBinding: ActivityPaymentDemoBinding? = null
    private var backIconIv: ImageView? = null
    private var title: TextView? = null
    private var appBarLayout: AppBarLayout? = null

    private var paymentSheetCustomDemoAdapter: PaymentSheetCustomDemoAdapter? = null

    private var paymentSelectAdapter: PaymentSelectAdapter? = null

    private var checkedItem = ArrayList<PaymentSheetCustomDemoAdapter.Item>()

    //loading active
    private var isLoadingAnimStatus = false

    //paymentMethod for pay
    private var paymentMethod: PaymentMethod? = null

    //currentRequestCreatePayment for pay
    private var currentRequestCreatePayment: RequestCreatePayment? = null

    //currentRequestConfirmPayment for pay
    private var currentRequestConfirmPayment: RequestConfirmPayment? = null

    // Current Currency Unit
    val currencyUnit = TestRequestData.currency

    //return
    private var returnUrl:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = ActivityPaymentDemoBinding.inflate(layoutInflater)
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
        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
        val params = MoneyCollectButtonViewParams.Builder()
            .activity(this)
            .moneyCollectPaymentModel(moneyCollectPaymentModel)
            .build()
        viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewParams(params)

        val linearLayoutManager = LinearLayoutManager(this)
            .apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        paymentSheetCustomDemoAdapter =
            PaymentSheetCustomDemoAdapter(this@PaymentDemoActivity)
        viewBinding?.sheetExpandedMenuRl?.run {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = paymentSheetCustomDemoAdapter
            paymentSheetCustomDemoAdapter?.items?.let { checkedItem.addAll(it) }
        }
        paymentSheetCustomDemoAdapter?.setOnKotlinItemClickListener(object :
            PaymentSheetCustomDemoAdapter.IKotlinCustomItemClickListener {
            override fun onItemClickListener(position: Int) {
                if (!isLoadingAnimStatus){
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
                    reCalculCheckoutAmount()
                }
            }
        })
        reCalculCheckoutAmount()


        val paymentSelectAdapterLayoutManager = LinearLayoutManager(this)
            .apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        paymentSelectAdapter =
            PaymentSelectAdapter(this@PaymentDemoActivity)
        viewBinding?.selectPaymentRl?.run {
            setHasFixedSize(true)
            layoutManager = paymentSelectAdapterLayoutManager
            adapter = paymentSelectAdapter
            paymentSelectAdapter?.items?.let {
                paymentMethod=it[0]
                if (it[0].type?.equals(CheckoutCreditCardCurrency.CREDIT_CARD.code) == true){
                    viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.text=getString(R.string.payment_now_continue_str)
                }else{
                    viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.text=getString(R.string.payment_now_payment_str)
                }
            }
        }
        paymentSelectAdapter?.setOnKotlinItemClickListener(object :
            PaymentSelectAdapter.IKotlinCustomItemClickListener {
            @SuppressLint("SetTextI18n")
            override fun onItemClickListener(position: Int) {
                paymentSelectAdapter?.getItem(position)?.let {
                    if (!isLoadingAnimStatus && !(paymentMethod?.type.equals(it.type))) {
                        viewBinding?.paymentErrorMessageTv?.text = ""
                        paymentMethod = it
                        if (it.type?.equals(CheckoutCreditCardCurrency.CREDIT_CARD.code) == true) {
                            viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.text =
                                getString(R.string.payment_now_continue_str)
                        } else {
                            viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.text =
                                getString(R.string.payment_now_payment_str)
                        }
                        paymentSelectAdapter?.setCurrentPosition(position)
                    }
                }
            }
        })
    }


    override fun onClick(view: View?) {
        if (view != null) {
            //clear status
            viewBinding?.paymentErrorMessageTv?.text = ""
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                if (view.id == R.id.back_icon) {
                    finish()
                } else if (view.id == viewBinding?.paymentCheckoutBtn?.cardConfirmButton?.id) {
                    jumpToCreditCardPage()
                }
            }
        }
    }


    private fun jumpToCreditCardPage(){
        //build the data of RequestCreatePayment
        currentRequestCreatePayment = TestRequestData.testRequestPayment
        currentRequestConfirmPayment = TestRequestData.testConfirmPayment
        currentRequestCreatePayment?.lineItems = formatLineItems(checkedItem)
        val numAmount = BigDecimal(
            (viewBinding?.paymentCheckoutAmount?.text.toString()).replace(
                getCurrencyUnitTag(currencyUnit, this@PaymentDemoActivity),
                ""
            )
        )
        val numUnit = getCurrencyTransnum(currencyUnit)
        var amount = numAmount.multiply(numUnit)
        currentRequestCreatePayment?.amount = amount.toBigInteger()
        currentRequestConfirmPayment?.amount = amount.toBigInteger()
        // pay bill
        paymentMethod?.let {
            if (it.type.equals(CheckoutCreditCardCurrency.CREDIT_CARD.code)) {
                var intent = Intent(this, PayCardActivity::class.java)
                var bundle = Bundle()
                var testBankIvList = TestRequestData.testBankIvList
                var testRequestPaymentMethod = TestRequestData.testRequestPaymentMethod
                //pass currentPaymentModel
                bundle.putSerializable(
                    Constant.CURRENT_PAYMENT_MODEL,
                    MoneyCollectPaymentModel.PAY
                )
                //pass RequestCreatePayment
                bundle.putParcelable(
                    Constant.CREATE_PAYMENT_REQUEST_TAG,
                    currentRequestCreatePayment
                )
                //pass RequestConfirmPayment
                bundle.putParcelable(
                    Constant.CONFIRM_PAYMENT_REQUEST_TAG,
                    currentRequestConfirmPayment
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
                intent.putExtra(Constant.CURRENT_PAYMENT_BUNDLE, bundle)
                startActivityLauncher.launch(intent)
            } else {
                if (isLoadingAnimStatus) {
                    viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
                }
                isLoadingAnimStatus = true
                viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(false)
                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewContext(null)
                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewModel(
                    moneyCollectPaymentModel)
                viewBinding?.paymentCheckoutBtn?.showAnimByPaymentHolding()

                dealData(it)
            }
        }
    }


    /**
     * reCalcu Checkout Amount
     */
    @SuppressLint("SetTextI18n")
    fun reCalculCheckoutAmount() {
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
        df?.roundingMode = RoundingMode.HALF_UP
        viewBinding?.paymentCheckoutAmount?.text = getCurrencyUnitTag(currencyUnit,
            this@PaymentDemoActivity) + df?.format(
            amount)
    }


    /**
     * format for lineItems
     */
    private fun formatLineItems(arrayItem: ArrayList<PaymentSheetCustomDemoAdapter.Item>): List<RequestCreatePayment.LineItems>? {
        var lineItems = ArrayList<RequestCreatePayment.LineItems>()
        for (item in arrayItem) {
            item.amount?.let {
                val numAmount = BigDecimal(item.amount)
                val numUnit = getCurrencyTransnum(item.currency)
                var amount = numAmount.multiply(numUnit)
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

    /**
     * deal data
     */
    private fun dealData(paymentMethod: PaymentMethod) {
        TestRequestData.testRequestPaymentMethod?.let {
            val  requestPaymentMethod= RequestPaymentMethod(
                paymentMethod.type,
                RequestPaymentMethod.BillingDetails(
                    Address(
                        line1 = it.billingDetails?.address?.line1,
                        line2 = it.billingDetails?.address?.line2,
                        city = it.billingDetails?.address?.city,
                        state = it.billingDetails?.address?.state,
                        postalCode = it.billingDetails?.address?.postalCode,
                        country = it.billingDetails?.address?.country
                    ),
                    it.billingDetails?.email,
                    it.billingDetails?.firstName,
                    it.billingDetails?.lastName,
                    it.billingDetails?.phone
                ),
                null
            )
            createPaymentMethod(requestPaymentMethod)
        }
    }


    /**
     * create paymentMethod
     */
    private fun createPaymentMethod(requestPaymentMethod: RequestPaymentMethod) {
        if(TextUtils.isEmpty(requestPaymentMethod.type)){
            viewBinding?.paymentErrorMessageTv?.text = resources.getString(R.string.payment_method_type_empty_str)
            return
        }

        moneyCollect.createPaymentMethod(
            this@PaymentDemoActivity, requestPaymentMethod,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                    createPayment(result)
                }

                override fun onError(e: Exception) {
                    dealError(e)
                }
            })
    }

    /**
     * create payment
     */
    private fun createPayment(paymentMethod: PaymentMethod) {
        if (currentRequestCreatePayment == null){
            viewBinding?.paymentErrorMessageTv?.text = getString(R.string.request_create_payment_empty_str)
            return
        }
        currentRequestCreatePayment?.let {
            val requestCreatePayment = RequestCreatePayment(
                automaticPaymentMethods = it.automaticPaymentMethods,
                amount = it.amount,
                confirm = false,
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
                paymentMethodTypes = listOf("${paymentMethod.type}"),
                preAuth = it.preAuth,
                receiptEmail = it.receiptEmail,
                returnUrl = it.returnUrl,
                setupFutureUsage = it.setupFutureUsage,
                shipping = RequestCreatePayment.Shipping(
                    address =    Address(
                        line1 = paymentMethod.billingDetails?.address?.line1,
                        line2 = paymentMethod.billingDetails?.address?.line2,
                        city = paymentMethod.billingDetails?.address?.city,
                        state = paymentMethod.billingDetails?.address?.state,
                        postalCode = paymentMethod.billingDetails?.address?.postalCode,
                        country = paymentMethod.billingDetails?.address?.country
                    ),
                    firstName = it.shipping?.firstName,
                    lastName = it.shipping?.lastName,
                    phone = it.shipping?.phone
                ),
                statementDescriptor = it.statementDescriptor,
                statementDescriptorSuffix = it.statementDescriptorSuffix,
                userAgent = it.userAgent,
                website = it.website
            )

            if (TextUtils.isEmpty(requestCreatePayment.paymentMethod)) {
                viewBinding?.paymentErrorMessageTv?.setText(R.string.payment_method_empty_str)
                return
            }
            moneyCollect.createPayment(requestCreatePayment,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        confirmPayment(result)
                    }

                    override fun onError(e: Exception) {
                        dealError(e)
                    }
                })
        }
    }


    /**
     * confirm Payment
     */
    private fun confirmPayment(payment: Payment) {
        returnUrl = if (payment.paymentMethodTypes?.contains(CheckoutLocalCurrency.Atome.code) == true) {
            "asiabill://payment:8080/webpay?paymentMethod=" + payment.paymentMethod
        } else {
            payment.returnUrl
        }
        val requestConfirmPayment = RequestConfirmPayment(
            BigInteger.valueOf(payment.amount!!),
            payment.currency,
            payment.id,
            payment.ip,
            payment.notifyUrl,
            payment.paymentMethod,
            payment.receiptEmail,
            returnUrl,
            payment.setupFutureUsage,
            RequestConfirmPayment.Shipping(
                TestRequestData.address,
                TestRequestData.firstName,
                TestRequestData.lastName,
                TestRequestData.phone
            ),
            payment.website
        )
        moneyCollect.confirmPayment(requestConfirmPayment, payment.clientSecret,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {
                    dealResult(result)
                }

                override fun onError(e: java.lang.Exception) {
                    dealError(e)
                }
            }
        )
    }


    private fun dealError(e: Exception){
        isLoadingAnimStatus = false
        viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
        viewBinding?.paymentErrorMessageTv?.text = e.message
    }

    private fun dealResult(result: Payment) {
        //if nextAction object is not null and redirectToUrl address is not null, further 3 d verification
        if (result.nextAction != null) {
            if (!TextUtils.isEmpty(result.nextAction?.type)) {
                var redirectToUrl=result.nextAction?.redirectToUrl
                if (result.nextAction?.type?.equals(TestRequestData.weChatPayNextActionType) == true) {
                    redirectToUrl=result.nextAction?.wechatPayH5?.redirectToUrl
                }
                if (!TextUtils.isEmpty(redirectToUrl)) {
                    val intent = Intent(this@PaymentDemoActivity, ValidationLocalWebActivity::class.java)
                    intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl)
                    intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.id)
                    intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, result.clientSecret)
                    startActivityLauncher.launch(intent)
                } else {
                    viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_PENDING_MESSAGE
                }
            }else {
                viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_PENDING_MESSAGE
            }
        } else {
            if (result.status.equals(Constant.PAYMENT_SUCCEEDED)) {
                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewContext(null)
                viewBinding?.paymentCheckoutBtn?.setMoneyCollectButtonViewModel(moneyCollectPaymentModel)
                viewBinding?.paymentCheckoutBtn?.showAnimByPaymentCompleteAndRefresh()
            } else {
                viewBinding?.paymentCheckoutBtn?.stopPaymentAnim()
            }
            when (result.status) {
                Constant.PAYMENT_SUCCEEDED -> {
                }
                Constant.PAYMENT_FAILED -> {
                    viewBinding?.paymentErrorMessageTv?.text = result.errorMessage
                }
                Constant.PAYMENT_UN_CAPTURED -> {
                    viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_UN_CAPTURED_MESSAGE
                }
                Constant.PAYMENT_PENDING -> {
                    viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_PENDING_MESSAGE
                }
                Constant.PAYMENT_CANCELED -> {
                    viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_CANCELED_MESSAGE
                }
                else -> {
                    viewBinding?.paymentErrorMessageTv?.text = Constant.PAYMENT_PENDING_MESSAGE
                }
            }
        }
        isLoadingAnimStatus = false
        viewBinding?.paymentCheckoutBtn?.setCardConfirmButtonStatus(true)
    }

    @SuppressLint("LongLogTag")
    private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Constant.WEB_RESULT_CODE) {
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
                        viewBinding?.paymentErrorMessageTv?.text = resultStr
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
        private val activity: PaymentDemoActivity,
    ) : RecyclerView.Adapter<PaymentSheetCustomDemoAdapter.ExamplesViewHolder>() {

        private var itemClickListener: IKotlinCustomItemClickListener? = null

        var items = listOf(
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