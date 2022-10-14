package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.moneycollect.android.model.Address;
import com.moneycollect.android.model.MoneyCollectCardListViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface;
import com.moneycollect.android.ui.view.MoneyCollectCardListView;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivityLocalPaymentLayoutBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.CurrencyUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;

public class LocalPaymentActivity extends BaseExampleActivity implements View.OnClickListener {

    private ActivityLocalPaymentLayoutBinding viewBinding;

    /***  [MoneyCollectCardListView]*/
    MoneyCollectCardListView cardListLayout;

    // PaymentModel (PAY)
    private MoneyCollectPaymentModel currentModel = MoneyCollectPaymentModel.PAY_LOCAL;

    /***  [MoneyCollectResultBackInterface]*/
    private MoneyCollectResultBackInterface moneyCollectResultBackInterface;

    //customerId for pay
    private String customerId;

    //RequestCreatePayment params for pay
    private RequestCreatePayment currentRequestCreatePayment;
    //RequestPaymentMethod params for pay
    private RequestPaymentMethod currentRequestCreatePaymentMethod;

    //groupList params
    private ArrayList<PaymentMethod> groupList = new ArrayList<>();
    //childList params
    private ArrayList<ArrayList<PaymentMethod>> childList = new ArrayList<>();

    //loading active
    private boolean isRequestLoading = false;

