package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.moneycollect.android.model.Address;
import com.moneycollect.android.model.MoneyCollectContentViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.view.MoneyCollectContentView;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivityCreateAPaymentMethodBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.TempUtils;

import org.jetbrains.annotations.NotNull;

import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.FAULT;
import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.SUCCESS;

/**
 * [PaymentMethodExampleActivity] show create PaymentMethod、retrieve PaymentMethod and attach PaymentMethod  sample
 */
public class PaymentMethodExampleActivity extends BaseExampleActivity implements View.OnClickListener {

    private ActivityCreateAPaymentMethodBinding viewBinding;

    /***  [MoneyCollectContentView]*/
    private MoneyCollectContentView moneyCollectContentView;

    private Button attachPMBtn;

    private Button retrievePMBtn;

    private TextView resultTv;
    private TextView resultTag;

    //paymentMethodId for pay
    private String paymentMethodId;

    //address line1
    private TextInputEditText addressLine1Et;
    //address line2
    private TextInputEditText addressLine2Et;
    private TextInputEditText cityEt;
    private TextInputEditText stateEt;
    private TextInputEditText postCodeEt;
    private TextInputEditText countryEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCreateAPaymentMethodBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUi();
        initBillingDetailUi();
    }

    private void initBillingDetailUi() {
        addressLine1Et = viewBinding.addressEt;
        addressLine2Et = viewBinding.addressSecondEt;
        cityEt = viewBinding.cityEt;
        stateEt = viewBinding.stateEt;
        postCodeEt = viewBinding.postCodeEt;
        countryEt = viewBinding.countryEt;

        addressLine1Et.setText(TestRequestData.Companion.getAddress().getLine1());
        addressLine2Et.setText(TestRequestData.Companion.getAddress().getLine2());
        cityEt.setText(TestRequestData.Companion.getAddress().getCity());
        stateEt.setText(TestRequestData.Companion.getAddress().getState());
        postCodeEt.setText(TestRequestData.Companion.getAddress().getPostalCode());
        countryEt.setText(TestRequestData.Companion.getAddress().getCountry());
    }

    @SuppressLint("VisibleForTests")
    private void initUi() {
        resultTv = viewBinding.resultTv;
        resultTag = viewBinding.resultTag;
        attachPMBtn = viewBinding.mcAttachPaymentMethodBtn;
        attachPMBtn.setOnClickListener(this);
        retrievePMBtn = viewBinding.mcRetrievePaymentMethodBtn;
        retrievePMBtn.setOnClickListener(this);

        moneyCollectContentView = viewBinding.createPmWidget;
        MoneyCollectContentViewParams.Builder moneyCollectContentViewParamsBuilder =new MoneyCollectContentViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(MoneyCollectPaymentModel.CREATE_PAYMENT_METHOD)
                .toolbarLayoutVisibility(View.GONE)
                .futureUseLayoutVisible(View.GONE);
        moneyCollectContentView.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build());

        //data of moneyCollectContentView
        moneyCollectContentView.getContentResultParamsLiveData().observe(this, result -> {
            if (result.getStatus() != null) {
                if (result.getStatus().equals(SUCCESS)) {
                    RequestPaymentMethod requestPaymentMethod = new RequestPaymentMethod(
                            "card",
                            new RequestPaymentMethod.BillingDetails(
                                    new Address(
                                            cityEt.getText().toString().trim(),
                                            countryEt.getText().toString().trim(),
                                            addressLine1Et.getText().toString().trim(),
                                            addressLine2Et.getText().toString().trim(),
                                            postCodeEt.getText().toString().trim(),
                                            stateEt.getText().toString().trim()
                                    ),
                                    result.getBillingDetails().email,
                                    result.getBillingDetails().firstName,
                                    result.getBillingDetails().lastName,
                                    TestRequestData.Companion.getPhone()
                            ),
                            new RequestPaymentMethod.Card(
                                    result.getCardParams().cardNo,
                                    result.getCardParams().expMonth,
                                    result.getCardParams().expYear,
                                    result.getCardParams().securityCode
                            )
                    );
                    createPaymentMethod(requestPaymentMethod);
                } else if (result.getStatus().equals(FAULT)) {
                    showToast(result.getDescription());
                }
            }
        });
    }

    /**
     * create PaymentMethod
     */
    private void createPaymentMethod(RequestPaymentMethod requestPaymentMethod) {
        String error = TempUtils.checkRequestPaymentMethodData(this, requestPaymentMethod);
        if (!TextUtils.isEmpty(error)) {
            showToast(error);
            return;
        }
        showLoadingDialog();
        moneyCollect.createPaymentMethod(this, requestPaymentMethod,
                new ApiResultCallback<PaymentMethod>() {
                    @Override
                    public void onSuccess(@NotNull PaymentMethod result) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.create_payment_method_success_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                        paymentMethodId = result.id;
                        if (result != null && result.id != null) {
                            //refresh TestRequestData data
                            TestRequestData.Companion.setPaymentId(result.id);
                            TestRequestData.Companion.getTestRequestPayment().setPaymentMethod(result.id);
                            if (result.billingDetails != null) {
                                TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.address
                                        = result.billingDetails.address != null ? result.billingDetails.address : TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.address;
                                TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.email
                                        = result.billingDetails.email != null ? result.billingDetails.email : TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.email;
                                TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.firstName
                                        = result.billingDetails.firstName != null ? result.billingDetails.firstName : TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.firstName;
                                TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.lastName
                                        = result.billingDetails.lastName != null ? result.billingDetails.lastName : TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.lastName;
                                TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.phone
                                        = result.billingDetails.phone != null ? result.billingDetails.phone : TestRequestData.Companion.getTestRequestPaymentMethod().billingDetails.phone;
                            }
                        }
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.create_payment_method_error_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                    }
                }

        );
    }


    /**
     * attach PaymentMethod to customer
     */
    @SuppressLint("SetTextI18n")
    private void attachPaymentMethod() {
        if (!TextUtils.isEmpty(paymentMethodId)) {
            showLoadingDialog();
            moneyCollect.attachPaymentMethod(paymentMethodId,
                    TestRequestData.Companion.getCustomerId(),
                    new ApiResultCallback<Object>() {
                        @Override
                        public void onSuccess(@NotNull Object result) {
                            dismissLoadingDialog();
                            resultTag.setText(String.format("Attach PaymentMethod %s to customer %s success...", paymentMethodId, TestRequestData.Companion.getCustomerId()));
                            resultTv.setText("");
                        }

                        @Override
                        public void onError(@NotNull Exception e) {
                            dismissLoadingDialog();
                            resultTag.setText(String.format("Attach PaymentMethod %s to customer %s error...", paymentMethodId, TestRequestData.Companion.getCustomerId()));
                            resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                        }
                    }
            );
        }
    }


    /**
     * retrieve PaymentMethod
     */
    @SuppressLint("SetTextI18n")
    private void retrievePaymentMethod() {
        if (!TextUtils.isEmpty(paymentMethodId)) {
            showLoadingDialog();
            moneyCollect.retrievePaymentMethod(paymentMethodId,
                    new ApiResultCallback<PaymentMethod>() {
                        @Override
                        public void onSuccess(@NotNull PaymentMethod result) {
                            dismissLoadingDialog();
                            resultTag.setText(String.format("Retrieve PaymentMethod %s success...", paymentMethodId));
                            resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                        }

                        @Override
                        public void onError(@NotNull Exception e) {
                            dismissLoadingDialog();
                            resultTag.setText(String.format("Retrieve PaymentMethod %s error...", paymentMethodId));
                            resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                        }
                    }
            );
        }
    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        if (view != null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(view.getId(), 800)) {
                if (view.getId() == R.id.mc_attach_payment_method_btn) {
                    if (TextUtils.isEmpty(paymentMethodId)) {
                        showToast(getString(R.string.payment_method_id_empty_str));
                        return;
                    }
                    attachPaymentMethod();
                } else if (view.getId() == R.id.mc_retrieve_payment_method_btn) {
                    if (TextUtils.isEmpty(paymentMethodId)) {
                        showToast(getString(R.string.payment_method_id_empty_str));
                        return;
                    }
                    retrievePaymentMethod();
                }
            }
        }
    }
}
