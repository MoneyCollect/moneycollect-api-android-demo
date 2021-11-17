package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.moneycollect.android.MoneyCollect;
import com.moneycollect.android.MoneyCollectFactory;
import com.moneycollect.android.model.MoneyCollectButtonViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestConfirmPayment;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivityPaymentSheetCustomDemoBinding;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.CurrencyUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.moneycollect.example_java.utils.CardImageUtils.setCardImg;

public class PaymentSheetCustomDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "PaymentSheetCustomDemoActivity_PaymentResult";

    //button type (pay)
    private MoneyCollectPaymentModel moneyCollectPaymentModel = MoneyCollectPaymentModel.PAY;

    // Current Currency Unit
    private String currencyUnit = TestRequestData.Companion.getCurrency();

    private ActivityPaymentSheetCustomDemoBinding viewBinding;
    private ImageView backIconIv;
    private TextView title;
    private AppBarLayout appBarLayout;
    private MoneyCollect moneyCollect;

    private PaymentSheetCustomDemoAdapter paymentSheetCustomDemoAdapter = null;

    private List<PaymentSheetCustomDemoAdapter.Item> checkedItem = new ArrayList<>();

    //loading active
    private boolean isLoadingAnimStatus = false;

    //paymentMethod for pay
    private PaymentMethod paymentMethod;

    //currentRequestCreatePayment for pay
    private RequestCreatePayment currentRequestCreatePayment;

    //currentRequestConfirmPayment for pay
    private RequestConfirmPayment currentRequestConfirmPayment;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        moneyCollect = new MoneyCollectFactory(getApplication()).create();
        viewBinding = ActivityPaymentSheetCustomDemoBinding.inflate(getLayoutInflater());
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
        MoneyCollectButtonViewParams params = new MoneyCollectButtonViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(moneyCollectPaymentModel)
                .build();
        viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewParams(params);
        viewBinding.sheetPaymentMethodSelectCl.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewBinding.sheetExpandedMenuRl.setHasFixedSize(true);
        viewBinding.sheetExpandedMenuRl.setLayoutManager(linearLayoutManager);
        paymentSheetCustomDemoAdapter = new PaymentSheetCustomDemoAdapter(this);
        viewBinding.sheetExpandedMenuRl.setAdapter(paymentSheetCustomDemoAdapter);
        checkedItem.addAll(paymentSheetCustomDemoAdapter.items);
        paymentSheetCustomDemoAdapter.setOnKotlinItemClickListener(new IKotlinCustomItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
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
            }
        });
        reCalcuCheckoutAmount();
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

    @Override
    public void onClick(View v) {
        if (v != null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(v.getId(), 800)) {
                if (v.getId() == R.id.back_icon) {
                    finish();
                } else if (v.getId() == viewBinding.paymentCheckoutBtn.getCardConfirmButton().getId()) {

                    if (isLoadingAnimStatus) {
                        viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                    }
                    isLoadingAnimStatus = true;
                    viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(false);
                    viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewContext(null);
                    viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                    viewBinding.paymentCheckoutBtn.showAnimByPaymentHolding();

                    //build the data of RequestCreatePayment
                    currentRequestCreatePayment = TestRequestData.Companion.getTestRequestPayment();
                    currentRequestConfirmPayment = TestRequestData.Companion.getTestConfirmPayment();
                    currentRequestCreatePayment.setLineItems(formatlineItems((ArrayList<PaymentSheetCustomDemoAdapter.Item>) checkedItem));
                    BigDecimal numamount = new BigDecimal(
                            (viewBinding.paymentCheckoutAmount.getText().toString()).replace(
                                    CurrencyUtils.getCurrencyUnitTag(currencyUnit, PaymentSheetCustomDemoActivity.this),
                                    "")
                    );
                    BigDecimal numUnit = CurrencyUtils.getCurrencyTransnum(currencyUnit);
                    BigDecimal amount = numamount.multiply(numUnit);
                    currentRequestCreatePayment.setAmount(amount.toBigInteger());
                    currentRequestConfirmPayment.setAmount(amount.toBigInteger());
                    // pay bill
                    if (paymentMethod != null) {
                        createPayment(paymentMethod);
                    }
                } else if (v.getId() == viewBinding.sheetPaymentMethodSelectCl.getId()) {
                    // jump the list of card
                    Intent intent = new Intent(this, SaveCardActivity.class);
                    Bundle bundle = new Bundle();

                    RequestPaymentMethod testRequestPaymentMethod = TestRequestData.Companion.getTestRequestPaymentMethod();
                    //pass currentPaymentModel
                    bundle.putSerializable(
                            Constant.CURRENT_PAYMENT_MODEL,
                            MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD
                    );
                    //pass currentId
                    bundle.putString(
                            Constant.CUSTOMER_ID_TAG,
                            TestRequestData.Companion.getCustomerId()
                    );
                    //pass RequestPaymentMethod
                    bundle.putParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG, testRequestPaymentMethod);
                    //pass supportBankList
                    bundle.putSerializable(Constant.SUPPORT_BANK_LIST_TAG, TestRequestData.Companion.getTestBankIvList());
                    intent.putExtra(Constant.CURRENT_PAYMENT_BUNDLE, bundle);
                    startActivityLauncher.launch(intent);
                }
            }
        }
    }

    /**
     * create payment
     */
    private void createPayment(PaymentMethod paymentMethod) {

        RequestCreatePayment requestCreatePayment = new RequestCreatePayment(
                currentRequestCreatePayment.getAmount(),
                currentRequestCreatePayment.getConfirmationMethod(),
                currentRequestCreatePayment.getCurrency(),
                currentRequestCreatePayment.getCustomerId(),
                currentRequestCreatePayment.getDescription(),
                currentRequestCreatePayment.getIp(),
                currentRequestCreatePayment.getNotifyUrl(),
                currentRequestCreatePayment.getOrderNo(),
                paymentMethod.id,
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
                currentRequestCreatePayment.getWebsite()
        );

        moneyCollect.createPayment(requestCreatePayment, new ApiResultCallback<Payment>() {
            @Override
            public void onSuccess(@NotNull Payment result) {
                confirmPayment(result, paymentMethod);
            }

            @Override
            public void onError(@NotNull Exception e) {
                isLoadingAnimStatus = false;
                viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
                viewBinding.paymentErrorMessageTv.setVisibility(View.VISIBLE);
                viewBinding.paymentErrorMessageTv.setText(e.getMessage());
            }
        });

    }

    /**
     * confirm payment
     */
    private void confirmPayment(Payment payment, PaymentMethod paymentMethod) {
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
                    public void onSuccess(@NotNull Payment payment) {
                        isLoadingAnimStatus = false;
                        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
                        //	Status: succeeded,uncaptured,pending,failed,canceled
                        if (payment.getNextAction() != null) {
                            String redirectToUrl = payment.getNextAction().redirectToUrl;
                            if (!TextUtils.isEmpty(redirectToUrl)) {
                                Intent intent = new Intent(PaymentSheetCustomDemoActivity.this, ValidationWebActivity.class);
                                intent.putExtra(Constant.VALIDATION_PARAM_URL, redirectToUrl);
                                intent.putExtra(Constant.VALIDATION_PAYMENT_ID, payment.getId());
                                intent.putExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET, payment.getClientSecret());
                                startActivityLauncher.launch(intent);
                            } else {
                                viewBinding.paymentErrorMessageTv.setText(Constant.PAYMENT_PENDING_MESSAGE);
                            }
                        } else {
                            if (payment.getStatus().equals(Constant.PAYMENT_SUCCEEDED)) {
                                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewContext(null);
                                viewBinding.paymentCheckoutBtn.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                                viewBinding.paymentCheckoutBtn.showAnimByPaymentCompleteAndRefresh();
                            } else {
                                viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                            }
                            if (payment != null && payment.getStatus() != null) {
                                switch (payment.getStatus()) {
                                    case Constant.PAYMENT_SUCCEEDED:
                                        break;
                                    case Constant.PAYMENT_FAILED:
                                        viewBinding.paymentErrorMessageTv.setText(payment.getErrorMessage());
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
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        isLoadingAnimStatus = false;
                        viewBinding.paymentCheckoutBtn.stopPaymentAnim();
                        viewBinding.paymentCheckoutBtn.setCardConfirmButtonStatus(true);
                        viewBinding.paymentErrorMessageTv.setVisibility(View.VISIBLE);
                        viewBinding.paymentErrorMessageTv.setText(e.getMessage());
                    }
                }
        );

    }

    class PaymentSheetCustomDemoAdapter extends RecyclerView.Adapter<PaymentSheetCustomDemoAdapter.PaymentSheetCustomDemoViewHolder> {

        private List<PaymentSheetCustomDemoAdapter.Item> items = new ArrayList<>();

        private PaymentSheetCustomDemoActivity activity;

        private IKotlinCustomItemClickListener itemClickListener;

        public PaymentSheetCustomDemoAdapter(PaymentSheetCustomDemoActivity activity) {
            this.activity = activity;
            initList();
        }

        private void initList() {
            items.add(new Item(
                    R.mipmap.icon_payment_goods_one,
                    "Waterproof Smartwatch A5",
                    "109.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
            items.add(new Item(
                    R.mipmap.icon_payment_goods_two,
                    "GPS Smartwatch T3",
                    "11069.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
            items.add(new Item(
                    R.mipmap.icon_payment_goods_three,
                    "GPS Smartwatch T2",
                    "59.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
            items.add(new Item(
                    R.mipmap.icon_payment_goods_four,
                    "Waterproof Smartwatch A6",
                    "385.00",
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

        @Override
        public void onBindViewHolder(@NonNull @NotNull PaymentSheetCustomDemoViewHolder holder, int position) {
            holder.iconIv.setImageResource(items.get(position).images);
            holder.nameTv.setText(items.get(position).name);
            holder.priceTv.setText(CurrencyUtils.getCurrencyUnitTag(activity.currencyUnit, activity) + items.get(position).amount);
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
            PaymentSheetCustomDemoActivity.this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onActivityResult(ActivityResult result) {
                    //selected paymentmethods's list
                    if (result.getResultCode() == Constant.SAVE_RESULT_CODE) {
                        if (result != null && result.getData() != null) {
                            paymentMethod =
                                    result.getData().getParcelableExtra(Constant.SAVE_PAYMENT_METHOD);
                        }
                        if (paymentMethod != null) {
                            viewBinding.sheetPaymentMethodSelectTv.setTextColor(getResources().getColor(R.color.color_333333));
                            viewBinding.sheetPaymentMethodSelectIv.setVisibility(View.VISIBLE);
                            setCardImg(
                                    PaymentSheetCustomDemoActivity.this,
                                    viewBinding.sheetPaymentMethodSelectIv,
                                    paymentMethod.card.brand
                            );
                            viewBinding.sheetPaymentMethodSelectTv.setText(
                                    String.format(
                                            " ···· %s",
                                            paymentMethod.card.last4
                                    )
                            );
                        }
                        //add a paymentmethod
                    } else if (result.getResultCode() == Constant.ADD_RESULT_CODE) {
                        if (result != null && result.getData() != null) {
                            paymentMethod =
                                    result.getData().getParcelableExtra(Constant.ADD_PAYMENT_METHOD);
                        }
                        if (paymentMethod != null) {
                            viewBinding.sheetPaymentMethodSelectTv.setTextColor(getResources().getColor(R.color.color_333333));
                            viewBinding.sheetPaymentMethodSelectIv.setVisibility(View.VISIBLE);
                            setCardImg(
                                    PaymentSheetCustomDemoActivity.this,
                                    viewBinding.sheetPaymentMethodSelectIv,
                                    paymentMethod.card.brand
                            );
                            viewBinding.sheetPaymentMethodSelectTv.setText(
                                    String.format(
                                            " ···· %s",
                                            paymentMethod.card.last4
                                    )
                            );
                        }
                    } else if (result.getResultCode() == Constant.WEB_RESULT_CODE) {
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
                            viewBinding.paymentErrorMessageTv.setVisibility(View.VISIBLE);
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
