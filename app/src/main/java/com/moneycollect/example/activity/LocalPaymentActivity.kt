package com.moneycollect.example.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.MoneyCollectCardListViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface
import com.moneycollect.android.ui.view.MoneyCollectCardListView
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.TestRequestData.Companion.address
import com.moneycollect.example.TestRequestData.Companion.firstName
import com.moneycollect.example.TestRequestData.Companion.lastName
import com.moneycollect.example.TestRequestData.Companion.phone
import com.moneycollect.example.databinding.ActivityLocalPaymentLayoutBinding
import com.moneycollect.example.utils.CheckoutLocalCurrency
import java.math.BigInteger

class LocalPaymentActivity : BaseExampleActivity(),View.OnClickListener{

    private var viewBinding: ActivityLocalPaymentLayoutBinding?=null

    /***  [MoneyCollectCardListView]*/
    var cardListLayout: MoneyCollectCardListView?=null

    // PaymentModel (ATTACH_PAYMENT_METHOD)
    private var currentModel: MoneyCollectPaymentModel = MoneyCollectPaymentModel.PAY_LOCAL

    /***  [MoneyCollectResultBackInterface]*/
    private var  moneyCollectResultBackInterface: MoneyCollectResultBackInterface?=null

    //customerId
    var customerId: String?=null

    //RequestCreatePayment params for pay
    private var currentRequestCreatePayment: RequestCreatePayment?=null

    //RequestPaymentMethod params for pay
    private var currentRequestCreatePaymentMethod:RequestPaymentMethod?=null

    //groupList params
    private var groupList: ArrayList<PaymentMethod> = ArrayList<PaymentMethod>()
    //childList params
    private var childList: ArrayList<ArrayList<PaymentMethod>> = ArrayList<ArrayList<PaymentMethod>>()

    //loading active
    private var isRequestLoading=false

