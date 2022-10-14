package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivityPaymentSheetDemoBinding;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.CurrencyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.moneycollect.example_java.Constant.CURRENT_PAYMENT_BUNDLE;

public class PaymentLocalDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "PaymentSheetDemoActivity_PaymentResult";
    // PaymentModel (PAY)
    MoneyCollectPaymentModel currentPaymentModel = MoneyCollectPaymentModel.PAY_LOCAL;

    // Current Currency Unit
    private String currencyUnit = TestRequestData.Companion.getCurrency();
    private ActivityPaymentSheetDemoBinding viewBinding;
    private ImageView backIconIv;
    private TextView title;
    private AppBarLayout appBarLayout;
    private PaymentSheetDemoAdapter paymentSheetDemoAdapter = null;
    public List<PaymentSheetDemoAdapter.Item> checkedItem = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        viewBinding = ActivityPaymentSheetDemoBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initUI();
    }

    private void initUI() {
        appBarLayout = viewBinding.appBar;
        title = viewBinding.title;
        backIconIv = viewBinding.backIcon;
        backIconIv.setOnClickListener(this);
        viewBinding.paymentCancelBtn.setOnClickListener(this);
        viewBinding.paymentCheckoutBtn.setOnClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewBinding.sheetExpandedMenuRl.setHasFixedSize(true);
        viewBinding.sheetExpandedMenuRl.setLayoutManager(linearLayoutManager);
        paymentSheetDemoAdapter = new PaymentSheetDemoAdapter(this);
        viewBinding.sheetExpandedMenuRl.setAdapter(paymentSheetDemoAdapter);
        checkedItem.addAll(paymentSheetDemoAdapter.items);
        paymentSheetDemoAdapter.setOnKotlinItemClickListener(new IKotlinItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                PaymentSheetDemoAdapter.Item item = paymentSheetDemoAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                item.checked = !item.checked;
                paymentSheetDemoAdapter.notifyDataSetChanged();
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
        for (PaymentSheetDemoAdapter.Item itemfor : checkedItem) {
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
    private List<RequestCreatePayment.LineItems> formatlineItems(ArrayList<PaymentSheetDemoAdapter.Item> arrayItem) {
        List<RequestCreatePayment.LineItems> lineItems = new ArrayList<RequestCreatePayment.LineItems>();
        for (PaymentSheetDemoAdapter.Item item : arrayItem) {
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
                if (v.getId() == R.id.back_icon || v.getId() == R.id.payment_cancel_btn) {
                    finish();
                } else if (v.getId() == R.id.payment_checkout_btn) {
                    Intent intent = new Intent(this, LocalPaymentActivity.class);
                    Bundle bundle = new Bundle();
                    RequestCreatePayment testRequestPayment = TestRequestData.Companion.getTestRequestPayment();

                    String customerId = TestRequestData.Companion.getCustomerId();
                    RequestPaymentMethod testRaymentMethod = TestRequestData.Companion.getTestRequestPaymentMethod();
                    testRequestPayment.setLineItems(formatlineItems((ArrayList<PaymentSheetDemoAdapter.Item>) checkedItem));

                    BigDecimal numamount = new BigDecimal(
                            (viewBinding.paymentCheckoutAmount.getText().toString()).replace(
                                    CurrencyUtils.getCurrencyUnitTag(currencyUnit, PaymentLocalDemoActivity.this),
                                    ""
                            ));
                    BigDecimal numUnit = CurrencyUtils.getCurrencyTransnum(currencyUnit);
                    BigDecimal amount = numamount.multiply(numUnit);
                    testRequestPayment.setAmount(amount.toBigInteger());

                    //pass currentPaymentModel
                    bundle.putSerializable(
                            Constant.CURRENT_PAYMENT_MODEL,
                            currentPaymentModel
                    );
                    //pass RequestCreatePayment
                    bundle.putParcelable(
                            Constant.CREATE_PAYMENT_REQUEST_TAG,
                            testRequestPayment
                    );

                    //pass currentId
                    bundle.putString(
                            Constant.CUSTOMER_ID_TAG,
                            customerId
                    );
                    //pass RequestPaymentMethod
                    bundle.putParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG, testRaymentMethod);

                    intent.putExtra(CURRENT_PAYMENT_BUNDLE, bundle);
                    startActivityLauncher.launch(intent);
                }
            }
        }
    }

    private ActivityResultLauncher<Intent> startActivityLauncher =
            PaymentLocalDemoActivity.this.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onActivityResult(ActivityResult result) {
                    // resultPayment
                    if (result.getResultCode() == Constant.PAYMENT_RESULT_CODE) {
                        Payment payment =
                                result.getData().getParcelableExtra(Constant.PAYMENT_RESULT_PAYMENT);
                        if (payment != null) {
                            if(payment.getStatus() != null) {
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
                            }else {
                                Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE);
                            }
                        }
                    }
                }
            });

    class PaymentSheetDemoAdapter extends RecyclerView.Adapter<PaymentSheetDemoAdapter.PaymentSheetDemoViewHolder> {

        private List<Item> items = new ArrayList<>();

        private PaymentLocalDemoActivity activity;

        private IKotlinItemClickListener itemClickListener;

        public PaymentSheetDemoAdapter(PaymentLocalDemoActivity activity) {
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
                    "110.00",
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
                    "38.00",
                    activity.currencyUnit,
                    "Waterproof Smartwatch A5",
                    1,
                    true
            ));
        }

        @NonNull
        @Override
        public PaymentSheetDemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = activity.getLayoutInflater().inflate(R.layout.item_payment_sheet_goods, parent, false);
            return new PaymentSheetDemoViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentSheetDemoViewHolder holder, @SuppressLint("RecyclerView") int position) {
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
        public void setOnKotlinItemClickListener(IKotlinItemClickListener itemClickListener) {
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

        protected class PaymentSheetDemoViewHolder extends RecyclerView.ViewHolder {

            private ImageView iconIv;
            private TextView nameTv;
            private TextView priceTv;
            private TextView amountTv;

            public PaymentSheetDemoViewHolder(@NonNull View itemView) {
                super(itemView);
                iconIv = itemView.findViewById(R.id.item_payment_goods_iv);
                nameTv = itemView.findViewById(R.id.item_payment_goods_name_tv);
                priceTv = itemView.findViewById(R.id.item_payment_goods_price_tv);
                amountTv = itemView.findViewById(R.id.item_payment_goods_amount_tv);
            }
        }
    }

    //interface custom
    interface IKotlinItemClickListener {
        void onItemClickListener(int position);
    }
}
