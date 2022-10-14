package com.moneycollect.example_java.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moneycollect.android.model.MoneyCollectContentResultParams;
import com.moneycollect.android.model.MoneyCollectContentViewParams;
import com.moneycollect.android.model.MoneyCollectNameParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface;
import com.moneycollect.android.ui.view.MoneyCollectContentView;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.FragmentAddpaymentLayoutBinding;
import com.moneycollect.example_java.BaseExampleFragment;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.activity.PayCardActivity;
import com.moneycollect.example_java.activity.ValidationWebActivity;
import com.moneycollect.example_java.utils.TempUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.FAULT;
import static com.moneycollect.android.model.enumeration.MoneyCollectContentStyleCheck.SUCCESS;

/**
 * {@link AddWithPaymentFragment}
 * If you add the card is not set in the future use will only payment,otherwise provide pay for use in the future.
 */
public class AddWithPaymentFragment extends BaseExampleFragment implements View.OnClickListener {

    private FragmentAddpaymentLayoutBinding viewBinding;

    /***  [MoneyCollectContentView]*/
    private MoneyCollectContentView moneyCollectContentView;

    /*** [MoneyCollectResultBackInterface]*/
    private MoneyCollectResultBackInterface moneyCollectResultBackInterface;

    // PaymentModel (PAY)
    private MoneyCollectPaymentModel currentModel = MoneyCollectPaymentModel.PAY;

    //RequestCreatePayment params for pay
    private RequestCreatePayment currentRequestCreatePayment;
    //RequestConfirmPayment params for pay
    private RequestConfirmPayment currentRequestConfirmPayment;
    //RequestPaymentMethod params for pay
    private RequestPaymentMethod currentRequestCreatePaymentMethod;
    //SupportBankList params for pay
    private List<Integer> supportBankList;

