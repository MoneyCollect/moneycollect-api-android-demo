package com.moneycollect.example.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moneycollect.android.model.MoneyCollectContentResultParams
import com.moneycollect.android.model.MoneyCollectContentViewParams
import com.moneycollect.android.model.MoneyCollectNameParams
import com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface
import com.moneycollect.android.ui.view.MoneyCollectContentView
import com.moneycollect.example.BaseExampleFragment
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.activity.SaveCardActivity
import com.moneycollect.example.databinding.FragmentAddpaymentLayoutBinding
import com.moneycollect.example.utils.checkRequestPaymentMethodData
import com.moneycollect.example.utils.hideKeyboard
import java.util.*

/**
 * [AddCardFragment]
 * If you add the card is not set in the future use, will only call createPaymentMethod method and return a PaymentMethod,
 * otherwise create further calls after PaymentMethod attachPaymentMethod binding PaymentMethod provide pay for use in the future.
 */
class AddCardFragment : BaseExampleFragment(),View.OnClickListener{

    private var viewBinding: FragmentAddpaymentLayoutBinding?=null

    /***  [MoneyCollectContentView]*/
    private var moneyCollectContentView: MoneyCollectContentView?=null

    /*** [MoneyCollectResultBackInterface]*/
    private var  moneyCollectResultBackInterface: MoneyCollectResultBackInterface?=null

    // PaymentModel (ATTACH_PAYMENT_METHOD)
    private var currentModel:MoneyCollectPaymentModel=MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD

    //customerId for pay
    var customerId: String? =null

    //RequestPaymentMethod for pay
    private var currentRequestCreatePaymentMethod:RequestPaymentMethod?=null

    //SupportBankList params for pay
    private var supportBankList: List<Int>? = null

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
        currentRequestCreatePaymentMethod= arguments?.getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG)
        customerId= arguments?.getString(Constant.CUSTOMER_ID_TAG)
        supportBankList = arguments?.getSerializable(Constant.SUPPORT_BANK_LIST_TAG) as ArrayList<Int>?

        moneyCollectContentView= viewBinding?.mcAddContentWidget
        moneyCollectResultBackInterface=moneyCollectContentView
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
                    moneyCollectContentResultParams.cardParams?.securityCode
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
                moneyCollectResultBackInterface!!.failExceptionBack(error)
                return
            }

            showLoadingDialog()
            moneyCollect.createPaymentMethod(
                it, requestPaymentMethod,
                object : ApiResultCallback<PaymentMethod> {
                    override fun onSuccess(result: PaymentMethod) {
                        if (moneyCollectContentResultParams.isFutureUse == IS_FUTURE_USE_ON) {
                            attachPaymentMethod(result)
                        } else {
                            dismissLoadingDialog()
                            resultBack(result)
                        }
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        moneyCollectResultBackInterface?.failExceptionBack(e.message)
                    }
                })
        }
    }

    /**
     * return paymentMethod
     */
    fun resultBack(paymentMethod: PaymentMethod?){
        paymentMethod?.let {
            val intent = Intent()
            intent.putExtra(Constant.ADD_PAYMENT_METHOD, it)
            activity?.setResult(Constant.ADD_RESULT_CODE, intent)
            activity?.finish()
        }
    }

    /**
     * attach paymentMethod
     */
    fun attachPaymentMethod(paymentMethod: PaymentMethod?) {
        if (TextUtils.isEmpty(customerId)){
            dismissLoadingDialog()
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.customer_id_empty_str))
            return
        }
        val paymentMethodId=paymentMethod?.id
        if (TextUtils.isEmpty(paymentMethodId)){
            dismissLoadingDialog()
            moneyCollectResultBackInterface?.failExceptionBack(getString(R.string.payment_method_id_empty_str))
            return
        }
        paymentMethodId?.let { paymentMethodId ->
            customerId?.let { customerId ->
                moneyCollect.attachPaymentMethod(paymentMethodId, customerId,
                    object : ApiResultCallback<Any> {
                        override fun onSuccess(result: Any) {
                            dismissLoadingDialog()
                            resultBack(paymentMethod)
                        }

                        override fun onError(e: Exception) {
                            dismissLoadingDialog()
                            moneyCollectResultBackInterface?.failExceptionBack(e.message)
                        }
                    })
            }
        }
    }



    private companion object {
        const val SAVE_PAYMENT:String="SavePaymentFragment"  //save fragment tag
        const val IS_FUTURE_USE_ON:String="on"
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
           if (view.id == moneyCollectContentView?.getToolbarBackIcon()?.id && moneyCollectContentView?.isRequestLoading==false) {
               activity?.let {
                   hideKeyboard(it)
                   (it as? SaveCardActivity)?.switchContent(SAVE_PAYMENT)
               }
            }
        }
    }
}