package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.moneycollect.android.model.Address;
import com.moneycollect.android.model.MoneyCollectButtonViewParams;

import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivityPaymentDemoBinding;

import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.adapter.PaymentSelectAdapter;
import com.moneycollect.example_java.utils.CurrencyUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PaymentDemoActivity extends BaseExampleActivity implements View.OnClickListener {
    private final static String TAG = "PaymentDemoActivity";

    // PaymentModel (PAY_LOCAL)  payment model,support save and pay
    private MoneyCollectPaymentModel moneyCollectPaymentModel = MoneyCollectPaymentModel.PAY_LOCAL;

    // Current Currency Unit
    private String currencyUnit = TestRequestData.Companion.getCurrency();

    private ActivityPaymentDemoBinding viewBinding;
    private ImageView backIconIv;
    private TextView title;
    private AppBarLayout appBarLayout;

    private PaymentSheetCustomDemoAdapter paymentSheetCustomDemoAdapter = null;

    private PaymentSelectAdapter paymentSelectAdapter = null;

    private List<PaymentSheetCustomDemoAdapter.Item> checkedItem = new ArrayList<>();

    //loading active
    private boolean isLoadingAnimStatus = false;

    //paymentMethod for pay
    private PaymentMethod paymentMethod;

    //currentRequestCreatePayment for pay
    private RequestCreatePayment currentRequestCreatePayment;

    //currentRequestConfirmPayment for pay
    private RequestConfirmPayment currentRequestConfirmPayment;

    //return
    private String returnUrl = "";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        viewBinding = ActivityPaymentDemoBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initUI();
    }

    private void initUI() {
        appBarLayout = viewBinding.appBar;
        title = viewBinding.title;
        backIconIv = viewBinding.backIcon;
        backIconIv.setOnClickListener(this);
        viewBinding.paymentCheckoutBtn.getCardConfirmButton().setOnClickListener(this);
        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
        MoneyCollectButtonViewParams params = new MoneyCollectButtonViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(moneyCollectPaymentModel)
                .build();
        viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewParams(params);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewBinding.sheetExpandedMenuRl.setHasFixedSize(true);
        viewBinding.sheetExpandedMenuRl.setLayoutManager(linearLayoutManager);
        paymentSheetCustomDemoAdapter = new PaymentSheetCustomDemoAdapter(this);
        viewBinding.sheetExpandedMenuRl.setAdapter(paymentSheetCustomDemoAdapter);
        checkedItem.addAll(paymentSheetCustomDemoAdapter.items);
        paymentSheetCustomDemoAdapter.setOnKotlinItemClickListener(position -> {
            PaymentSheetCustomDemoAdapter.Item item = paymentSheetCustomDemoAdapter.getItem(position);
            if (item == null) {
                return;
            }
            item.checked = !item.checked;
            paymentSheetCustomDemoAdapter.notifyDataSetChanged();
            if (checkedItem.contains(item)) {
                checkedItem.remove(item);
            } else {
                checkedItem.add(item);
            }
            reCalcuCheckoutAmount();
        });
        reCalcuCheckoutAmount();


        LinearLayoutManager paymentSelectAdapterLayoutManager = new LinearLayoutManager(this);
        paymentSelectAdapterLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewBinding.selectPaymentRl.setHasFixedSize(true);
        viewBinding.selectPaymentRl.setLayoutManager(paymentSelectAdapterLayoutManager);
        paymentSelectAdapter = new PaymentSelectAdapter(this);
        viewBinding.selectPaymentRl.setAdapter(paymentSelectAdapter);
        if (paymentSelectAdapter.getItems()!=null && paymentSelectAdapter.getItems().size()>0) {
            paymentMethod = paymentSelectAdapter.getItems().get(0);
            if (paymentSelectAdapter.getItems().get(0).type.equals(CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode())){
                viewBinding.paymentCheckoutBtn.getCardConfirmButton().setText(getString(R.string.payment_now_continue_str));
            }else{
                viewBinding.paymentCheckoutBtn.getCardConfirmButton().setText(getString(R.string.payment_now_payment_str));
            }
        }

        paymentSelectAdapter.setOnKotlinItemClickListener(position -> {
            PaymentMethod pm=paymentSelectAdapter.getItem(position);
            if (pm!=null){
                if (!isLoadingAnimStatus && !(paymentMethod.type.equals(pm.type))) {
                    viewBinding.paymentErrorMessageTv.setText("");
                    paymentMethod = pm;
                    if (pm.type.equals(CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode())) {
                        viewBinding.paymentCheckoutBtn.getCardConfirmButton().setText(getString(R.string.payment_now_continue_str));
                    } else {
                        viewBinding.paymentCheckoutBtn.getCardConfirmButton().setText(getString(R.string.payment_now_payment_str));
                    }
                    paymentSelectAdapter.setCurrentPosition(position);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(v.getId(), 800)) {
                if (v.getId() == R.id.back_icon) {
                    finish();
                } else if (v.getId() == viewBinding.paymentCheckoutBtn.getCardConfirmButton().getId()) {
                    jumpToCreditCardPage();
                }
            }
        }
    }

    private void jumpToCreditCardPage() {
        viewBinding.paymentErrorMessageTv.setText("");
        //build the data of RequestCreatePayment
        currentRequestCreatePayment = TestRequestData.Companion.getTestRequestPayment();
        currentRequestConfirmPayment = TestRequestData.Companion.getTestConfirmPayment();
        currentRequestCreatePayment.setLineItems(formatlineItems((ArrayList<PaymentSheetCustomDemoAdapter.Item>) checkedItem));
        BigDecimal numamount = new BigDecimal(
                (viewBinding.paymentCheckoutAmount.getText().toString()).replace(
                        CurrencyUtils.getCurrencyUnitTag(currencyUnit, PaymentDemoActivity.this),
                        "")
        );
        BigDecimal numUnit = CurrencyUtils.getCurrencyTransnum(currencyUnit);
        BigDecimal amount = numamount.multiply(numUnit);
        currentRequestCreatePayment.setAmount(amount.toBigInteger());
        currentRequestConfirmPayment.setAmount(amount.toBigInteger());
        // pay bill
        if (paymentMethod != null) {
            if (paymentMethod.type.equals(CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode())) {
                Intent intent = new Intent(this, PayCardActivity.class);
                Bundle bundle = new Bundle();
                ArrayList testBankIvList = TestRequestData.Companion.getTestBankIvList();
                RequestPaymentMethod testRequestPaymentMethod = TestRequestData.Companion.getTestRequestPaymentMethod();
                //pass currentPaymentModel
                bundle.putSerializable(
                        Constant.CURRENT_PAYMENT_MODEL,
                        MoneyCollectPaymentModel.PAY
                );
                //pass RequestCreatePayment
                bundle.putParcelable(
                        Constant.CREATE_PAYMENT_REQUEST_TAG,
                        currentRequestCreatePayment
                );
                //pass RequestConfirmPayment
                bundle.putParcelable(
                        Constant.CONFIRM_PAYMENT_REQUEST_TAG,
                        currentRequestConfirmPayment
                );
                //pass currentId
                bundle.putString(
                        Constant.CUSTOMER_ID_TAG,
                        TestRequestData.Companion.getCustomerId()
                );
                //pass default RequestPaymentMethod
                bundle.putParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG, testRequestPaymentMethod);
                //pass default supportBankList
                bundle.putSerializable(Constant.SUPPORT_BANK_LIST_TAG, testBankIvList);
                intent.putExtra(Constant.CURRENT_PAYMENT_BUNDLE, bundle);
                startActivityLauncher.launch(intent);
            }else {
                if (isLoadingAnimStatus) {
                    viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                }
                isLoadingAnimStatus = true;
                viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(false);
                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewContext(null);
                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                viewBinding.paymentCheckoutBtn.showAnimByPaymentHolding();
                dealData(paymentMethod);
            }
        }

    }


    /**
     * deal data
     */
    @SuppressLint("VisibleForTests")
    private void dealData(PaymentMethod paymentMethod) {
        RequestPaymentMethod requestPM=TestRequestData.Companion.getTestRequestPaymentMethod();
        if (requestPM != null) {
            RequestPaymentMethod  requestPaymentMethod = new RequestPaymentMethod(
                    paymentMethod.type,
                    new RequestPaymentMethod.BillingDetails(
                            new Address(
                                    requestPM.billingDetails.address.getCity(),
                                    requestPM.billingDetails.address.getCountry(),
                                    requestPM.billingDetails.address.getLine1(),
                                    requestPM.billingDetails.address.getLine2(),
                                    requestPM.billingDetails.address.getPostalCode(),
                                    requestPM.billingDetails.address.getState()
                            ),
                            requestPM.billingDetails.email,
                            requestPM.billingDetails.firstName,
                            requestPM.billingDetails.lastName,
                            requestPM.billingDetails.phone
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
            viewBinding.paymentErrorMessageTv.setText(getString(R.string.payment_method_type_empty_str));
            return;
        }
        moneyCollect.createPaymentMethod(this, requestPaymentMethod, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NotNull PaymentMethod result) {
                createPayment(result);
            }

            @Override
            public void onError(@NotNull Exception e) {
                dealError(e);
            }
        });
    }


    private void dealError(Exception e){
        isLoadingAnimStatus = false;
        viewBinding.paymentCheckoutBtn.stopPaymentAnim();
        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
        viewBinding.paymentErrorMessageTv.setText(e.getMessage());
    }

    /**
     * reCalcu Checkout Amount
     */
    private void reCalcuCheckoutAmount() {
        BigDecimal amount = CurrencyUtils.getCurrencyZeroDecimalFormat(currencyUnit);
        BigDecimal amountItem;
        for (PaymentSheetCustomDemoAdapter.Item itemfor : checkedItem) {
            if (itemfor.amount != null) {
                amountItem = new BigDecimal(itemfor.amount);
                amount = amount.add(amountItem);
            }
        }
        DecimalFormat df = CurrencyUtils.getCurrencyDecimalFormat(currencyUnit);
        df.setRoundingMode(RoundingMode.HALF_UP);
        viewBinding.paymentCheckoutAmount.setText(CurrencyUtils.getCurrencyUnitTag(currencyUnit,
                this) + df.format(amount));
    }

    /**
     * format for lineItems
     */
    private List<RequestCreatePayment.LineItems> formatlineItems(ArrayList<PaymentSheetCustomDemoAdapter.Item> arrayItem) {
        List<RequestCreatePayment.LineItems> lineItems = new ArrayList<RequestCreatePayment.LineItems>();
        for (PaymentSheetCustomDemoAdapter.Item item : arrayItem) {
            if (item.amount == null) {
                continue;
            }
            BigDecimal numamount = new BigDecimal(item.amount);
            BigDecimal numUnit = CurrencyUtils.getCurrencyTransnum(item.currency);
            BigDecimal amount = numamount.multiply(numUnit);
            List<String> images = new ArrayList<>();
            images.add(String.valueOf(item.images));
            RequestCreatePayment.LineItems lineItem = new RequestCreatePayment.LineItems(
                    amount.toBigInteger(),
                    item.currency,
                    item.name,
                    images,
                    item.name,
                    item.quantity
            );
            lineItems.add(lineItem);
        }
        return lineItems;
    }


    /**
     * create payment
     */
    @SuppressLint("VisibleForTests")
    private void createPayment(PaymentMethod paymentMethod) {
        if (currentRequestCreatePayment == null){
            viewBinding.paymentErrorMessageTv.setText(getString(R.string.request_create_payment_empty_str));
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
        if (TextUtils.isEmpty(requestCreatePayment.getPaymentMethod())) {
            viewBinding.paymentErrorMessageTv.setText(R.string.payment_method_empty_str);
            return;
        }
        moneyCollect.createPayment(requestCreatePayment, new ApiResultCallback<Payment>() {
            @Override
            public void onSuccess(@NotNull Payment result) {
                confirmPayment(result);
            }

            @Override
            public void onError(@NotNull Exception e) {
                dealError(e);
            }
        });

    }


    /**
     * confirm Payment
     */
    private void confirmPayment(Payment payment) {
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
                payment.getWebsite()
        );
        moneyCollect.confirmPayment(requestConfirmPayment, payment.getClientSecret(),
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {
                        dealResult(result);
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dealError(e);
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
                    Intent intent = new Intent(PaymentDemoActivity.this, ValidationLocalWebActivity.class);
                    intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl);
                    intent.putExtra(Constant.VALIDATION_PAYMENT_ID, result.getId());
                    intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, result.getClientSecret());
                    startActivityLauncher.launch(intent);
                } else {
                    viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
                }
            }else {
                viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
            }
        } else {
            if (result.getStatus().equals(Constant.PAYMENT_SUCCEEDED)) {
                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewContext(null);
                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                viewBinding.paymentCheckoutBtn.showAnimByPaymentCompleteAndRefresh();
            } else {
                viewBinding.paymentCheckoutBtn.stopPaymentAnim();
            }
            if (result != null && result.getStatus() != null) {
                switch (result.getStatus()) {
                    case Constant.PAYMENT_SUCCEEDED:
                        break;
                    case Constant.PAYMENT_FAILED:
                        viewBinding.paymentErrorMessageTv.setText(result.getErrorMessage());
                        break;
                    case Constant.PAYMENT_UN_CAPTURED:
                        viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_UN_CAPTURED_MESSAGE);
                        break;
                    case Constant.PAYMENT_PENDING:
                        viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
                        break;
                    case Constant.PAYMENT_CANCELED:
                        viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_CANCELED_MESSAGE);
                        break;
                    default:
                        viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
                        break;
                }
            }else {
                viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
            }
        }
        isLoadingAnimStatus = false;
        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
    }


    class PaymentSheetCustomDemoAdapter extends RecyclerView.Adapter<PaymentSheetCustomDemoAdapter.PaymentSheetCustomDemoViewHolder> {

        private List<Item> items = new ArrayList<>();

        private PaymentDemoActivity activity;

        private IKotlinCustomItemClickListener itemClickListener;

        public PaymentSheetCustomDemoAdapter(PaymentDemoActivity activity) {
            this.activity = activity;
            initList();
        }

        private void initList() {
            items.add(new Item(
                    R.mipmap.icon_payment_goods_two,
                    "GPS Smartwatch T3",
                    "110.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
            items.add(new Item(
                    R.mipmap.icon_payment_goods_four,
                    "Waterproof Smartwatch A6",
                    "38.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
        }

        @NonNull
        @Override
        public PaymentSheetCustomDemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = activity.getLayoutInflater().inflate(R.layout.item_payment_sheet_goods, parent, false);
            return new PaymentSheetCustomDemoViewHolder(root);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull @NotNull PaymentSheetCustomDemoViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.iconIv.setImageResource(items.get(position).images);
            holder.nameTv.setText(items.get(position).name);
            holder.priceTv.setText(CurrencyUtils.getCurrencyUnitTag(activity.currencyUnit, activity) + CurrencyUtils.getAmountTransferNum(activity.currencyUnit,items.get(position).amount));
            if (items.get(position).checked) {
                holder.amountTv.setTextColor(activity.getResources().getColor(R.color.color_1A73E8));
            } else {
                holder.amountTv.setTextColor(activity.getResources().getColor(R.color.color_333333));
            }
            // click event
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClickListener(position);
                }
            });
        }


        public Item getItem(int i) {
            if (i <= items.size()) {
                return items.get(i);
            }
            return new Item();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // set
        public void setOnKotlinItemClickListener(IKotlinCustomItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        private class Item {

            // A list of up to 5 URLs of images for this line item, meant to be displayable to the customer.
            private Integer images;

            //nameTv The product’s name, meant to be displayable to the customer
            private String name;

            //priceTv The amount to be collected per unit of the line item
            private String amount;

            //currency	Three-letter ISO currency code.
            private String currency;

            //description The description for the line item, to be displayed on the Checkout page.
            private String description;

            //quantity
            private int quantity;

            //item is checked  /yes:true   no:false
            private boolean checked;

            public Item() {
            }

            public Item(Integer images, String name, String amount, String currency, String description, int quantity, boolean checked) {
                this.images = images;
                this.name = name;
                this.amount = amount;
                this.currency = currency;
                this.description = description;
                this.quantity = quantity;
                this.checked = checked;
            }
        }

        protected class PaymentSheetCustomDemoViewHolder extends RecyclerView.ViewHolder {

            private ImageView iconIv;
            private TextView nameTv;
            private TextView priceTv;
            private TextView amountTv;

            public PaymentSheetCustomDemoViewHolder(@NonNull View itemView) {
                super(itemView);
                iconIv = itemView.findViewById(R.id.item_payment_goods_iv);
                nameTv = itemView.findViewById(R.id.item_payment_goods_name_tv);
                priceTv = itemView.findViewById(R.id.item_payment_goods_price_tv);
                amountTv = itemView.findViewById(R.id.item_payment_goods_amount_tv);
            }
        }
    }

    //interface custom
    interface IKotlinCustomItemClickListener {
        void onItemClickListener(int position);

    }

    private ActivityResultLauncher<Intent> startActivityLauncher =
            PaymentDemoActivity.this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Constant.WEB_RESULT_CODE) {
                        String resultStr = "";
                        if (result.getData() != null) {
                            resultStr = result.getData().getStringExtra(Constant.WEB_RESULT_TAG);
                        }
                        if (TextUtils.isEmpty(resultStr)) {
                            viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewContext(null);
                            viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                            viewBinding.paymentCheckoutBtn.showAnimByPaymentCompleteAndRefresh();
                        } else {
                            isLoadingAnimStatus = false;
                            viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                            viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
                            viewBinding.paymentErrorMessageTv.setText(resultStr);
                        }
                        //3D Secure authentication resultPayment
                        Payment payment =
                                result.getData().getParcelableExtra(Constant.PAYMENT_RESULT_PAYMENT);
                        if (payment != null) {
                            switch (payment.getStatus()) {
                                case Constant.PAYMENT_SUCCEEDED:
                                    Log.e(TAG, Constant.PAYMENT_SUCCESSFUL_MESSAGE);
                                    break;
                                case Constant.PAYMENT_FAILED:
                                    Log.e(TAG, payment.getErrorMessage());
                                    break;
                                case Constant.PAYMENT_UN_CAPTURED:
                                    Log.e(TAG, Constant.PAYMENT_UN_CAPTURED_MESSAGE);
                                    break;
                                case Constant.PAYMENT_PENDING:
                                    Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE);
                                    break;
                                case Constant.PAYMENT_CANCELED:
                                    Log.e(TAG, Constant.PAYMENT_CANCELED_MESSAGE);
                                    break;
                                default:
                                    Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE);
                                    break;
                            }
                        }
                    }
                    if (paymentMethod != null) {
                        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
                    }
                }
            });
}
