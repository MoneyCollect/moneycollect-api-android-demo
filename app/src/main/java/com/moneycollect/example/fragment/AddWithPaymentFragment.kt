package com.moneycollect.example.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.moneycollect.android.model.MoneyCollectContentResultParams
import com.moneycollect.android.model.MoneyCollectContentViewParams
import com.moneycollect.android.model.MoneyCollectNameParams
import com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface
import com.moneycollect.android.ui.view.MoneyCollectContentView
import com.moneycollect.example.BaseExampleFragment
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.activity.PayCardActivity
import com.moneycollect.example.activity.ValidationWebActivity
import com.moneycollect.example.databinding.FragmentAddpaymentLayoutBinding
import com.moneycollect.example.utils.checkRequestPaymentMethodData
import com.moneycollect.example.utils.hideKeyboard
import java.util.*

/**
 * [AddWithPaymentFragment]
 * If you add the card is not set in the future use will only payment,otherwise provide pay for use in the future.
 */
class AddWithPaymentFragment : BaseExampleFragment(),View.OnClickListener{

    private var viewBinding: FragmentAddpaymentLayoutBinding?=null

    /***  [MoneyCollectContentView]*/
    private var moneyCollectContentView: MoneyCollectContentView?=null

    /*** [MoneyCollectResultBackInterface]*/
    private var  moneyCollectResultBackInterface: MoneyCollectResultBackInterface?=null

    // PaymentModel (PAY)
    private var currentModel:MoneyCollectPaymentModel=MoneyCollectPaymentModel.PAY

    //RequestCreatePayment params for pay
    private var currentRequestCreatePayment:RequestCreatePayment?=null
    //RequestConfirmPayment params for pay
    private var currentRequestConfirmPayment:RequestConfirmPayment?=null
    //RequestPaymentMethod params for pay
    private var currentRequestCreatePaymentMethod:RequestPaymentMethod?=null

    //SupportBankList params for pay
    private var supportBankList: List<Int>? = null

