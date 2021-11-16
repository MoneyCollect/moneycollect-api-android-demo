package com.moneycollect.example.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.moneycollect.android.model.MoneyCollectContentViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.request.RequestCreateCustomer
import com.moneycollect.android.model.response.Customer
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.view.MoneyCollectContentView
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityCreateCustomerLayoutBinding
import com.moneycollect.example.utils.checkRequestCreateCustomerData
import com.moneycollect.example.utils.formatString

/**
 *  [CreateCustomerActivity] show create customer sample
 */
class CreateCustomerActivity :BaseExampleActivity(){

    private var viewBinding: ActivityCreateCustomerLayoutBinding?=null

    /***  [MoneyCollectContentView]*/
    private var moneyCollectContentView: MoneyCollectContentView?=null

    private var resultTv: TextView?=null
    private var resultTag: TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCreateCustomerLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUi()
    }

    private fun initUi() {
        resultTv=viewBinding?.resultTv
        resultTag=viewBinding?.resultTag

        moneyCollectContentView= viewBinding?.createPmWidget
        val moneyCollectContentViewParamsBuilder = MoneyCollectContentViewParams.Builder()
            .activity(this)
            .moneyCollectPaymentModel(MoneyCollectPaymentModel.CREATE_CUSTOMER)
            .toolbarLayoutVisibility(View.GONE)
            .futureUseLayoutVisible(View.GONE)
        moneyCollectContentView?.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build())

        moneyCollectContentView?.contentResultParamsLiveData?.observe(this,
            { result ->
                result.apply {
                    when (result?.status) {
                        MoneyCollectContentStyleCheck.SUCCESS -> {
                            result.let {
                                val requestCustomer = RequestCreateCustomer(
                                    TestRequestData.address,
                                    description = "test",
                                    email = it.billingDetails?.email,
                                    firstName = it.billingDetails?.firstName,
                                    lastName = it.billingDetails?.lastName,
                                    phone = TestRequestData.phone,
                                    shipping = RequestCreateCustomer.Shipping(
                                        TestRequestData.address,
                                        firstName = it.billingDetails?.firstName,
                                        lastName = it.billingDetails?.lastName,
                                        phone = TestRequestData.phone
                                    )
                                )
                                createCustomer(requestCustomer)
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
     * create customer
     */
    private fun createCustomer(requestCreateCustomer: RequestCreateCustomer) {
        val error = checkRequestCreateCustomerData(this, requestCreateCustomer)
        if (!TextUtils.isEmpty(error)) {
            showToast(error)
            return
        }
        showLoadingDialog()
        moneyCollect.createCustomer(requestCreateCustomer,
            object : ApiResultCallback<Customer> {
                override fun onSuccess(result: Customer) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.create_customer_success_str)
                    resultTv?.text = formatString(Gson().toJson(result))
                    if(result != null){
                        result.id?.let {
                            TestRequestData.customerId = result.id.toString();
                            TestRequestData.testRequestPayment.customerId = result.id.toString()
                        }
                    }
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.create_customer_error_str)
                    resultTv?.text = formatString(Gson().toJson(e))
                }
            })
    }
}