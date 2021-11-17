package com.moneycollect.example_java.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivityCreatePaymentLayoutBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.TempUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * [PaymentExampleActivity] show create Payment、retrieve Payment and confirm Payment  sample
 */
public class PaymentExampleActivity extends BaseExampleActivity implements View.OnClickListener{

    private ActivityCreatePaymentLayoutBinding viewBinding;

    private Button createPaymentBtn;

    private Button retrievePaymentBtn;

    private Button confirmPaymentBtn;

    private TextView resultTv;
    private TextView resultTag;

    //payment for pay
    private Payment payment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCreatePaymentLayoutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUi();
    }

    private void initUi() {
        resultTv=viewBinding.resultTv;
        resultTag=viewBinding.resultTag;

        createPaymentBtn=viewBinding.mcCreatePaymentBtn;
        createPaymentBtn.setOnClickListener(this);

        retrievePaymentBtn=viewBinding.mcRetrievePaymentBtn;
        retrievePaymentBtn.setOnClickListener(this);

        confirmPaymentBtn=viewBinding.mcConfirmPaymentBtn;
        confirmPaymentBtn.setOnClickListener(this);
    }

    /**
     * create Payment
     */
    private void createPayment() {
        RequestCreatePayment createPayment = TestRequestData.Companion.getTestRequestPayment();
        if (createPayment.getConfirmationMethod() == RequestCreatePayment.ConfirmationMethod.Automatic){
            if (TextUtils.isEmpty(createPayment.getPaymentMethod())){
                showToast(getString(R.string.payment_method_empty_str));
                return;
            }
        }
        showLoadingDialog();
        moneyCollect.createPayment(createPayment, new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {
                        dismissLoadingDialog();
                        payment=result;
                        resultTag.setText(R.string.create_payment_success_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.create_payment_error_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                    }
                }
        );
    }

    /**
     * confirm Payment
     */
    private void confirmPayment() {
        if (TextUtils.isEmpty(payment.getPaymentMethod())) {
            showToast(getString(R.string.payment_method_empty_str));
            return;
        }
        if (TextUtils.isEmpty(payment.getId())) {
            showToast(getString(R.string.payment_id_empty_str));
            return;
        }
        if (TextUtils.isEmpty(payment.getClientSecret())) {
            showToast(getString(R.string.payment_client_secret_empty_str));
            return;
        }
        showLoadingDialog();
        RequestConfirmPayment requestConfirmPayment = new RequestConfirmPayment(
                BigInteger.valueOf(payment.getAmount()),
                payment.getCurrency(),
                payment.getId(),
                payment.getIp(),
                payment.getNotifyUrl(),
                payment.getPaymentMethod(),
                payment.getReceiptEmail(),
                payment.getReturnUrl(),
                payment.getSetupFutureUsage(),
                new RequestConfirmPayment.Shipping(
                        TestRequestData.Companion.getAddress(),
                        TestRequestData.Companion.getFirstName(),
                        TestRequestData.Companion.getLastName(),
                        TestRequestData.Companion.getPhone()
                ),
               TestRequestData.Companion.getWebsite()
        );
        moneyCollect.confirmPayment(requestConfirmPayment, payment.getClientSecret(),
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.confirm_payment_success_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.confirm_payment_error_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                    }
                }
        );
    }

    /**
     * retrieve Payment
     */
    private void retrievePayment() {
        if (TextUtils.isEmpty(payment.getId())) {
            showToast(getString(R.string.payment_id_empty_str));
            return;
        }

        showLoadingDialog();
        moneyCollect.retrievePayment(payment.getId(), payment.getClientSecret(),
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.retrive_payment_success_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        resultTag.setText(R.string.retrieve_payment_error_str);
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                    }
                }
        );
    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(view.getId(), 800)) {
                if (view.getId() ==  R.id.mc_create_payment_btn){
                    createPayment();
                }else if (view.getId() ==  R.id.mc_confirm_payment_btn){
                    if (payment ==null ){
                        showToast(getString(R.string.payment_empty_str));
                        return;
                    }
                    if (TextUtils.isEmpty(payment.getId())){
                        showToast(getString(R.string.payment_id_empty_str));
                        return;
                    }
                    confirmPayment();
                }else if (view.getId() ==  R.id.mc_retrieve_payment_btn){
                    if (payment ==null ){
                        showToast(getString(R.string.payment_empty_str));
                        return;
                    }
                    if (TextUtils.isEmpty(payment.getId())){
                        showToast(getString(R.string.payment_id_empty_str));
                        return;
                    }
                    retrievePayment();
                }
            }
        }
    }
}
