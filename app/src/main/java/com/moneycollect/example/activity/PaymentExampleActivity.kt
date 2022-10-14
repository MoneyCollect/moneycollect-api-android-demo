package com.moneycollect.example.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.moneycollect.android.model.request.RequestConfirmPayment
import com.moneycollect.android.model.request.RequestCreatePayment
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityCreatePaymentLayoutBinding
import com.moneycollect.example.utils.formatString

/**
 * [PaymentExampleActivity] show create Payment、retrieve Payment and confirm Payment  sample
 */
class PaymentExampleActivity : BaseExampleActivity(), View.OnClickListener {

    private var viewBinding: ActivityCreatePaymentLayoutBinding? = null

    private var createPaymentBtn: Button? = null

    private var retrievePaymentBtn: Button? = null

    private var confirmPaymentBtn: Button? = null

    private var resultTv: TextView? = null
    private var resultTag: TextView? = null

    //payment for pay
    private var payment: Payment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCreatePaymentLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUi()
    }

    private fun initUi() {
        resultTv = viewBinding?.resultTv
        resultTag = viewBinding?.resultTag

        createPaymentBtn = viewBinding?.mcCreatePaymentBtn
        createPaymentBtn?.setOnClickListener(this)

        retrievePaymentBtn = viewBinding?.mcRetrievePaymentBtn
        retrievePaymentBtn?.setOnClickListener(this)

        confirmPaymentBtn = viewBinding?.mcConfirmPaymentBtn
        confirmPaymentBtn?.setOnClickListener(this)
    }

    /**
     * create Payment
     */
    private fun createPayment() {
        val createPayment = TestRequestData.testRequestPayment
        if (createPayment.confirmationMethod == RequestCreatePayment.ConfirmationMethod.Automatic) {
            if (TextUtils.isEmpty(createPayment.paymentMethod)) {
                showToast(getString(R.string.payment_method_empty_str))
                return
            }
        }
        showLoadingDialog()
        moneyCollect.createPayment(createPayment,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {
                    dismissLoadingDialog()
                    payment = result
                    resultTag?.text = getString(R.string.create_payment_success_str)
                    resultTv?.text = formatString(Gson().toJson(result))
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    resultTag?.text =getString(R.string.create_payment_error_str)
                    resultTv?.text = formatString(Gson().toJson(e))
                }
            })
    }

    /**
     * confirm Payment
     */
    private fun confirmPayment() {
        if (TextUtils.isEmpty(payment?.paymentMethod)) {
            showToast(getString(R.string.payment_method_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment?.id)) {
            showToast(getString(R.string.payment_id_empty_str))
            return
        }
        if (TextUtils.isEmpty(payment?.clientSecret)) {
            showToast(getString(R.string.payment_client_secret_empty_str))
            return
        }
        showLoadingDialog()
        val requestConfirmPayment = RequestConfirmPayment(
            amount = payment?.amount?.toBigInteger(),
            currency = payment?.currency,
            id = payment?.id,
            ip = payment?.ip,
            notifyUrl = payment?.notifyUrl,
            paymentMethod = payment?.paymentMethod,
            receiptEmail = payment?.receiptEmail,
            returnUrl = payment?.returnUrl,
            setupFutureUsage = payment?.setupFutureUsage,
            shipping = RequestConfirmPayment.Shipping(
                TestRequestData.address,
                firstName = TestRequestData.firstName,
                lastName = TestRequestData.lastName,
                phone = TestRequestData.phone
            ),
            website = TestRequestData.website
        )
        moneyCollect.confirmPayment(requestConfirmPayment, payment?.clientSecret,
            object : ApiResultCallback<Payment> {
                override fun onSuccess(result: Payment) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.confirm_payment_success_str)
                    resultTv?.text = formatString(Gson().toJson(result))
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    resultTag?.text = getString(R.string.confirm_payment_error_str)
                    resultTv?.text = formatString(Gson().toJson(e))
                }
            })
    }

    /**
     * retrieve Payment
     */
    private fun retrievePayment() {
        if (TextUtils.isEmpty(payment?.id)) {
            showToast(getString(R.string.payment_id_empty_str))
            return
        }
        payment?.id?.let {
            showLoadingDialog()
            moneyCollect.retrievePayment(it, payment?.clientSecret,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        dismissLoadingDialog()
                        resultTag?.text = getString(R.string.retrive_payment_success_str)
                        resultTv?.text = formatString(Gson().toJson(result))
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        resultTag?.text = getString(R.string.retrieve_payment_error_str)
                        resultTv?.text = formatString(Gson().toJson(e))
                    }
                })
        }
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view != null) {
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                when (view.id) {
                    R.id.mc_create_payment_btn -> {
                        createPayment()
                    }
                    R.id.mc_confirm_payment_btn -> {
                        if (payment == null) {
                            showToast(getString(R.string.payment_empty_str))
                            return
                        }
                        if (TextUtils.isEmpty(payment?.id)) {
                            showToast(getString(R.string.payment_id_empty_str))
                            return
                        }
                        confirmPayment()
                    }
                    R.id.mc_retrieve_payment_btn -> {
                        if (payment == null) {
                            showToast(getString(R.string.payment_empty_str))
                            return
                        }
                        if (TextUtils.isEmpty(payment?.id)) {
                            showToast(getString(R.string.payment_id_empty_str))
                            return
                        }
                        retrievePayment()
                    }
                }
            }
        }
    }
}