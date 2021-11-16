package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.MoneyCollectContentViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.view.MoneyCollectContentView
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityCreateAPaymentMethodBinding
import com.moneycollect.example.utils.checkRequestPaymentMethodData
import com.moneycollect.example.utils.formatString

/**
 * [PaymentMethodExampleActivity] show create PaymentMethod、retrieve PaymentMethod and attach PaymentMethod  sample
 */
class PaymentMethodExampleActivity : BaseExampleActivity(),View.OnClickListener{

    private var viewBinding: ActivityCreateAPaymentMethodBinding?=null

    /***  [MoneyCollectContentView]*/
    private var moneyCollectContentView: MoneyCollectContentView?=null

    private var attachPMBtn: Button?=null

    private var retrievePMBtn: Button?=null

    private var resultTv: TextView?=null
    private var resultTag: TextView?=null

    //paymentMethodId for pay
    private var paymentMethodId:String?=null

    //address line1
    private var addressLine1Et: TextInputEditText?=null
    //address line2
    private var addressLine2Et: TextInputEditText?=null
    private var cityEt: TextInputEditText?=null
    private var stateEt: TextInputEditText?=null
    private var postCodeEt: TextInputEditText?=null
    private var countryEt: TextInputEditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCreateAPaymentMethodBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUi()
        initBillingDetailUi()
    }

    private fun initBillingDetailUi() {
        addressLine1Et=viewBinding?.addressEt
        addressLine2Et=viewBinding?.addressSecondEt
        cityEt=viewBinding?.cityEt
        stateEt=viewBinding?.stateEt
        postCodeEt=viewBinding?.postCodeEt
        countryEt=viewBinding?.countryEt

        addressLine1Et?.setText(TestRequestData.address.line1)
        addressLine2Et?.setText(TestRequestData.address.line2)
        cityEt?.setText(TestRequestData.address.city)
        stateEt?.setText(TestRequestData.address.state)
        postCodeEt?.setText(TestRequestData.address.postalCode)
        countryEt?.setText(TestRequestData.address.country)
    }

    private fun initUi() {
        resultTv=viewBinding?.resultTv
        resultTag=viewBinding?.resultTag
        attachPMBtn=viewBinding?.mcAttachPaymentMethodBtn
        attachPMBtn?.setOnClickListener(this)
        retrievePMBtn=viewBinding?.mcRetrievePaymentMethodBtn
        retrievePMBtn?.setOnClickListener(this)

        moneyCollectContentView= viewBinding?.createPmWidget
        val moneyCollectContentViewParamsBuilder = MoneyCollectContentViewParams.Builder()
            .activity(this)
            .moneyCollectPaymentModel(MoneyCollectPaymentModel.CREATE_PAYMENT_METHOD)
            .toolbarLayoutVisibility(View.GONE)
            .futureUseLayoutVisible(View.GONE)
        moneyCollectContentView?.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build())

        //data of moneyCollectContentView
        moneyCollectContentView?.contentResultParamsLiveData?.observe(this,
            { result ->
                result.apply {
                    when (result?.status) {
                        MoneyCollectContentStyleCheck.SUCCESS -> {
                            result.let {
                                val requestPaymentMethod = RequestPaymentMethod(
                                    "card",
                                    RequestPaymentMethod.BillingDetails(
                                        Address(
                                            cityEt?.text.toString().trim(),
                                            countryEt?.text.toString().trim(),
                                            addressLine1Et?.text.toString().trim(),
                                            addressLine2Et?.text.toString().trim(),
                                            postCodeEt?.text.toString().trim(),
                                            stateEt?.text.toString().trim()
                                        ),
                                        it.billingDetails?.email,
                                        it.billingDetails?.firstName,
                                        it.billingDetails?.lastName,
                                        TestRequestData.phone
                                    ),
                                    RequestPaymentMethod.Card(
                                        it.cardParams?.cardNo,
                                        it.cardParams?.expMonth,
                                        it.cardParams?.expYear,
                                        it.cardParams?.securityCode,
                                    )
                                )
                                createPaymentMethod(requestPaymentMethod)
                            }
                        }
                        MoneyCollectContentStyleCheck.FAULT -> {
                            showToast(result.description)
                        }
                    }
                }
            }
        )
    }

    /**
     * create PaymentMethod
     */
    private fun createPaymentMethod(requestPaymentMethod: RequestPaymentMethod) {
        val error = checkRequestPaymentMethodData(this, requestPaymentMethod)
        if (!TextUtils.isEmpty(error)) {
            showToast(error)
            return
        }
        showLoadingDialog()
        moneyCollect.createPaymentMethod(this, requestPaymentMethod,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.create_payment_method_success_str)
                    resultTv?.text = formatString(Gson().toJson(result))
                    paymentMethodId = result.id
                    if (result != null) {
                        result.id?.let {
                            //refresh TestRequestData data
                            TestRequestData.paymentMethodId = result.id.toString()
                            TestRequestData.testRequestPayment.paymentMethod = result.id.toString()
                            result.billingDetails?.address?.let {
                                TestRequestData.testRequestPaymentMethod.billingDetails?.address =
                                    result.billingDetails?.address
                                TestRequestData.address = it
                            }
                            result.billingDetails?.email?.let {
                                TestRequestData.testRequestPaymentMethod.billingDetails?.email =
                                    result.billingDetails?.email
                                TestRequestData.email = it
                            }
                            result.billingDetails?.firstName?.let {
                                TestRequestData.testRequestPaymentMethod.billingDetails?.firstName =
                                    result.billingDetails?.firstName
                                TestRequestData.firstName = it
                            }
                            result.billingDetails?.lastName?.let {
                                TestRequestData.testRequestPaymentMethod.billingDetails?.lastName =
                                    result.billingDetails?.lastName
                                TestRequestData.lastName = it
                            }
                            result.billingDetails?.phone?.let {
                                TestRequestData.testRequestPaymentMethod.billingDetails?.phone =
                                    result.billingDetails?.phone
                                TestRequestData.phone = it
                            }
                        }
                    }
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.create_payment_method_error_str)
                    resultTv?.text = formatString(Gson().toJson(e))
                }
            })
    }

    /**
     * attach PaymentMethod to customer
     */
    @SuppressLint("SetTextI18n")
    fun attachPaymentMethod() {
        paymentMethodId?.let {
            showLoadingDialog()
            moneyCollect.attachPaymentMethod(it,
                TestRequestData.customerId,
                object : ApiResultCallback<Any> {
                    override fun onSuccess(result: Any) {
                        dismissLoadingDialog()
                        resultTag?.text =
                            "Attach PaymentMethod{${paymentMethodId}} to customer{${TestRequestData.customerId}} success..."
                        resultTv?.text = ""
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        resultTag?.text =
                            "Attach PaymentMethod{${paymentMethodId}} to customer{${TestRequestData.customerId}} error..."
                        resultTv?.text = formatString(Gson().toJson(e))
                    }
                })
        }
    }

    /**
     * retrieve PaymentMethod
     */
    @SuppressLint("SetTextI18n")
    private fun retrievePaymentMethod() {
        paymentMethodId?.let {
            showLoadingDialog()
            moneyCollect.retrievePaymentMethod(it,
                object : ApiResultCallback<PaymentMethod> {
                    override fun onSuccess(result: PaymentMethod) {
                        dismissLoadingDialog()
                        resultTag?.text = "Retrieve PaymentMethod{${paymentMethodId}} success..."
                        resultTv?.text = formatString(Gson().toJson(result))
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        resultTag?.text = "Retrieve PaymentMethod{${paymentMethodId}} error..."
                        resultTv?.text = formatString(Gson().toJson(e))
                    }
                })
        }
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                if (view.id == R.id.mc_attach_payment_method_btn) {
                    if (TextUtils.isEmpty(paymentMethodId)){
                        showToast(getString(R.string.payment_method_id_empty_str))
                        return
                    }
                    attachPaymentMethod()
                }else if (view.id == R.id.mc_retrieve_payment_method_btn) {
                    if (TextUtils.isEmpty(paymentMethodId)){
                        showToast(getString(R.string.payment_method_id_empty_str))
                        return
                    }
                    retrievePaymentMethod()
                }
            }
        }
    }
}