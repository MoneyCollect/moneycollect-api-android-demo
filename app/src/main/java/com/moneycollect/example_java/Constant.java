package com.moneycollect.example_java;

public class Constant {
    //intent extra tag
    public final static String CURRENT_PAYMENT_BUNDLE = "CURRENT_PAYMENT_BUNDLE";
    public final static String CURRENT_PAYMENT_MODEL = "CURRENT_PAYMENT_MODEL";
    public final static String CREATE_PAYMENT_REQUEST_TAG = "CURRENT_PAYMENT_REQUEST_TAG";
    public final static String CONFIRM_PAYMENT_REQUEST_TAG = "CONFIRM_PAYMENT_REQUEST_TAG";
    public final static String CREATE_PAYMENT_METHOD_REQUEST_TAG = "CREATE_PAYMENT_METHOD_REQUEST_TAG";
    public final static String CUSTOMER_ID_TAG = "CUSTOMER_ID_TAG";
    public final static String SUPPORT_BANK_LIST_TAG = "SUPPORT_BANK_LIST_TAG";
    public final static String SAVE_PAYMENT_METHOD = "SAVE_PAYMENT_METHOD";
    public final static String ADD_PAYMENT_METHOD = "ADD_PAYMENT_METHOD";
    public final static String WEB_RESULT_TAG = "ADD_PAYMENT_METHOD";
    public final static String PAYMENT_RESULT_PAYMENT = "PAYMENT_RESULT_PAYMENT";

    //intent request code
    public final static int SAVE_REQUEST_CODE = 100;

    //intent result code
    public final static int SAVE_RESULT_CODE = 1;
    public final static int ADD_RESULT_CODE = 2;
    public final static int WEB_RESULT_CODE = 3;
    public final static int PAYMENT_RESULT_CODE = 999;
    //Web params
    public static int dialogLayoutId = 0;
    public final static String VALIDATION_PARAM_URL = "VALIDATION_PARAM_URL";
    public final static String VALIDATION_PAYMENT_ID = "VALIDATION_PAYMENT_ID";
    public final static String VALIDATION_PAYMENT_CLIENTSECRET = "VALIDATION_PAYMENT_CLIENTSECRET";
    public static String PAYMENT_ID_STR = "payment_id";
    public static String PAYMENT_CLIENT_SECRET_STR = "payment_client_secret";
    public static String SOURCE_REDIRECT_SLUG_STR = "source_redirect_slug";

    //payment state
    public final static String PAYMENT_SUCCEEDED = "succeeded";
    public final static String PAYMENT_UN_CAPTURED = "uncaptured";
    public final static String PAYMENT_PENDING = "pending";
    public final static String PAYMENT_FAILED = "failed";
    public final static String PAYMENT_CANCELED = "canceled";

    //payment status message
    public final static String PAYMENT_SUCCESSFUL_MESSAGE = "the payment is successful";
    public final static String PAYMENT_UN_CAPTURED_MESSAGE = "the payment is uncaptured";
    public final static String PAYMENT_PENDING_MESSAGE = "the payment is pending";
    public final static String PAYMENT_CANCELED_MESSAGE = "the payment is canceled";
    public final static String PAYMENT_DEFAULT_MESSAGE = "the payment is pending";


}
