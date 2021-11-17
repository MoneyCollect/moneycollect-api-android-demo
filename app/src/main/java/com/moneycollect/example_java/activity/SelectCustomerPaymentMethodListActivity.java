package com.moneycollect.example_java.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivitySelectPaymentMethodListLayoutBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.TempUtils;
import org.jetbrains.annotations.NotNull;

/**
 * [SelectCustomerPaymentMethodListActivity] show retrieve the payment list
 */
public class SelectCustomerPaymentMethodListActivity extends BaseExampleActivity implements View.OnClickListener{

    private ActivitySelectPaymentMethodListLayoutBinding viewBinding;
    private Button selectPMListBtn;
    private TextView resultTv;
    private TextView resultTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySelectPaymentMethodListLayoutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUi();
    }

    private void initUi() {
        resultTv=viewBinding.resultTv;
        resultTag=viewBinding.resultTag;
        selectPMListBtn=viewBinding.mcSelectPaymentMethodListBtn;
        selectPMListBtn.setOnClickListener(this);
    }

    /**
     * select all paymentMethods
     */
    private void selectAllPaymentMethods() {
        String customerId=TestRequestData.Companion.getCustomerId();
        if (TextUtils.isEmpty(customerId)){
            showToast(getString(R.string.customer_id_empty_str));
            return;
        }
        showLoadingDialog();
        moneyCollect.selectAllPaymentMethods(customerId, new ApiResultCallback<Object>() {
            @Override
            public void onSuccess(@NotNull Object result) {
                dismissLoadingDialog();
                resultTag.setText(R.string.select_payment_method_list_success_str);
                resultTv.setText(TempUtils.formatString(new Gson().toJson(result)));
            }

            @Override
            public void onError(@NotNull Exception e) {
                dismissLoadingDialog();
                resultTag.setText(R.string.select_payment_method_list_error_str);
                resultTv.setText(TempUtils.formatString(new Gson().toJson(e)));
            }
        });
    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(view.getId(), 800)) {
                if (view.getId() == R.id.mc_select_payment_method_list_btn) {
                    selectAllPaymentMethods();
                }
            }
        }
    }
}
