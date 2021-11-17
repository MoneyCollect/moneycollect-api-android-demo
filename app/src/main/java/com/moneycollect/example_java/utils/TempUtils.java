package com.moneycollect.example_java.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.moneycollect.android.model.request.RequestCreateCustomer;
import com.moneycollect.android.model.request.RequestPaymentMethod;
import com.moneycollect.example.R;

public class TempUtils {
    public static String formatString(String text){
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }



    /**
     *
     * immSoftInput hide
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager immSoftInput = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        immSoftInput.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    /**
     * check requestPaymentMethod data
     */
    public static String checkRequestPaymentMethodData( Activity activity,RequestPaymentMethod requestPaymentMethod) {
        if (activity==null || requestPaymentMethod==null){
            return "";
        }
        if (TextUtils.isEmpty(requestPaymentMethod.type)) {
            return activity.getString(R.string.payment_method_type_empty_str);
        } else {
            if (requestPaymentMethod.type.equals("card")) {
                if (requestPaymentMethod.billingDetails == null) {
                    return activity.getString(R.string.billing_detail_empty_str);
                } else {
                    if (TextUtils.isEmpty(requestPaymentMethod.billingDetails.firstName)) {
                        return activity.getString(R.string.billing_detail_first_name_empty_str);
                    }
                    if (TextUtils.isEmpty(requestPaymentMethod.billingDetails.lastName)) {
                        return activity.getString(R.string.billing_detail_last_name_empty_str);
                    }

                    if (requestPaymentMethod.billingDetails.address != null) {
                        if (TextUtils.isEmpty(requestPaymentMethod.billingDetails.address.getCountry())) {
                            return activity.getString(R.string.billing_detail_country_empty_str);
                        }
                        if (TextUtils.isEmpty(requestPaymentMethod.billingDetails.address.getLine1())) {
                            return activity.getString(R.string.billing_detail_line1_empty_str);
                        }
                    }
                }
            }
        }
        if (requestPaymentMethod.card != null) {
            if (TextUtils.isEmpty(requestPaymentMethod.card.cardNo)) {
                return activity.getString(R.string.card_no_empty_str);
            }
            if (TextUtils.isEmpty(requestPaymentMethod.card.expYear)) {
                return activity.getString(R.string.expyear_empty_str);
            }
            if (TextUtils.isEmpty(requestPaymentMethod.card.expMonth)) {
                return activity.getString(R.string.expmonth_empty_str);
            }
            if (TextUtils.isEmpty(requestPaymentMethod.card.securityCode)) {
                return activity.getString(R.string.securitycode_empty_str);
            }
        } else {
            return activity.getString(R.string.card_information_empty_str);
        }
        return "";
    }


    /**
     * check requestCreateCustomer data
     */
    public static String checkRequestCreateCustomerData(Activity activity, RequestCreateCustomer requestCreateCustomer) {
        if (TextUtils.isEmpty(requestCreateCustomer.email)) {
            return activity.getString(R.string.customer_email_empty_str);
        } else if (TextUtils.isEmpty(requestCreateCustomer.firstName)) {
            return activity.getString(R.string.customer_first_name_empty_str);

        } else if (TextUtils.isEmpty(requestCreateCustomer.lastName)) {
            return activity.getString(R.string.customer_last_name_empty_str);
        } else if (TextUtils.isEmpty(requestCreateCustomer.phone)) {
            return activity.getString(R.string.customer_phone_empty_str);
        }
        if (requestCreateCustomer.address != null) {
            if (TextUtils.isEmpty(requestCreateCustomer.address.getCity())) {
                return activity.getString(R.string.city_empty_str);
            } else if (TextUtils.isEmpty(requestCreateCustomer.address.getCountry())) {
                return activity.getString(R.string.country_empty_str);
            } else if (TextUtils.isEmpty(requestCreateCustomer.address.getLine1())) {
                return activity.getString(R.string.line1_empty_str);
            }
        }

        if (requestCreateCustomer.getShipping() != null) {
            if (TextUtils.isEmpty(requestCreateCustomer.getShipping().firstName)) {
                return activity.getString(R.string.shipping_first_name_empty_str);
            } else if (TextUtils.isEmpty(requestCreateCustomer.getShipping().lastName)) {
                return activity.getString(R.string.shipping_last_name_empty_str);
            } else if (TextUtils.isEmpty(requestCreateCustomer.getShipping().phone)) {
                return activity.getString(R.string.shipping_phone_empty_str);
            }
        }
        return "";
    }
}
