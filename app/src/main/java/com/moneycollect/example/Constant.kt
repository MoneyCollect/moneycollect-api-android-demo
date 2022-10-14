package com.moneycollect.example

class Constant {
    companion object {
        //intent extra tag
        const val CURRENT_PAYMENT_BUNDLE="CURRENT_PAYMENT_BUNDLE"
        const val CURRENT_PAYMENT_MODEL="CURRENT_PAYMENT_MODEL"
        const val CREATE_PAYMENT_REQUEST_TAG="CURRENT_PAYMENT_REQUEST_TAG"
        const val CONFIRM_PAYMENT_REQUEST_TAG="CONFIRM_PAYMENT_REQUEST_TAG"
        const val CREATE_PAYMENT_METHOD_REQUEST_TAG="CREATE_PAYMENT_METHOD_REQUEST_TAG"
        const val CUSTOMER_ID_TAG="CUSTOMER_ID_TAG"
        const val SUPPORT_BANK_LIST_TAG="SUPPORT_BANK_LIST_TAG"
        const val SAVE_PAYMENT_METHOD="SAVE_PAYMENT_METHOD"
        const val ADD_PAYMENT_METHOD="ADD_PAYMENT_METHOD"
        const val WEB_RESULT_TAG="ADD_PAYMENT_METHOD"
        const val PAYMENT_RESULT_PAYMENT="PAYMENT_RESULT_PAYMENT"

        //intent request code
        const val SAVE_REQUEST_CODE =100

        //intent result code
        const val SAVE_RESULT_CODE =1
        const val ADD_RESULT_CODE =2
        const val WEB_RESULT_CODE =3
        const val PAYMENT_RESULT_CODE=999

        //Web params
        var dialogLayoutId = 0
        const val VALIDATION_PARAM_URL = "VALIDATION_PARAM_URL"
        const val VALIDATION_PAYMENT_ID = "VALIDATION_PAYMENT_ID"
        const val VALIDATION_PAYMENT_CLIENTSECRET = "VALIDATION_PAYMENT_CLIENTSECRET"
        const val PAYMENT_ID_STR = "payment_id"
        const val PAYMENT_CLIENT_SECRET_STR = "payment_client_secret"
        const val SOURCE_REDIRECT_SLUG_STR = "source_redirect_slug"

        //payment state
        const val PAYMENT_SUCCEEDED= "succeeded"
        const val PAYMENT_UN_CAPTURED = "uncaptured"
        const val PAYMENT_PENDING = "pending"
        const val PAYMENT_FAILED = "failed"
        const val PAYMENT_CANCELED = "canceled"

        //payment status message
        const val PAYMENT_SUCCESSFUL_MESSAGE = "the payment is successful"
        const val PAYMENT_UN_CAPTURED_MESSAGE = "the payment is uncaptured"
        const val PAYMENT_PENDING_MESSAGE = "the payment is pending"
        const val PAYMENT_CANCELED_MESSAGE = "the payment is canceled"
        const val PAYMENT_DEFAULT_MESSAGE = "the payment is pending"

        //local scheme url
        var PAYMENT_LOCAL_SCHEME_URL = ""
    }
}