package com.moneycollect.example.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.moneycollect.android.model.MoneyCollectCardListViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface
import com.moneycollect.android.ui.view.MoneyCollectCardListView
import com.moneycollect.example.BaseExampleFragment
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.activity.PayCardActivity
import com.moneycollect.example.activity.ValidationWebActivity
import com.moneycollect.example.databinding.FragmentSaveWithPaymentLayoutBinding
import java.util.*

/**
 * [SaveWithPaymentCardFragment]
 * Show the payment list,offer users pay the payment, also you can add a new card to pay jump [AddWithPaymentFragment]
 *
 */
class SaveWithPaymentCardFragment : BaseExampleFragment(),View.OnClickListener{

    private var viewBinding: FragmentSaveWithPaymentLayoutBinding?=null

    /***  [MoneyCollectCardListView]*/
    var cardListLayout: MoneyCollectCardListView?=null

    /***  [MoneyCollectResultBackInterface]*/
    private var  moneyCollectResultBackInterface: MoneyCollectResultBackInterface?=null

    // PaymentModel (PAY)
    private var currentModel:MoneyCollectPaymentModel=MoneyCollectPaymentModel.PAY

    //customerId
    var customerId: String?=null

    //RequestCreatePayment params for pay
    private var currentRequestCreatePayment:RequestCreatePayment?=null
    //RequestConfirmPayment params for pay
    private var currentRequestConfirmPayment:RequestConfirmPayment?=null

    //groupList params
    private var groupList: ArrayList<PaymentMethod> = ArrayList<PaymentMethod>()
    //childList params
    private var childList: ArrayList<ArrayList<PaymentMethod>> = ArrayList<ArrayList<PaymentMethod>>()

    //loading active
    private var isRequestLoading=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewBinding = FragmentSaveWithPaymentLayoutBinding.inflate(layoutInflater, container, false)
        initUi()
        return viewBinding?.root
    }

    private fun initUi() {
        currentModel= arguments?.getSerializable(Constant.CURRENT_PAYMENT_MODEL) as MoneyCollectPaymentModel
        currentRequestCreatePayment= arguments?.getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG)
        currentRequestConfirmPayment= arguments?.getParcelable(Constant.CONFIRM_PAYMENT_REQUEST_TAG)
        customerId= arguments?.getString(Constant.CUSTOMER_ID_TAG)

        cardListLayout= viewBinding?.mcCardList
        moneyCollectResultBackInterface=cardListLayout?.gainMoneyCollectResultBackInterface()
        val moneyCollectCardListViewParams = MoneyCollectCardListViewParams.Builder()
            .activity(activity)
            .moneyCollectPaymentModel(currentModel)
            .addClickListener(this)
            .build()
        cardListLayout?.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams)

        //Listening to pay button return data, further request
        activity?.let {
            cardListLayout?.paymentMethodParamsLiveData?.observe(it,
                { result ->
                    result.apply {
                        if (result != null) {
                            createPayment(result)
                        } else {
                            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_method_list_empty_str))
                        }
                    }
                }
            )
        }
        selectAllPaymentMethods()
    }


    /**
     * create payment
     */
    private fun createPayment(paymentMethod: PaymentMethod) {
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
                paymentMethodTypes = TestRequestData.paymentMethodTypes,
                preAuth = it.preAuth,
                receiptEmail = it.receiptEmail,
                returnUrl = it.returnUrl,
                setupFutureUsage = it.setupFutureUsage,
                shipping = RequestCreatePayment.Shipping(
                    address = it.shipping?.address,
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
                    address = it.shipping?.address,
                    firstName = it.shipping?.firstName,
                    lastName = it.shipping?.lastName,
                    phone = it.shipping?.phone
                ),
                website = it.website
            )

            moneyCollect.confirmPayment(requestConfirmPayment, payment.clientSecret,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        //if nextAction object is not null and redirectToUrl address is not null, further 3 d verification
                        if (result.nextAction != null) {
                            if (!TextUtils.isEmpty(result.nextAction?.type)) {
                                var redirectToUrl=result.nextAction?.redirectToUrl
                                if (result.nextAction?.type?.equals(TestRequestData.weChatPayNextActionType) == true) {
                                    redirectToUrl=result.nextAction?.wechatPayH5?.redirectToUrl
                                }
                                if (!TextUtils.isEmpty(redirectToUrl)) {
                                    val intent = Intent(activity, ValidationWebActivity::class.java)
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
                                    var intent =  Intent()
                                    intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, result)
                                    activity?.setResult(Constant.PAYMENT_RESULT_CODE,intent)
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

    /**
     * select all paymentMethods
     */
    private fun selectAllPaymentMethods() {
        if (TextUtils.isEmpty(customerId)){
            cardListLayout?.visibility = View.VISIBLE
            cardListLayout?.setPaymentButtonAndFootViewStatus(false)
            return
        }
        customerId?.let {
            showLoadingDialog()
            isRequestLoading=true
            moneyCollect.selectAllPaymentMethods(it,
                object : ApiResultCallback<Any> {
                    override fun onSuccess(result: Any) {
                        dismissLoadingDialog()
                        cardListLayout?.visibility = View.VISIBLE
                        when (result) {
                            !is ArrayList<*> -> {
                                cardListLayout?.setPaymentButtonAndFootViewStatus(false)
                            }
                            else -> {
                                when {
                                    result.isEmpty() -> {
                                        cardListLayout?.setPaymentButtonAndFootViewStatus(false)
                                    }
                                    else -> {
                                        childList.add(result as ArrayList<PaymentMethod>)
                                        when {
                                            childList.isNotEmpty() -> {
                                                groupList.add(childList[0][0])
                                                cardListLayout?.setDataList(groupList, childList)
                                                cardListLayout?.setPaymentButtonAndFootViewStatus(
                                                    true)
                                            }
                                            else -> {
                                                cardListLayout?.setPaymentButtonAndFootViewStatus(
                                                    false)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        isRequestLoading = false
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        cardListLayout?.visibility = View.VISIBLE
                        cardListLayout?.setPaymentButtonAndFootViewStatus(false)
                        isRequestLoading = false
                    }
                })
        }
    }



    companion object{
        const val ADD_PAYMENT:String="AddWithPaymentFragment" //add fragment tag
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
            if (view.id == cardListLayout?.getToolbarBackIcon()?.id && cardListLayout?.isRequestLoading==false && !isRequestLoading) {
                activity?.finish()
            }else  if (view.id == cardListLayout?.getChildFootView()?.id && cardListLayout?.isRequestLoading==false && !isRequestLoading) {
                activity?.let {
                    (it as? PayCardActivity)?.switchContent(ADD_PAYMENT)
                }
            }
        }
    }
}