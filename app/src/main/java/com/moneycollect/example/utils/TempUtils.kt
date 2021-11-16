package com.moneycollect.example.utils

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import com.moneycollect.android.model.request.RequestCreateCustomer
import com.moneycollect.android.model.request.RequestPaymentMethod
import com.moneycollect.example.R

/**
 * The format of the string
 */
fun formatString(text: String): String? {
    val json = StringBuilder()
    var indentString = ""
    for (element in text) {
        when (element) {
            '{', '[' -> {
                json.append("""

    $indentString$element
    
    """.trimIndent())
                indentString += "\t"
                json.append(indentString)
            }
            '}', ']' -> {
                indentString = indentString.replaceFirst("\t".toRegex(), "")
                json.append("""
    
    $indentString$element
    """.trimIndent())
            }
            ',' -> json.append("""
    $element
    $indentString
    """.trimIndent())
            else -> json.append(element)
        }
    }
    return json.toString()
}

/**
 *
 * immSoftInput hide
 */
fun hideKeyboard(activity: Activity) {
    val immSoftInput: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    immSoftInput.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
}

/**
 * check requestPaymentMethod data
 */
fun checkRequestPaymentMethodData(
    activity: Activity?,
    requestPaymentMethod: RequestPaymentMethod?,
): String? {
    if (activity == null || requestPaymentMethod == null) {
        return ""
    }
    when {
        !TextUtils.isEmpty(requestPaymentMethod.type) -> {
            when (requestPaymentMethod.type) {
                "card" -> {
                    when (requestPaymentMethod.billingDetails) {
                        null -> {
                            return activity.getString(R.string.billing_detail_empty_str)
                        }
                        else -> {
                            when {
                                TextUtils.isEmpty(requestPaymentMethod.billingDetails?.firstName) -> {
                                    return activity.getString(R.string.billing_detail_first_name_empty_str)
                                }
                                TextUtils.isEmpty(requestPaymentMethod.billingDetails?.lastName) -> {
                                    return activity.getString(R.string.billing_detail_last_name_empty_str)
                                }
                                requestPaymentMethod.billingDetails?.address != null -> {
                                    if (TextUtils.isEmpty(requestPaymentMethod.billingDetails?.address?.country)) {
                                        return activity.getString(R.string.billing_detail_country_empty_str)
                                    }
                                    if (TextUtils.isEmpty(requestPaymentMethod.billingDetails?.address?.line1)) {
                                        return activity.getString(R.string.billing_detail_line1_empty_str)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            return activity.getString(R.string.payment_method_type_empty_str)
        }
    }
    when {
        requestPaymentMethod.card != null -> {
            when {
                TextUtils.isEmpty(requestPaymentMethod.card?.cardNo) -> {
                    return activity.getString(R.string.card_no_empty_str)
                }
                TextUtils.isEmpty(requestPaymentMethod.card?.expYear) -> {
                    return activity.getString(R.string.expyear_empty_str)
                }
                TextUtils.isEmpty(requestPaymentMethod.card?.expMonth) -> {
                    return activity.getString(R.string.expmonth_empty_str)
                }
                TextUtils.isEmpty(requestPaymentMethod.card?.securityCode) -> {
                    return activity.getString(R.string.securitycode_empty_str)
                }
            }
        }
        else -> {
            return activity.getString(R.string.card_information_empty_str)
        }
    }
    return ""
}


/**
 * check requestCreateCustomer data
 */
fun checkRequestCreateCustomerData(
    activity: Activity,
    requestCreateCustomer: RequestCreateCustomer,
): String? {
    when {
        TextUtils.isEmpty(requestCreateCustomer.email) -> {
            return activity.getString(R.string.customer_email_empty_str)
        }
        TextUtils.isEmpty(requestCreateCustomer.firstName) -> {
            return activity.getString(R.string.customer_first_name_empty_str)
        }
        TextUtils.isEmpty(requestCreateCustomer.lastName) -> {
            return activity.getString(R.string.customer_last_name_empty_str)
        }
        TextUtils.isEmpty(requestCreateCustomer.phone) -> {
            return activity.getString(R.string.customer_phone_empty_str)
        }
        requestCreateCustomer.address != null -> {
            when {
                TextUtils.isEmpty(requestCreateCustomer.address?.city) -> {
                    return activity.getString(R.string.city_empty_str)
                }
                TextUtils.isEmpty(requestCreateCustomer.address?.country) -> {
                    return activity.getString(R.string.country_empty_str)
                }
                TextUtils.isEmpty(requestCreateCustomer.address?.line1) -> {
                    return activity.getString(R.string.line1_empty_str)
                }
            }
        }
    }
    if (requestCreateCustomer.shipping != null) {
        when {
            TextUtils.isEmpty(requestCreateCustomer.shipping?.firstName) -> {
                return activity.getString(R.string.shipping_first_name_empty_str)
            }
            TextUtils.isEmpty(requestCreateCustomer.shipping?.lastName) -> {
                return activity.getString(R.string.shipping_last_name_empty_str)
            }
            TextUtils.isEmpty(requestCreateCustomer.shipping?.phone) -> {
                return activity.getString(R.string.shipping_phone_empty_str)
            }
        }
    }
    return ""
}