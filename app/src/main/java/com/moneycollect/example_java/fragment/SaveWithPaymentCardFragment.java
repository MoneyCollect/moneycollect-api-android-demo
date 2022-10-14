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

import com.moneycollect.android.model.MoneyCollectCardListViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.imp.MoneyCollectResultBackInterface;
import com.moneycollect.android.ui.view.MoneyCollectCardListView;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.FragmentSaveWithPaymentLayoutBinding;
import com.moneycollect.example_java.BaseExampleFragment;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.activity.PayCardActivity;
import com.moneycollect.example_java.activity.ValidationWebActivity;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * {@link SaveWithPaymentCardFragment}
 * Show the payment list,offer users pay the payment, also you can add a new card to pay jump {@link AddWithPaymentFragment}
 */
public class SaveWithPaymentCardFragment extends BaseExampleFragment implements View.OnClickListener {

    private FragmentSaveWithPaymentLayoutBinding viewBinding;

    /***  [MoneyCollectCardListView]*/
    MoneyCollectCardListView cardListLayout;

    /***  [MoneyCollectResultBackInterface]*/
    private MoneyCollectResultBackInterface moneyCollectResultBackInterface;

    // PaymentModel (PAY)
    private MoneyCollectPaymentModel currentModel = MoneyCollectPaymentModel.PAY;

    //RequestCreatePayment params for pay
    private RequestCreatePayment currentRequestCreatePayment;
    //RequestConfirmPayment params for pay
    private RequestConfirmPayment currentRequestConfirmPayment;

    //customerId for pay
    private String customerId;

    //groupList params
    private ArrayList<PaymentMethod> groupList = new ArrayList<>();
    //childList params
    private ArrayList<ArrayList<PaymentMethod>> childList = new ArrayList<>();

    //loading active
    private boolean isRequestLoading = false;

    private static String ADD_PAYMENT = "AddWithPaymentFragment";

    @androidx.annotation.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSaveWithPaymentLayoutBinding.inflate(getLayoutInflater(), container, false);
        initUi();
        return viewBinding.getRoot();
    }

    private void initUi() {
        if (getArguments() != null) {
            currentModel = (MoneyCollectPaymentModel) getArguments().getSerializable(Constant.CURRENT_PAYMENT_MODEL);
            currentRequestCreatePayment = getArguments().getParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG);
            currentRequestConfirmPayment = getArguments().getParcelable(Constant.CONFIRM_PAYMENT_REQUEST_TAG);
            customerId = getArguments().getString(Constant.CUSTOMER_ID_TAG);
        }
        cardListLayout = viewBinding.mcCardList;
        moneyCollectResultBackInterface = cardListLayout.gainMoneyCollectResultBackInterface();
        MoneyCollectCardListViewParams moneyCollectCardListViewParams = new MoneyCollectCardListViewParams.Builder()
                .activity(getActivity())
                .moneyCollectPaymentModel(currentModel)
                .addClickListener(this)
                .build();
        cardListLayout.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams);

        cardListLayout.getPaymentMethodParamsLiveData().observe(getActivity(),
                result -> {
                    if (result != null) {
                        createPayment(result);
                    } else {
                        moneyCollectResultBackInterface.failExceptionBack(getString(R.string.payment_method_list_empty_str));
                    }
                }
        );
        selectAllPaymentMethods();
    }


    /**
     * create payment
     */
    private void createPayment(PaymentMethod paymentMethod) {
        if (currentRequestCreatePayment == null) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.request_create_payment_empty_str));
            return;
        }
        if (TextUtils.isEmpty(currentRequestCreatePayment.getCustomerId())) {
            moneyCollectResultBackInterface.failExceptionBack(getString(R.string.customer_id_empty_str));
            return;
        }
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
                currentRequestCreatePayment.getSetupFutureUsage(),
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
                confirmPayment(result);
            }

            @Override
            public void onError(@NotNull Exception e) {
                moneyCollectResultBackInterface.failExceptionBack(e.getMessage());
            }
        });

    }

    /**
     * confirm payment
     */
    private void confirmPayment(Payment payment) {
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
                        if (result==null){
                            moneyCollectResultBackInterface.paymentConfirmResultBack(false, Constant.PAYMENT_PENDING_MESSAGE);
                            return;
                        }
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
                                        if (getActivity()!=null) {
                                            getActivity().setResult(Constant.PAYMENT_RESULT_CODE, intent);
                                        }
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

    private ActivityResultLauncher<Intent> startActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result == null) return;
                    if (result.getResultCode() == Constant.WEB_RESULT_CODE) {
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
     * select all paymentMethods
     */
    private void selectAllPaymentMethods() {
        if (TextUtils.isEmpty(customerId)) {
            cardListLayout.setVisibility(View.VISIBLE);
            cardListLayout.setPaymentButtonAndFootViewStatus(false);
            return;
        }
        isRequestLoading = true;
        showLoadingDialog();
        moneyCollect.selectAllPaymentMethods(customerId, new ApiResultCallback<Object>() {
            @Override
            public void onSuccess(@NotNull Object result) {
                dismissLoadingDialog();
                cardListLayout.setVisibility(View.VISIBLE);
                if (result instanceof ArrayList) {
                    if (((ArrayList) result).size() > 0) {
                        childList.add((ArrayList<PaymentMethod>) result);
                        if (childList.size() > 0) {
                            groupList.add(childList.get(0).get(0));
                            cardListLayout.setDataList(groupList, childList);
                            cardListLayout.setPaymentButtonAndFootViewStatus(true);
                        } else {
                            cardListLayout.setPaymentButtonAndFootViewStatus(false);
                        }
                    } else {
                        cardListLayout.setPaymentButtonAndFootViewStatus(false);
                    }
                } else {
                    cardListLayout.setPaymentButtonAndFootViewStatus(false);
                }
                isRequestLoading = false;
            }

            @Override
            public void onError(@NotNull Exception e) {
                dismissLoadingDialog();
                cardListLayout.setVisibility(View.VISIBLE);
                cardListLayout.setPaymentButtonAndFootViewStatus(false);
                isRequestLoading = false;
            }
        });

    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        if (view != null && activity!=null) {
            if (view.getId() == cardListLayout.getToolbarBackIcon().getId() && !cardListLayout.isRequestLoading() && !isRequestLoading) {
                activity.finish();
            } else if (view.getId() == cardListLayout.getChildFootView().getId() && !cardListLayout.isRequestLoading() && !isRequestLoading) {
                if (activity instanceof PayCardActivity) {
                    ((PayCardActivity)activity).switchContent(ADD_PAYMENT);
                }
            }
        }
    }
}