    private var REQUEST_WEB_CODE=1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewBinding =  FragmentAddpaymentLayoutBinding.inflate(layoutInflater, container, false)
        initUi()
        return viewBinding?.root
    }

    private fun initUi() {
        currentModel= arguments?.getSerializable(Constant.CURRENT_PAYMENT_MODEL) as MoneyCollectPaymentModel
        currentRequestCreatePayment= arguments?.getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG)
        currentRequestConfirmPayment= arguments?.getParcelable(Constant.CONFIRM_PAYMENT_REQUEST_TAG)
        currentRequestCreatePaymentMethod= arguments?.getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG)
        supportBankList = arguments?.getSerializable(Constant.SUPPORT_BANK_LIST_TAG) as ArrayList<Int>?

        moneyCollectContentView = viewBinding?.mcAddContentWidget
        moneyCollectResultBackInterface = moneyCollectContentView?.gainMoneyCollectResultBackInterface()
        val moneyCollectContentViewParamsBuilder = MoneyCollectContentViewParams.Builder()
            .activity(activity)
            .moneyCollectPaymentModel(currentModel)
            .supportCardViewList(supportBankList)
            .toolbarLayoutVisibility(View.VISIBLE)
            .addClickListener(this)

        if (currentRequestCreatePaymentMethod != null && currentRequestCreatePaymentMethod?.billingDetails != null) {
            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod?.billingDetails?.firstName)
                || !TextUtils.isEmpty(currentRequestCreatePaymentMethod?.billingDetails?.lastName)
            ) {
                val moneyCollectNameParams = MoneyCollectNameParams.Builder()
                    .firstName(currentRequestCreatePaymentMethod?.billingDetails?.firstName)
                    .lastName(currentRequestCreatePaymentMethod?.billingDetails?.lastName)
                    .build()
                moneyCollectContentViewParamsBuilder.nameParams(moneyCollectNameParams)
            }
            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod?.billingDetails?.email)) {
                moneyCollectContentViewParamsBuilder.email(currentRequestCreatePaymentMethod?.billingDetails?.email)
            }
        }
        moneyCollectContentView?.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build())

        //Listening to pay button return data, further request
        activity?.let {
            moneyCollectContentView?.contentResultParamsLiveData?.observe(it,
                { result ->
                    result.apply {
                        when (result?.status) {
                            MoneyCollectContentStyleCheck.SUCCESS -> {
                                dealData(result)
                            }
                            MoneyCollectContentStyleCheck.FAULT -> {
                                moneyCollectResultBackInterface?.failExceptionBack(result.description)
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * deal data
     */
    private fun dealData(moneyCollectContentResultParams: MoneyCollectContentResultParams) {
        currentRequestCreatePaymentMethod?.let {
            val  requestPaymentMethod=RequestPaymentMethod(
                it.type,
                RequestPaymentMethod.BillingDetails(
                    it.billingDetails?.address,
                    moneyCollectContentResultParams.billingDetails?.email,
                    moneyCollectContentResultParams.billingDetails?.firstName,
                    moneyCollectContentResultParams.billingDetails?.lastName,
                    it.billingDetails?.phone
                ),
                RequestPaymentMethod.Card(
                    moneyCollectContentResultParams.cardParams?.cardNo,
                    moneyCollectContentResultParams.cardParams?.expMonth,
                    moneyCollectContentResultParams.cardParams?.expYear,
                    moneyCollectContentResultParams.cardParams?.securityCode,
                )
            )
            createPaymentMethod(requestPaymentMethod, moneyCollectContentResultParams)
        }
    }

    /**
     * create paymentMethod
     */
    private fun createPaymentMethod(
        requestPaymentMethod: RequestPaymentMethod,
        moneyCollectContentResultParams: MoneyCollectContentResultParams,
    ) {
        activity?.let {
            val error = checkRequestPaymentMethodData(activity, requestPaymentMethod)
            if (!TextUtils.isEmpty(error)) {
                moneyCollectResultBackInterface?.failExceptionBack(error)
                return
            }
            moneyCollect.createPaymentMethod(
                it, requestPaymentMethod,
                object : ApiResultCallback<PaymentMethod> {
                    override fun onSuccess(result: PaymentMethod) {
                        createPayment(result, moneyCollectContentResultParams)
                    }

                    override fun onError(e: Exception) {
                        moneyCollectResultBackInterface?.failExceptionBack(e.message)
                    }
                })
        }
    }

    /**
     * create payment
     */
    private fun createPayment(
        paymentMethod: PaymentMethod,
        moneyCollectContentResultParams: MoneyCollectContentResultParams,
    ) {
        if (currentRequestCreatePayment == null){
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.request_create_payment_empty_str))
            return
        }
        if (TextUtils.isEmpty(currentRequestCreatePayment?.customerId)) {
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.customer_id_empty_str))
            return
        }
        currentRequestCreatePayment?.let {
            val requestCreatePayment = RequestCreatePayment(
                amount = it.amount,
                confirmationMethod = it.confirmationMethod,
                currency = it.currency,
                customerId = it.customerId,
                description = it.description,
                ip = it.ip,
                lineItems = it.lineItems,
                notifyUrl = it.notifyUrl,
                orderNo = it.orderNo,
                paymentMethod = paymentMethod.id,
                preAuth = it.preAuth,
                receiptEmail = it.receiptEmail,
                returnUrl = it.returnUrl,
                setupFutureUsage = moneyCollectContentResultParams.isFutureUse,
                shipping = RequestCreatePayment.Shipping(
                    it.shipping?.address,
                    firstName = it.shipping?.firstName,
                    lastName = it.shipping?.lastName,
                    phone = it.shipping?.phone
                ),
                statementDescriptor = it.statementDescriptor,
                statementDescriptorSuffix = it.statementDescriptorSuffix,
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
                        //	Status: incomplete
                        confirmPayment(result, paymentMethod)
                    }

                    override fun onError(e: Exception) {
                        moneyCollectResultBackInterface?.failExceptionBack(e.message)
                    }
                })
        }
    }

    /**
     * confirm payment
     */
    private fun confirmPayment(payment: Payment, paymentMethod: PaymentMethod) {
        if (currentRequestConfirmPayment == null){
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.request_confirm_payment_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment.paymentMethod)) {
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_method_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment.id)) {
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_id_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment.clientSecret)) {
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_client_secret_empty_str))
            return
        }
        currentRequestConfirmPayment?.let {
            val requestConfirmPayment = RequestConfirmPayment(
                amount = payment.amount?.toBigInteger(),
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
                    override fun onSuccess(payment: Payment) {
                        //if nextAction object is not null and redirectToUrl address is not null, further 3 d verification
                        if (payment.nextAction != null) {
                            if (!TextUtils.isEmpty(payment.nextAction?.redirectToUrl)) {
                                val intent = Intent(activity, ValidationWebActivity::class.java)
                                intent.putExtra(Constant.VALIDATION_PARAM_URL,
                                    payment.nextAction?.redirectToUrl)
                                intent.putExtra(Constant.VALIDATION_PAYMENT_ID, payment.id)
                                intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET,
                                    payment.clientSecret)
                                startActivityLauncher.launch(intent)
                            } else {
                                moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                                    Constant.PAYMENT_PENDING_MESSAGE)
                            }
                        } else {
                            //Need to deal with the state has succeeded, uncaptured, pending, failed, canceled
                            when (payment.status) {
                                Constant.PAYMENT_SUCCEEDED -> {
                                    var intent =  Intent()
                                    intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, payment)
                                    activity?.setResult(Constant.PAYMENT_RESULT_CODE,intent)
                                    moneyCollectResultBackInterface?.paymentConfirmResultBack(true,
                                        "")
                                }
                                Constant.PAYMENT_FAILED -> {
                                    moneyCollectResultBackInterface?.paymentConfirmResultBack(false,
                                        payment.errorMessage)
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

                    override fun onError(e: Exception) {
                        moneyCollectResultBackInterface?.failExceptionBack(e.message)
                    }
                })
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
                        var intent =  Intent()
                        intent.putExtra(Constant.WEB_RESULT_TAG, resultStr)
                        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, payment)
                        activity?.setResult(Constant.PAYMENT_RESULT_CODE,intent)
                        moneyCollectResultBackInterface?.paymentConfirmResultBack(true, "")
                    }else{
                        moneyCollectResultBackInterface?.failExceptionBack(resultStr)
                    }
                }
            }
        }

    private companion object {
        const val SAVE_PAYMENT:String="SaveWithPaymentCardFragment"  //save fragment tag
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
           if (view.id == moneyCollectContentView?.getToolbarBackIcon()?.id && moneyCollectContentView?.isRequestLoading==false) {
               activity?.let {
                   hideKeyboard(it)
                   (it as? PayCardActivity)?.switchContent(SAVE_PAYMENT)
               }
            }
        }
    }
}