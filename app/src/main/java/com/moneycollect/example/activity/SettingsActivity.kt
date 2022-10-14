package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.moneycollect.android.model.Address
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.SettingsActivityBinding
import com.moneycollect.example.utils.CheckoutCreditCardCurrency
import java.math.BigInteger


class SettingsActivity : AppCompatActivity() {
    private var viewBinding: SettingsActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, ConfigurationFragment())
            .commit()

        var backIconIv = viewBinding!!.backIcon
        backIconIv.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    class ConfigurationFragment : PreferenceFragmentCompat(){

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val countryEtPreference= findPreference<EditTextPreference>(resources.getString(R.string.country_key))
            val currencyEtPreference= findPreference<EditTextPreference>(resources.getString(R.string.currency_key))
//            val amountEtPreference= findPreference<EditTextPreference>(resources.getString(R.string.amount_key))
            val channelEtPreference= findPreference<EditTextPreference>(resources.getString(R.string.channel_key))

            val addPMEtPreference= findPreference<EditTextPreference>(resources.getString(R.string.add_local_payment_method_key))

            countryEtPreference?.setOnPreferenceChangeListener { preference, newValue ->
                changeCountry(newValue = newValue as String,countryEtPreference)
            }

            currencyEtPreference?.setOnPreferenceChangeListener { preference, newValue ->
                changeCurrency(newValue = newValue as String,currencyEtPreference)
            }

//            amountEtPreference?.setOnPreferenceChangeListener { preference, newValue ->
//                changeAmount(newValue = newValue,amountEtPreference)
//            }

            channelEtPreference?.setOnPreferenceChangeListener { preference, newValue ->
                changeChannel(newValue = newValue as String,channelEtPreference)
            }

//            addPMEtPreference?.setOnPreferenceChangeListener { preference, newValue ->
//                addPaymentMethod(newValue = newValue as String,addPMEtPreference)
//            }

            countryEtPreference?.text=TestRequestData.country
            currencyEtPreference?.text=TestRequestData.currency
//            amountEtPreference?.text=TestRequestData.amount.toString()
            channelEtPreference?.text=TestRequestData.fromChannel
        }

        private fun addPaymentMethod(newValue: String, addPMEtPreference: EditTextPreference?):Boolean{
            var  testAddPaymentMethod = PaymentMethod(
                "",
                "",
                "",
                newValue,
                null,
                null
            )
            var testLocalBankList=arrayListOf<PaymentMethod>()
            testLocalBankList.addAll(TestRequestData.testLocalBankList)
            testLocalBankList.add(testAddPaymentMethod)
            TestRequestData.testLocalBankList=testLocalBankList

            var testAllBankList= arrayListOf<PaymentMethod>()
            testAllBankList.addAll(TestRequestData.testAllBankList)
            testAllBankList.add(testAddPaymentMethod)
            TestRequestData.testAllBankList=testAllBankList
            return false
        }

        @SuppressLint("VisibleForTests")
        private fun changeCountry(newValue: String, countryEtPreference: EditTextPreference):Boolean{
            countryEtPreference.text=newValue
            TestRequestData.country = newValue
            var address= Address(
                line1 = TestRequestData.address.line1,
                line2 = TestRequestData.address.line2,
                city = TestRequestData.address.city,
                state = TestRequestData.address.state,
                postalCode = TestRequestData.address.postalCode,
                country = newValue,
            )
            TestRequestData.address=address
            TestRequestData.testCustomer.address=address
            TestRequestData.testCustomer.shipping?.address=address
            TestRequestData.testBilling.address=address
            TestRequestData.testRequestPayment.shipping?.address=address
            TestRequestData.testConfirmPayment.shipping?.address=address
            return false
        }

        private fun changeCurrency(newValue: String, currencyEtPreference: EditTextPreference):Boolean{
            currencyEtPreference.text=newValue
            TestRequestData.currency = newValue
            TestRequestData.testRequestPayment.currency = newValue
            TestRequestData.testConfirmPayment.currency = newValue
            return false
        }

        private fun changeAmount(newValue: Any, amountEtPreference: EditTextPreference):Boolean{
            amountEtPreference.text=newValue.toString()
            TestRequestData.amount = BigInteger(newValue.toString())
            TestRequestData.testRequestPayment.amount = BigInteger(newValue.toString())
            TestRequestData.testConfirmPayment.amount = BigInteger(newValue.toString())
            for (item in TestRequestData.lineItems) {
                item.amount=BigInteger(newValue.toString())
            }
            return false
        }

        private fun changeChannel(newValue: String, channelEtPreference: EditTextPreference):Boolean{
            channelEtPreference.text=newValue
            TestRequestData.fromChannel = newValue
            TestRequestData.testRequestPayment.fromChannel = newValue
            return false
        }
    }
}