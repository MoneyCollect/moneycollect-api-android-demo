package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.moneycollect.android.model.MoneyCollectContentViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestCreateCustomer;
import com.moneycollect.android.model.response.Customer;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.view.MoneyCollectContentView;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivityCreateCustomerLayoutBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.TempUtils;

import org.jetbrains.annotations.NotNull;

import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.FAULT;
import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.SUCCESS;

/**
 * [CreateCustomerActivity] show create customer sample
 */
public class CreateCustomerActivity extends BaseExampleActivity {

    private ActivityCreateCustomerLayoutBinding viewBinding;

    /***  [MoneyCollectContentView]*/
    private MoneyCollectContentView moneyCollectContentView;

    private TextView resultTv;
    private TextView resultTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCreateCustomerLayoutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUi();
    }

    private void initUi() {
        resultTv = viewBinding.resultTv;
        resultTag = viewBinding.resultTag;
        moneyCollectContentView = viewBinding.createPmWidget;

        MoneyCollectContentViewParams.Builder moneyCollectContentViewParamsBuilder =new MoneyCollectContentViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(MoneyCollectPaymentModel.CREATE_CUSTOMER)
                .toolbarLayoutVisibility(View.GONE)
                .futureUseLayoutVisible(View.GONE);
        moneyCollectContentView.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build());

        moneyCollectContentView.getContentResultParamsLiveData().observe(this, result -> {
            if (result.getStatus() != null) {
                if (result.getStatus().equals(SUCCESS)) {
                    RequestCreateCustomer requestCreateCustomer = new RequestCreateCustomer(
                            TestRequestData.Companion.getAddress(),
                            "test",
                            result.getBillingDetails().email,
                            result.getBillingDetails().firstName,
                            result.getBillingDetails().lastName,
                            TestRequestData.Companion.getPhone(),
                            new RequestCreateCustomer.Shipping(
                                    TestRequestData.Companion.getAddress(),
                                    result.getBillingDetails().firstName,
                                    result.getBillingDetails().lastName,
                                    TestRequestData.Companion.getPhone()
                            )
                    );
                    createCustomer(requestCreateCustomer);
                } else if (result.getStatus().equals(FAULT)) {
                    showToast(result.getDescription());
                }
            }
        });
    }

    /**
     * create customer
     */
    private void createCustomer(RequestCreateCustomer requestCreateCustomer) {
        String error = TempUtils.checkRequestCreateCustomerData(this, requestCreateCustomer);
        if (!TextUtils.isEmpty(error)) {
            showToast(error);
            return;
        }
        showLoadingDialog();
        moneyCollect.createCustomer(requestCreateCustomer,
                new ApiResultCallback<Customer>() {
                    @Override
                    public void onSuccess(@NotNull Customer result) {
                        dismissLoadingDialog();
                        resultTag.setText(getString(R.string.create_customer_success_str));
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
                        if (result != null && result.id != null) {
                            TestRequestData.Companion.setCustomerId(result.id);
                            TestRequestData.Companion.getTestRequestPayment().setCustomerId(result.id);
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        resultTag.setText(getString(R.string.create_customer_error_str));
                        resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
                    }
                }
        );
    }
}
