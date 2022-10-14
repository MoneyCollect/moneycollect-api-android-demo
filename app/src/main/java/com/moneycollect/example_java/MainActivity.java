package com.moneycollect.example_java;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneycollect.android.model.request.RequestCreateCustomer;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.android.model.response.Customer;
import com.moneycollect.android.model.response.Devide;
import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;

import com.moneycollect.example_java.activity.PaymentDemoActivity;
import com.moneycollect.example_java.activity.PaymentLocalDemoActivity;
import com.moneycollect.example_java.activity.PaymentSheetCustomDemoActivity;
import com.moneycollect.example_java.activity.PaymentSheetDemoActivity;
import com.moneycollect.example_java.databinding.ActivityMainBinding;
import com.moneycollect.example_java.activity.CreateCustomerActivity;
import com.moneycollect.example_java.activity.PaymentExampleActivity;
import com.moneycollect.example_java.activity.PaymentMethodExampleActivity;
import com.moneycollect.example_java.activity.SelectButtonTypeActivity;
import com.moneycollect.example_java.activity.SelectCustomerPaymentMethodListActivity;
import com.moneycollect.example_java.activity.SettingsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseExampleActivity {

    private String TAG = "MoneyCollect_MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        viewBinding.expandedMenu.setHasFixedSize(true);
        viewBinding.expandedMenu.setLayoutManager(linearLayoutManager);
        viewBinding.expandedMenu.setAdapter(new ExamplesAdapter(this));
    }


    private void uploadDevice() {
        moneyCollect.uploadDevice(this, new ApiResultCallback<Devide>() {
            @Override
            public void onSuccess(@NotNull Devide result) {

            }

            @Override
            public void onError(@NotNull Exception e) {

            }
        });
    }

    private void createCustomer() {
        RequestCreateCustomer requestCustomer = TestRequestData.Companion.getTestCustomer();
        moneyCollect.createCustomer(requestCustomer, new ApiResultCallback<Customer>() {
            @Override
            public void onSuccess(@NotNull Customer result) {

            }

            @Override
            public void onError(@NotNull Exception e) {

            }
        });
    }

    private void createPaymentMethod() {
        RequestPaymentMethod createPaymentMethod = new RequestPaymentMethod("card",
                TestRequestData.Companion.getTestBilling(),
                TestRequestData.Companion.getTestCard());
        moneyCollect.createPaymentMethod(this, createPaymentMethod, new ApiResultCallback<PaymentMethod>() {
            @Override
            public void onSuccess(@NotNull PaymentMethod result) {

            }

            @Override
            public void onError(@NotNull Exception e) {

            }
        });
    }

    private void attachPaymentMethod() {
        moneyCollect.attachPaymentMethod(
                TestRequestData.Companion.getPaymentMethodId(),
                TestRequestData.Companion.getCustomerId(),
                new ApiResultCallback<Object>() {
                    @Override
                    public void onError(@NotNull Exception e) {

                    }

                    @Override
                    public void onSuccess(@NotNull Object result) {

                    }
                });
    }


    private void selectAllPaymentMethods() {
        moneyCollect.selectAllPaymentMethods(TestRequestData.Companion.getCustomerId(),
                new ApiResultCallback<Object>() {
                    @Override
                    public void onSuccess(@NotNull Object result) {

                    }

                    @Override
                    public void onError(@NotNull Exception e) {

                    }
                });
    }

    private void createPayment() {
        moneyCollect.createPayment(TestRequestData.Companion.getTestRequestPayment(),
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {

                    }

                    @Override
                    public void onError(@NotNull Exception e) {

                    }
                });
    }

    private void confirmPayment() {
        String clientSecret = TestRequestData.Companion.getClientSecret();
        moneyCollect.confirmPayment(TestRequestData.Companion.getTestConfirmPayment(), clientSecret,
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {

                    }

                    @Override
                    public void onError(@NotNull Exception e) {

                    }
                });
    }


    private void retrievePayment() {
        String clientSecret = TestRequestData.Companion.getClientSecret();
        moneyCollect.retrievePayment(TestRequestData.Companion.getPaymentId(), clientSecret,
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment result) {

                    }

                    @Override
                    public void onError(@NotNull Exception e) {

                    }
                });
    }

    private void retrievePaymentMethod() {
        moneyCollect.retrievePaymentMethod(TestRequestData.Companion.getPaymentMethodId(),
                new ApiResultCallback<PaymentMethod>() {
                    @Override
                    public void onSuccess(@NotNull PaymentMethod result) {

                    }

                    @Override
                    public void onError(@NotNull Exception e) {

                    }
                });
    }

    private class ExamplesAdapter extends RecyclerView.Adapter<ExamplesAdapter.ExamplesViewHolder> {

        private Activity activity;

        public ExamplesAdapter(Activity activity) {
            this.activity = activity;
            initList();
        }

        private void initList() {
            items.add(new Item(activity.getString(R.string.payment_demo_example), PaymentDemoActivity.class));
            items.add(new Item(activity.getString(R.string.payment_local_demo_example), PaymentLocalDemoActivity.class));
            items.add(new Item(activity.getString(R.string.payment_sheet_demo_example), PaymentSheetDemoActivity.class));
            items.add(new Item(activity.getString(R.string.payment_sheet_demo_custom_example), PaymentSheetCustomDemoActivity.class));
            items.add(new Item(activity.getString(R.string.payment_card_paymentMethod_example), PaymentMethodExampleActivity.class));
            items.add(new Item(activity.getString(R.string.payment_card_create_customer_example), CreateCustomerActivity.class));
            items.add(new Item(activity.getString(R.string.payment_card_select_pm_list_example), SelectCustomerPaymentMethodListActivity.class));
            items.add(new Item(activity.getString(R.string.payment_card_payment_example), PaymentExampleActivity.class));
            items.add(new Item(activity.getString(R.string.payment_select_button_type_example), SelectButtonTypeActivity.class));
            items.add(new Item(activity.getString(R.string.payment_select_button_type_setting), SettingsActivity.class));
        }

        private List<Item> items = new ArrayList<>();

        @NonNull
        @Override
        public ExamplesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = activity.getLayoutInflater().inflate(R.layout.item_main_list_layout, parent, false);
            return new ExamplesViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull ExamplesViewHolder holder, @SuppressLint("RecyclerView") int position) {
            TextView textView = holder.textView;
            textView.setText(items.get(position).text);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(new Intent(activity, items.get(position).activityClass));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Item {
            String text;
            Class activityClass;

            public Item(String text, Class activityClass) {
                this.text = text;
                this.activityClass = activityClass;
            }
        }

        private class ExamplesViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ExamplesViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text1);
            }
        }
    }
}
