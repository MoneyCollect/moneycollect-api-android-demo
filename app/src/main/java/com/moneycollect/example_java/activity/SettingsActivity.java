package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.moneycollect.android.model.Address;
import com.moneycollect.android.model.request.RequestCreatePayment;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.SettingsActivityBinding;
import com.moneycollect.example_java.TestRequestData;

import java.math.BigInteger;
import java.util.ArrayList;


public class SettingsActivity extends AppCompatActivity {

    private SettingsActivityBinding viewBinding = null;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        viewBinding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, new ConfigurationFragment())
                .commit();

        ImageView backIconIv = viewBinding.backIcon;
        backIconIv.setOnClickListener(v -> finish());
    }

    public static class ConfigurationFragment extends PreferenceFragmentCompat{

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            EditTextPreference countryEtPreference= findPreference(getResources().getString(R.string.country_key));
            EditTextPreference currencyEtPreference= findPreference(getResources().getString(R.string.currency_key));
            EditTextPreference channelEtPreference= findPreference(getResources().getString(R.string.channel_key));


            countryEtPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                changeCountry((String) newValue,countryEtPreference);
                return false;
            });


            currencyEtPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                changeCurrency((String) newValue ,currencyEtPreference);
                return false;
            });

            channelEtPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                changeChannel((String) newValue ,channelEtPreference);
                return false;
            });

            countryEtPreference.setText(TestRequestData.Companion.getCountry());
            currencyEtPreference.setText(TestRequestData.Companion.getCurrency());
            channelEtPreference.setText(TestRequestData.Companion.getFromChannel());
        }

        private void addPaymentMethod(String newValue, EditTextPreference addPMEtPreference) {
            PaymentMethod testAddPaymentMethod = new PaymentMethod(
                    "",
                    "",
                    "",
                    newValue,
                    null,
                    null
            );
            ArrayList<PaymentMethod> testLocalBankList = new ArrayList<>(TestRequestData.Companion.getTestLocalBankList());
            testLocalBankList.add(testAddPaymentMethod);
            TestRequestData.Companion.setTestLocalBankList(testLocalBankList);

            ArrayList<PaymentMethod> testAllBankList = new ArrayList<>(TestRequestData.Companion.getTestAllBankList());
            testAllBankList.add(testAddPaymentMethod);
            TestRequestData.Companion.setTestAllBankList(testAllBankList);
        }

        private void changeCountry(String newValue, EditTextPreference countryEtPreference) {
            countryEtPreference.setText(newValue);
            TestRequestData.Companion.setCountry(newValue);

            @SuppressLint("VisibleForTests")
            Address address= new Address(
                    TestRequestData.Companion.getAddress().getCity(),
                    newValue,
                    TestRequestData.Companion.getAddress().getLine1(),
                    TestRequestData.Companion.getAddress().getLine2(),
                    TestRequestData.Companion.getAddress().getPostalCode(),
                    TestRequestData.Companion.getAddress().getState()
                    );
            TestRequestData.Companion.setAddress(address);
            TestRequestData.Companion.getTestCustomer().address=address;
            TestRequestData.Companion.getTestCustomer().getShipping().address=address;
            TestRequestData.Companion.getTestBilling().address=address;
            TestRequestData.Companion.getTestRequestPayment().getShipping().address=address;
            TestRequestData.Companion.getTestConfirmPayment().getShipping().address=address;
        }

        private void changeCurrency(String newValue, EditTextPreference currencyEtPreference) {
            currencyEtPreference.setText(newValue);
            TestRequestData.Companion.setCurrency(newValue);
            TestRequestData.Companion.getTestRequestPayment().setCurrency(newValue);
            TestRequestData.Companion.getTestConfirmPayment().setCurrency(newValue);
        }

        private void changeAmount(String newValue, EditTextPreference amountEtPreference) {
            amountEtPreference.setText(newValue);
            TestRequestData.Companion.setAmount(new BigInteger(newValue));
            TestRequestData.Companion.getTestRequestPayment().setAmount(new BigInteger(newValue));
            TestRequestData.Companion.getTestConfirmPayment().setAmount(new BigInteger(newValue));
            for (RequestCreatePayment.LineItems item : TestRequestData.Companion.getLineItems()) {
                item.amount=new BigInteger(newValue);
            }
        }

        private void changeChannel(String newValue, EditTextPreference channelEtPreference) {
            channelEtPreference.setText(newValue);
            TestRequestData.Companion.setFromChannel(newValue);
            TestRequestData.Companion.getTestRequestPayment().setFromChannel(newValue);
        }
    }

}