    //return
    private String returnUrl = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        viewBinding = ActivityLocalPaymentLayoutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initUi();
    }

    private void initUi() {
        Bundle bundle = getIntent().getExtras().getBundle(Constant.CURRENT_PAYMENT_BUNDLE);
        if (bundle != null) {
            currentModel = (MoneyCollectPaymentModel) bundle.getSerializable(Constant.CURRENT_PAYMENT_MODEL);
            currentRequestCreatePayment = bundle.getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG);
            currentRequestCreatePaymentMethod = bundle.getParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG);
            customerId = bundle.getString(Constant.CUSTOMER_ID_TAG);
        }
        cardListLayout = viewBinding.mcCardList;
        moneyCollectResultBackInterface = cardListLayout.gainMoneyCollectResultBackInterface();
        MoneyCollectCardListViewParams moneyCollectCardListViewParams = new MoneyCollectCardListViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(currentModel)
                .addClickListener(this)
                .build();
        cardListLayout.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams);

        cardListLayout.getPaymentMethodParamsLiveData().observe(this,
                result -> {
                    if (result != null) {
                        dealData(result);
                    } else {
                        moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_method_list_empty_str));
                    }
                }
        );
        selectAllPaymentMethods();
    }

    private void selectAllPaymentMethods() {
        cardListLayout.setVisibility(View.VISIBLE);
        childList.add(TestRequestData.Companion.getTestLocalBankList());
        if (childList.size()>0){
            cardListLayout.setPaymentButtonAndFootViewStatus(true);
            groupList.add(childList.get(0).get(0));
            cardListLayout.setDataList(groupList, childList);
        }else {
            cardListLayout.changeFootViewVisible(true);
        }
    }

    @SuppressLint("VisibleForTests")
    private void dealData(PaymentMethod paymentMethod) {
        if (currentRequestCreatePaymentMethod != null) {
            RequestPaymentMethod  requestPaymentMethod = new RequestPaymentMethod(
                    paymentMethod.type,
                    new RequestPaymentMethod.BillingDetails(
                            new Address(
                                    currentRequestCreatePaymentMethod.billingDetails.address.getCity(),
                                    currentRequestCreatePaymentMethod.billingDetails.address.getCountry(),
                                    currentRequestCreatePaymentMethod.billingDetails.address.getLine1(),
                                    currentRequestCreatePaymentMethod.billingDetails.address.getLine2(),
                                    currentRequestCreatePaymentMethod.billingDetails.address.getPostalCode(),
                                    currentRequestCreatePaymentMethod.billingDetails.address.getState()
                            ),
                            currentRequestCreatePaymentMethod.billingDetails.email,
                            currentRequestCreatePaymentMethod.billingDetails.firstName,
                            currentRequestCreatePaymentMethod.billingDetails.lastName,
                            currentRequestCreatePaymentMethod.billingDetails.phone
                    ),
                    null
            );
            createPaymentMethod(requestPaymentMethod);
        }
    }


    /**
     * create paymentMethod
     */
    private void createPaymentMethod(
            RequestPaymentMethod requestPaymentMethod) {
        if (TextUtils.isEmpty(requestPaymentMethod.type)) {
            moneyCollectResultBackInterface.failExceptionBack(getResources().getString(R.string.payment_method_type_empty_str));
            return;
        }
        moneyCollect.createPaymentMethod(this, requestPaymentMethod, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NotNull PaymentMethod result) {
                createPayment(result);
            }

            @Override
            public void onError(@NotNull Exception e) {
                moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
            }
        });
    }

    @SuppressLint("VisibleForTests")
    private void createPayment(PaymentMethod paymentMethod) {
        if (currentRequestCreatePayment == null){
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.request_create_payment_empty_str));
            return;
        }
        ArrayList<String> paymentMethodTypes=new ArrayList<>();
        paymentMethodTypes.add(paymentMethod.type);
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
                paymentMethodTypes,
                currentRequestCreatePayment.getPreAuth(),
                currentRequestCreatePayment.getReceiptEmail(),
                currentRequestCreatePayment.getReturnUrl(),
                currentRequestCreatePayment.getSetupFutureUsage(),
                new RequestCreatePayment.Shipping(
                        new Address(
                                paymentMethod.billingDetails.address.getCity(),
                                paymentMethod.billingDetails.address.getCountry(),
                                paymentMethod.billingDetails.address.getLine1(),
                                paymentMethod.billingDetails.address.getLine2(),
                                paymentMethod.billingDetails.address.getPostalCode(),
                                paymentMethod.billingDetails.address.getState()
                        ),
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
                confirmPayment(result);
            }

            @Override
            public void onError(@NotNull Exception e) {
                moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
            }
        });
    }

    /**
     * confirm Payment
     */
    private void confirmPayment(Payment payment) {
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

        if (payment.getPaymentMethodTypes() !=null && payment.getPaymentMethodTypes().contains(CurrencyUtils.CheckoutLocalCurrency.Atome.getCode())) {
            returnUrl = "asiabill://payment:8080/webpay?paymentMethod=" + payment.getPaymentMethod();
        }else {
            returnUrl = payment.getReturnUrl();
        }

        RequestConfirmPayment requestConfirmPayment = new RequestConfirmPayment(
                BigInteger.valueOf(payment.getAmount()),
                payment.getCurrency(),
                payment.getId(),
                payment.getIp(),
                payment.getNotifyUrl(),
                payment.getPaymentMethod(),
                payment.getReceiptEmail(),
                returnUrl,
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
                        dealResult(result);
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
                    }
                }
        );
    }

    private void dealResult(Payment result) {
        if (result.getNextAction() != null) {
            if (!TextUtils.isEmpty(result.getNextAction().type)) {
                String redirectToUrl = result.getNextAction().redirectToUrl;
                if (result.getNextAction().type.equals(TestRequestData.Companion.getWeChatPayNextActionType())) {
                    redirectToUrl=result.getNextAction().wechatPayH5.redirectToUrl;
                }
                if (!TextUtils.isEmpty(redirectToUrl)) {
                    Intent intent = new Intent(LocalPaymentActivity.this, ValidationLocalWebActivity.class);
                    intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl);
                    intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.getId());
                    intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, result.getClientSecret());
                    startActivityLauncher.launch(intent);
                } else {
                    moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            Constant.PAYMENT_PENDING_MESSAGE);
                }
            }else {
                moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                        Constant.PAYMENT_PENDING_MESSAGE);
            }
        } else {
            if (result.getStatus() != null) {
                switch (result.getStatus()) {
                    case Constant.PAYMENT_SUCCEEDED:
                        Intent intent = new Intent();
                        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, result);
                        setResult(Constant.PAYMENT_RESULT_CODE, intent);
                        moneyCollectResultBackInterface.paymentConfirmResultBack(true, "");
                        break;
                    case Constant.PAYMENT_FAILED:
                        moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            result.getErrorMessage());
                        break;
                    case Constant.PAYMENT_UN_CAPTURED:
                        moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            Constant.PAYMENT_UN_CAPTURED_MESSAGE);
                        break;
                    case Constant.PAYMENT_PENDING:
                        moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            Constant.PAYMENT_PENDING_MESSAGE);
                        break;
                    case Constant.PAYMENT_CANCELED:
                        moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            Constant.PAYMENT_CANCELED_MESSAGE);
                        break;
                    default:
                        moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                            Constant.PAYMENT_PENDING_MESSAGE);
                        break;
                }
            }else {
                moneyCollectResultBackInterface.paymentConfirmResultBack(false,
                        Constant.PAYMENT_PENDING_MESSAGE);
            }
        }
    }

    private ActivityResultLauncher<Intent> startActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Constant.WEB_RESULT_CODE) {
                                String resultStr = result.getData().getStringExtra(Constant.WEB_RESULT_TAG);
                                Payment payment = result.getData().getParcelableExtra(Constant.PAYMENT_RESULT_PAYMENT);
                                if (resultStr != null) {
                                    if (TextUtils.isEmpty(resultStr)){
                                        Intent intent = new Intent();
                                        intent.putExtra(Constant.WEB_RESULT_TAG, resultStr);
                                        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, payment);
                                        setResult(Constant.PAYMENT_RESULT_CODE, intent);
                                        moneyCollectResultBackInterface.paymentConfirmResultBack(true, "");
                                    }else{
                                        moneyCollectResultBackInterface.failExceptionBack(resultStr);
                                    }
                                }
                            }
                        }
                    });

    @Override
    public void onClick(View view) {
        if (view!=null) {
            if (view.getId() == cardListLayout.getToolbarBackIcon().getId() && !cardListLayout.isRequestLoading() && !isRequestLoading) {
                finish();
            }
        }
    }
}
