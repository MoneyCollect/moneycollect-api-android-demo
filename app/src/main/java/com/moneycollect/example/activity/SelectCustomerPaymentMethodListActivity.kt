package com.moneycollect.example.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivitySelectPaymentMethodListLayoutBinding
import com.moneycollect.example.utils.formatString

/**
 * [SelectCustomerPaymentMethodListActivity] show retrieve the payment list
 */
class SelectCustomerPaymentMethodListActivity : BaseExampleActivity(),View.OnClickListener{

    private var viewBinding: ActivitySelectPaymentMethodListLayoutBinding?=null

    private var selectPMListBtn: Button?=null

    private var resultTv: TextView?=null
    private var resultTag: TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySelectPaymentMethodListLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUi()
    }

    private fun initUi() {
        resultTv=viewBinding?.resultTv
        resultTag=viewBinding?.resultTag
        selectPMListBtn=viewBinding?.mcSelectPaymentMethodListBtn
        selectPMListBtn?.setOnClickListener(this)
    }

    /**
     * select All PaymentMethods
     */
    private fun selectAllPaymentMethods() {
        val customerId = TestRequestData.customerId
        if (TextUtils.isEmpty(customerId)) {
            showToast(getString(R.string.customer_id_empty_str))
            return
        }
        showLoadingDialog()
        moneyCollect.selectAllPaymentMethods(customerId,
            object : ApiResultCallback<Any> {
                override fun onSuccess(result: Any) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.select_payment_method_list_success_str)
                    resultTv?.text = formatString(Gson().toJson(result))
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.select_payment_method_list_error_str)
                    resultTv?.text = formatString(Gson().toJson(e))
                }
            })
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                if (view.id == R.id.mc_select_payment_method_list_btn) {
                    selectAllPaymentMethods()
                }
            }
        }
    }
}