    private static String SAVE_PAYMENT = "SaveWithPaymentCardFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentAddpaymentLayoutBinding.inflate(getLayoutInflater(), container, false);
        initUi();
        return viewBinding.getRoot();
    }

    private void initUi() {
        if (getArguments() != null) {
            currentModel = (MoneyCollectPaymentModel) getArguments().getSerializable(Constant.CURRENT_PAYMENT_MODEL);
            currentRequestCreatePayment = getArguments().getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG);
            currentRequestConfirmPayment = getArguments().getParcelable(Constant.CONFIRM_PAYMENT_REQUEST_TAG);
            currentRequestCreatePaymentMethod = getArguments().getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG);
            supportBankList = (ArrayList) getArguments().getSerializable(Constant.SUPPORT_BANK_LIST_TAG);
        }

        moneyCollectContentView = viewBinding.mcAddContentWidget;
        moneyCollectResultBackInterface = moneyCollectContentView.gainMoneyCollectResultBackInterface();
        MoneyCollectContentViewParams.Builder moneyCollectContentViewParamsBuilder = new MoneyCollectContentViewParams.Builder()
                .activity(getActivity())
                .moneyCollectPaymentModel(currentModel)
                .supportCardViewList(supportBankList)
                .toolbarLayoutVisibility(View.VISIBLE)
                .addClickListener(this);

        if (currentRequestCreatePaymentMethod != null && currentRequestCreatePaymentMethod.billingDetails != null) {
            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.firstName)
                    || !TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.lastName)) {
                MoneyCollectNameParams moneyCollectNameParams = new MoneyCollectNameParams.Builder()
                        .firstName(currentRequestCreatePaymentMethod.billingDetails.firstName)
                        .lastName(currentRequestCreatePaymentMethod.billingDetails.lastName)
                        .build();
                moneyCollectContentViewParamsBuilder.nameParams(moneyCollectNameParams);
            }

            if (!TextUtils.isEmpty(currentRequestCreatePaymentMethod.billingDetails.email)) {
                moneyCollectContentViewParamsBuilder.email(currentRequestCreatePaymentMethod.billingDetails.email);
            }
        }
        moneyCollectContentView.setMoneyCollectContentViewParams(moneyCollectContentViewParamsBuilder.build());

        //Listening to pay button return data, further request
        if (getActivity() != null) {
            moneyCollectContentView.getContentResultParamsLiveData().observe(getActivity(),
                    result -> {
                        if (result!=null && result.getStatus() != null) {
                            if (result.getStatus().equals(SUCCESS)) {
                                dealData(result);
                            } else if (result.getStatus().equals(FAULT)) {
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
        if (currentRequestCreatePaymentMethod != null) {
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
        String error = TempUtils.checkRequestPaymentMethodData(getActivity(), requestPaymentMethod);
        if (!TextUtils.isEmpty(error)) {
            moneyCollectResultBackInterface.failExceptionBack(error);
            return;
        }
        moneyCollect.createPaymentMethod(getActivity(), requestPaymentMethod, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NotNull PaymentMethod result) {
                createPayment(result, moneyCollectContentResultParams);
            }

            @Override
            public void onError(@NotNull Exception e) {
                moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
            }
        });
    }

    /**
     * create payment
     */
    private void createPayment(PaymentMethod paymentMethod, MoneyCollectContentResultParams moneyCollectContentResultParams) {
        if (currentRequestCreatePayment == null) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.request_create_payment_empty_str));
            return;
        }
        if (TextUtils.isEmpty(currentRequestCreatePayment.getCustomerId())) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.customer_id_empty_str));
            return;
        }
        if (currentRequestCreatePayment != null) {
            RequestCreatePayment requestCreatePayment = new RequestCreatePayment(
                    currentRequestCreatePayment.getAutomaticPaymentMethods(),
                    currentRequestCreatePayment.getAmount(),
                    false,
                    currentRequestCreatePayment.getConfirmationMethod(),
                    currentRequestCreatePayment.getCurrency(),
                    currentRequestCreatePayment.getCustomerId(),
                    currentRequestCreatePayment.getDescription(),
                    currentRequestCreatePayment.getFromChannel(),
                    currentRequestCreatePayment.getIp(),
                    currentRequestCreatePayment.getNotifyUrl(),
                    currentRequestCreatePayment.getOrderNo(),
                    paymentMethod.id,
                    TestRequestData.Companion.getPaymentMethodTypes(),
                    currentRequestCreatePayment.getPreAuth(),
                    currentRequestCreatePayment.getReceiptEmail(),
                    currentRequestCreatePayment.getReturnUrl(),
                    moneyCollectContentResultParams.isFutureUse(),
                    new RequestCreatePayment.Shipping(
                            currentRequestCreatePayment.getShipping().address,
                            currentRequestCreatePayment.getShipping().firstName,
                            currentRequestCreatePayment.getShipping().lastName,
                            currentRequestCreatePayment.getShipping().phone
                    ),
                    currentRequestCreatePayment.getLineItems(),
                    currentRequestCreatePayment.getStatementDescriptor(),
                    currentRequestCreatePayment.getStatementDescriptorSuffix(),
                    currentRequestCreatePayment.getUserAgent(),
                    currentRequestCreatePayment.getWebsite()
            );
            if (requestCreatePayment.getConfirmationMethod() == RequestCreatePayment.ConfirmationMethod.Automatic) {
                if (TextUtils.isEmpty(requestCreatePayment.getPaymentMethod())) {
                    moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_method_empty_str));
                    return;
                }
            }
            moneyCollect.createPayment(requestCreatePayment, new ApiResultCallback<Payment>() {
                @Override
                public void onSuccess(@NotNull Payment result) {
                    confirmPayment(result, paymentMethod);
                }

                @Override
                public void onError(@NotNull Exception e) {
                    moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
                }
            });
        }
    }

    /**
     * confirm payment
     */
    private void confirmPayment(Payment payment, PaymentMethod paymentMethod) {
        if (currentRequestConfirmPayment == null) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.request_confirm_payment_empty_str));
            return;
        }
        if (TextUtils.isEmpty(payment.getPaymentMethod())) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_method_empty_str));
            return;
        }
        if (TextUtils.isEmpty(payment.getId())) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_id_empty_str));
            return;
        }
        if (TextUtils.isEmpty(payment.getClientSecret())) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_client_secret_empty_str));
            return;
        }
        if (currentRequestConfirmPayment != null) {
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
                            currentRequestConfirmPayment.getShipping().address,
                            currentRequestConfirmPayment.getShipping().firstName,
                            currentRequestConfirmPayment.getShipping().lastName,
                            currentRequestConfirmPayment.getShipping().phone
                    ), currentRequestConfirmPayment.getWebsite()
            );

            moneyCollect.confirmPayment(requestConfirmPayment, payment.getClientSecret(), new ApiResultCallback<Payment>() {
                        @Override
                        public void onSuccess(@NotNull Payment result) {
                            if (result.getNextAction() != null) {
                                String redirectToUrl = result.getNextAction().redirectToUrl;
                                if (!TextUtils.isEmpty(redirectToUrl)) {
                                    Intent intent = new Intent(getActivity(), ValidationWebActivity.class);
                                    intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl);
                                    intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.getId());
                                    intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, result.getClientSecret());
                                    startActivityLauncher.launch(intent);
                                } else {
                                    moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_PENDING_MESSAGE);
                                }
                            } else {
                                String status = result.getStatus();
                                if (status != null) {
                                    switch (status) {
                                        case Constant.PAYMENT_SUCCEEDED:
                                            Intent intent = new Intent();
                                            intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, result);
                                            getActivity().setResult(Constant.PAYMENT_RESULT_CODE, intent);
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(true, "");
                                            break;
                                        case Constant.PAYMENT_FAILED:
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, result.getErrorMessage());
                                            break;
                                        case Constant.PAYMENT_UN_CAPTURED:
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_UN_CAPTURED_MESSAGE);
                                            break;
                                        case Constant.PAYMENT_PENDING:
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_PENDING_MESSAGE);
                                            break;
                                        case Constant.PAYMENT_CANCELED:
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_CANCELED_MESSAGE);
                                            break;
                                        default:
                                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_PENDING_MESSAGE);
                                            break;
                                    }
                                } else {
                                    moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_PENDING_MESSAGE);
                                }
                            }
                        }

                        @Override
                        public void onError(@NotNull Exception e) {
                            moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
                        }
                    }
            );
        }
    }

    private ActivityResultLauncher<Intent> startActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null && result.getResultCode() == Constant.WEB_RESULT_CODE) {
                        String resultStr = "";
                        Intent dataIntent = result.getData();
                        Payment payment = null;
                        if (dataIntent != null) {
                            resultStr = dataIntent.getStringExtra(Constant.WEB_RESULT_TAG);
                            payment = dataIntent.getParcelableExtra(Constant.PAYMENT_RESULT_PAYMENT);
                        }
                        if (TextUtils.isEmpty(resultStr)) {
                            Intent intent = new Intent();
                            intent.putExtra(Constant.WEB_RESULT_TAG, resultStr);
                            intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, payment);
                            if (getActivity()!=null) {
                                getActivity().setResult(Constant.PAYMENT_RESULT_CODE, intent);
                            }
                            moneyCollectResultBackInterface.paymentConfirmResultBack(true, "");
                        } else {
                            moneyCollectResultBackInterface.failExceptionBack(resultStr);
                        }
                    }
                }
            }
    );


    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        if (view != null && activity != null) {
            if (view.getId() == moneyCollectContentView.getToolbarBackIcon().getId() && !moneyCollectContentView.isRequestLoading()) {
                TempUtils.hideKeyboard(activity);
                if (activity instanceof PayCardActivity) {
                    ((PayCardActivity)activity).switchContent(SAVE_PAYMENT);
                }
            }
        }
    }
}
