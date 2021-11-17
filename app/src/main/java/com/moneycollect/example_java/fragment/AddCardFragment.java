package com.moneycollect.example_java.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.moneycollect.android.model.MoneyCollectContentResultParams;
import com.moneycollect.android.model.MoneyCollectContentViewParams;
import com.moneycollect.android.model.MoneyCollectNameParams;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface;
import com.moneycollect.android.ui.view.MoneyCollectContentView;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.FragmentAddpaymentLayoutBinding;
import com.moneycollect.example_java.BaseExampleFragment;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.activity.SaveCardActivity;
import com.moneycollect.example_java.utils.TempUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.*;

/**
 * [AddCardFragment]
 * If you add the card is not set in the future use, will only call createPaymentMethod method and return a PaymentMethod,
 * otherwise create further calls after PaymentMethod attachPaymentMethod binding PaymentMethod provide pay for use in the future.
 */
public class AddCardFragment extends BaseExampleFragment implements View.OnClickListener {

    private FragmentAddpaymentLayoutBinding viewBinding;

    /***  [MoneyCollectContentView]*/
    private MoneyCollectContentView moneyCollectContentView;

    /*** [MoneyCollectResultBackInterface]*/
    private MoneyCollectResultBackInterface moneyCollectResultBackInterface;

    // PaymentModel (ATTACH_PAYMENT_METHOD)
    private MoneyCollectPaymentModel currentModel= MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD;

    //customerId for pay
    String customerId;

    //RequestPaymentMethod for pay
    private RequestPaymentMethod currentRequestCreatePaymentMethod;

    //SupportBankList params for pay
    private List<Integer> supportBankList;

    private static String IS_FUTURE_USE_ON="on";

    private static String SAVE_PAYMENT="SavePaymentFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding =  FragmentAddpaymentLayoutBinding.inflate(getLayoutInflater(), container, false);
        initUi();
        return viewBinding.getRoot();
    }

    private void initUi() {
        if (getArguments() != null) {
            currentModel = (MoneyCollectPaymentModel) getArguments().getSerializable(Constant.CURRENT_PAYMENT_MODEL);
            currentRequestCreatePaymentMethod = getArguments().getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG);
            customerId = getArguments().getString(Constant.CUSTOMER_ID_TAG);
            supportBankList= (ArrayList) getArguments().getSerializable(Constant.SUPPORT_BANK_LIST_TAG);
        }

        moneyCollectContentView= viewBinding.mcAddContentWidget;
        moneyCollectResultBackInterface =moneyCollectContentView;
        MoneyCollectContentViewParams.Builder moneyCollectContentViewParamsBuilder = new MoneyCollectContentViewParams.Builder()
                .activity(getActivity())
                .moneyCollectPaymentModel(currentModel)
                .supportCardViewList(supportBankList)
                .toolbarLayoutVisibility(View.VISIBLE)
                .addClickListener(this);

        if (currentRequestCreatePaymentMethod!=null && currentRequestCreatePaymentMethod.billingDetails!=null){
            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.firstName)
                    || !TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.lastName)){
                MoneyCollectNameParams moneyCollectNameParams = new MoneyCollectNameParams.Builder()
                        .firstName(currentRequestCreatePaymentMethod.billingDetails.firstName)
                        .lastName(currentRequestCreatePaymentMethod.billingDetails.lastName)
                        .build();
                moneyCollectContentViewParamsBuilder.nameParams(moneyCollectNameParams);
            }

            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.email)){
                moneyCollectContentViewParamsBuilder.email(currentRequestCreatePaymentMethod.billingDetails.email);
            }
        }
        moneyCollectContentView.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build());

        if (getActivity()!=null) {
            moneyCollectContentView.getContentResultParamsLiveData().observe(getActivity(),
                    result -> {
                        if (result.getStatus() != null) {
                            if (result.getStatus().equals(SUCCESS)){
                                dealData(result);
                            }else  if (result.getStatus().equals(FAULT)){
                                moneyCollectResultBackInterface.failExceptionBack(result.getDescription());
                            }
                        }
                    }
            );
        }
    }

    /**
     * deal data
     */
    private void dealData(MoneyCollectContentResultParams moneyCollectContentResultParams) {
       if (currentRequestCreatePaymentMethod!=null) {
           RequestPaymentMethod requestPaymentMethod = new RequestPaymentMethod(
                   currentRequestCreatePaymentMethod.type,
                   new RequestPaymentMethod.BillingDetails(
                           currentRequestCreatePaymentMethod.billingDetails.address,
                           moneyCollectContentResultParams.getBillingDetails().email,
                           moneyCollectContentResultParams.getBillingDetails().firstName,
                           moneyCollectContentResultParams.getBillingDetails().lastName,
                           currentRequestCreatePaymentMethod.billingDetails.phone
                   ),
           new RequestPaymentMethod.Card(
                   moneyCollectContentResultParams.getCardParams().cardNo,
                   moneyCollectContentResultParams.getCardParams().expMonth,
                   moneyCollectContentResultParams.getCardParams().expYear,
                   moneyCollectContentResultParams.getCardParams().securityCode
                )
            );
           createPaymentMethod(requestPaymentMethod, moneyCollectContentResultParams);
       }
    }


    /**
     * create paymentMethod
     */
    private void createPaymentMethod(
            RequestPaymentMethod requestPaymentMethod,
            MoneyCollectContentResultParams moneyCollectContentResultParams
    ) {

        String error=TempUtils.checkRequestPaymentMethodData(getActivity(),requestPaymentMethod);
        if (!TextUtils.isEmpty(error)){
            moneyCollectResultBackInterface.failExceptionBack(error);
            return;
        }

        showLoadingDialog();
        moneyCollect.createPaymentMethod(getActivity(), requestPaymentMethod, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NotNull PaymentMethod result) {
                if (moneyCollectContentResultParams.isFutureUse().equals(IS_FUTURE_USE_ON)) {
                    attachPaymentMethod(result);
                } else {
                    dismissLoadingDialog();
                    resultBack(result);
                }
            }

            @Override
            public void onError(@NotNull Exception e) {
                dismissLoadingDialog();
                moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
            }
        });
    }

    /**
     * return paymentMethod
     */
    private void resultBack(PaymentMethod paymentMethod){
        if (getActivity()!=null) {
            Intent intent = new Intent();
            intent.putExtra(Constant.ADD_PAYMENT_METHOD, paymentMethod);
            getActivity().setResult(Constant.ADD_RESULT_CODE, intent);
            getActivity().finish();
        }
    }

    /**
     * attach paymentMethod
     */
    private void attachPaymentMethod(PaymentMethod paymentMethod) {
        if (TextUtils.isEmpty(customerId)){
            dismissLoadingDialog();
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.customer_id_empty_str));
            return;
        }
        String paymentMethodId=paymentMethod.id;
        if (TextUtils.isEmpty(paymentMethodId)){
            dismissLoadingDialog();
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_method_id_empty_str));
            return;
        }

        moneyCollect.attachPaymentMethod(paymentMethodId, customerId,
                new ApiResultCallback<Object>() {
                    @Override
                    public void onSuccess(@NotNull Object result) {
                        dismissLoadingDialog();
                        resultBack(paymentMethod);
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
                    }
                });
    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        if (view!=null) {
            if (view.getId() == moneyCollectContentView.getToolbarBackIcon().getId() && moneyCollectContentView.isRequestLoading()==false) {
                TempUtils.hideKeyboard(getActivity());
                ((SaveCardActivity)getActivity()).switchContent(SAVE_PAYMENT);
            }
        }
    }
}