    //return
    private var returnUrl:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = ActivityLocalPaymentLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        initUi()
    }

    private fun initUi() {
        val bundle = intent?.extras?.getBundle(Constant.CURRENT_PAYMENT_BUNDLE)
        bundle?.let {
            currentModel = it.getSerializable(Constant.CURRENT_PAYMENT_MODEL) as MoneyCollectPaymentModel
            currentRequestCreatePayment = it.getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG)
            currentRequestCreatePaymentMethod = it.getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG)
            customerId = it.getString(Constant.CUSTOMER_ID_TAG)
        }

        cardListLayout= viewBinding?.mcCardList
        moneyCollectResultBackInterface=cardListLayout?.gainMoneyCollectResultBackInterface()
        val moneyCollectCardListViewParams = MoneyCollectCardListViewParams.Builder()
            .activity(LocalPaymentActivity@ this)
            .moneyCollectPaymentModel(currentModel)
            .addClickListener(this)
            .build()
        cardListLayout?.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams)


        //Listening to pay button return data, further request
        cardListLayout?.paymentMethodParamsLiveData?.observe(LocalPaymentActivity@ this
        ) { result ->
            result.apply {
                if (result?.type != null && !TextUtils.isEmpty(result.type)) {
                    dealData(result)
                } else {
                    moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_method_list_empty_str))
                }
            }
        }

        selectAllPaymentMethods()
    }

    /**
     * deal data
     */
    private fun dealData(paymentMethod: PaymentMethod) {
        currentRequestCreatePaymentMethod?.let {
            val  requestPaymentMethod=RequestPaymentMethod(
                paymentMethod.type,
                RequestPaymentMethod.BillingDetails(
                    Address(
                        line1 = it.billingDetails?.address?.line1,
                        line2 = it.billingDetails?.address?.line2,
                        city = it.billingDetails?.address?.city,
                        state = it.billingDetails?.address?.state,
                        postalCode = it.billingDetails?.address?.postalCode,
                        country = it.billingDetails?.address?.country,
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
            moneyCollectResultBackInterface?.failExceptionBack(resources.getString(R.string.payment_method_type_empty_str))
            return
        }

        moneyCollect.createPaymentMethod(
            LocalPaymentActivity@ this, requestPaymentMethod,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                    createPayment(result)
                }

                override fun onError(e: Exception) {
                    moneyCollectResultBackInterface?.failExceptionBack(e.message)
                }
            })
    }


    /**
     * create payment
     */
    private fun createPayment(paymentMethod: PaymentMethod) {
        if (currentRequestCreatePayment == null){
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.request_create_payment_empty_str))
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
                        country = paymentMethod.billingDetails?.address?.country,
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
            if (requestCreatePayment.confirmationMethod == RequestCreatePayment.ConfirmationMethod.Automatic) {
                if (TextUtils.isEmpty(requestCreatePayment.paymentMethod)) {
                    moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_method_empty_str))
                    return
                }
            }
            moneyCollect.createPayment(requestCreatePayment,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        confirmPayment(result)
                    }

                    override fun onError(e: Exception) {
                        moneyCollectResultBackInterface?.failExceptionBack(e.message)
                    }
                })
        }
    }

    /**
     * confirm Payment
     */
    private fun confirmPayment(payment: Payment) {
        if (TextUtils.isEmpty(payment.paymentMethod)) {
            moneyCollectResultBackInterface!!.failExceptionBack(getString(R.string.payment_method_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment.id)) {
            moneyCollectResultBackInterface!!.failExceptionBack(getString(R.string.payment_id_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment.clientSecret)) {
            moneyCollectResultBackInterface!!.failExceptionBack(getString(R.string.payment_client_secret_empty_str))
            return
        }

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
                address,
                firstName,
                lastName,
                phone
            ),
            payment.website
        )
        moneyCollect.confirmPayment(requestConfirmPayment, payment.clientSecret,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {
                    dealResult(result)
                }

                override fun onError(e: java.lang.Exception) {
                    moneyCollectResultBackInterface!!.failExceptionBack(e.message)
                }
            }
        )
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
                    val intent = Intent(LocalPaymentActivity@ this, ValidationLocalWebActivity::class.java)
                    intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl)
                    intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.id)
                    intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, result.clientSecret)
                    startActivityLauncher.launch(intent)
                } else {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        Constant.PAYMENT_PENDING_MESSAGE)
                }
            }else {
                moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                    Constant.PAYMENT_PENDING_MESSAGE)
            }
        } else {
            //Need to deal with the state has succeeded, uncaptured, pending, failed, canceled
            when (result.status) {
                Constant.PAYMENT_SUCCEEDED -> {
                    var intent = Intent()
                    intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, result)
                    setResult(Constant.PAYMENT_RESULT_CODE, intent)
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(true,
                        "")
                }
                Constant.PAYMENT_FAILED -> {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        result.errorMessage)
                }
                Constant.PAYMENT_UN_CAPTURED -> {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        Constant.PAYMENT_UN_CAPTURED_MESSAGE)
                }
                Constant.PAYMENT_PENDING -> {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        Constant.PAYMENT_PENDING_MESSAGE)
                }
                Constant.PAYMENT_CANCELED -> {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        Constant.PAYMENT_CANCELED_MESSAGE)
                }
                else -> {
                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                        Constant.PAYMENT_PENDING_MESSAGE)
                }
            }
        }
    }

    private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Constant.WEB_RESULT_CODE) {
                var resultStr = it.data?.getStringExtra(Constant.WEB_RESULT_TAG)
                var payment =
                    it.data?.getParcelableExtra<Payment>(Constant.PAYMENT_RESULT_PAYMENT)
                if (resultStr != null) {
                    if (TextUtils.isEmpty(resultStr)){
                        val intent =  Intent()
                        intent.putExtra(Constant.WEB_RESULT_TAG, resultStr)
                        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, payment)
                        setResult(Constant.PAYMENT_RESULT_CODE, intent)
                        moneyCollectResultBackInterface?.paymentConfirmResultBack(true, "")
                    }else{
                        moneyCollectResultBackInterface?.failExceptionBack(resultStr)
                    }
                }
            }
        }

    /**
     * select all paymentMethods
     */
    private fun selectAllPaymentMethods() {
        cardListLayout?.visibility= View.VISIBLE
        childList.add(TestRequestData.testLocalBankList)
        when {
            childList.isNotEmpty() -> {
                cardListLayout?.setPaymentButtonAndFootViewStatus(true)
                groupList.add(childList[0][0])
                cardListLayout?.setDataList(groupList, childList)
            }
            else -> {
                cardListLayout?.changeFootViewVisible(true)
            }
        }
    }


    companion object{
        const val ADD_PAYMENT:String="AddPaymentFragment"  //add fragment tag
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
            if (view.id == cardListLayout?.getToolbarBackIcon()?.id && cardListLayout?.isRequestLoading==false && !isRequestLoading) {
                finish()
            }
        }
    }
}