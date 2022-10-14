package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.moneycollect.android.model.response.Payment;
import com.moneycollect.android.model.webview.SonicJavaScriptInterface;
import com.moneycollect.android.model.webview.SonicRuntimeImpl;
import com.moneycollect.android.model.webview.SonicSessionClientImpl;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.view.MoneyCollectCustomPopupWindow;
import com.moneycollect.example_java.BuildConfig;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivityValidationBrowserBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.TestRequestData;
import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionConfig;

import org.jetbrains.annotations.NotNull;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

/**
 * local payment process bank verification, done through {@link ValidationLocalWebActivity}
 */
public class ValidationLocalWebActivity extends BaseExampleActivity {

    private ActivityValidationBrowserBinding viewBinding;

    private SonicSession sonicSession;
    SonicSessionClientImpl sonicSessionClient;

    private WebView mWebView;
    private ProgressBar validationBrowserProgressbar;

    /***  [MoneyCollectCustomPopupWindow]*/
    private MoneyCollectCustomPopupWindow confirmDialog;

    private String url;

    //paymentId for pay
    private String paymentId;

    //paymentId for pay
    private String clientSecret;

    private static String SONIC_INTERFACE_NAME = "sonic";
    private static String SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME = "searchBoxJavaBridge_";

    private MoneyCollectCustomPopupWindow comfirmDialog;
    private Window popWindow;
    private WindowManager.LayoutParams popWl;

    private  String loadUrl = null;

    private  String redirectUrl = null;

    private boolean isWeiXinPay = false;


    private boolean isLocalJumpPay = false;

    int num = 0; //控制wxUrl加载的次数

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        if (intent!=null) {
            url = intent.getStringExtra(Constant.VALIDATION_PARAM_URL);
            paymentId = intent.getStringExtra(Constant.VALIDATION_PAYMENT_ID);
            clientSecret = intent.getStringExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        initSonic();
        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isWeiXinPay && !TextUtils.isEmpty(redirectUrl)) {
            mWebView.resumeTimers();
            mWebView.onResume();
            isWeiXinPay = false;
            if (!TextUtils.isEmpty(redirectUrl)){
                mWebView.loadUrl(redirectUrl);
            }
        }

        if (isLocalJumpPay){
            isLocalJumpPay = false;
            if (!TextUtils.isEmpty(Constant.PAYMENT_LOCAL_SCHEME_URL)) {
                mWebView.loadUrl(Constant.PAYMENT_LOCAL_SCHEME_URL);
                new Handler().postDelayed(() -> Constant.PAYMENT_LOCAL_SCHEME_URL = "", 200);
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        mWebView = viewBinding.webview;
        validationBrowserProgressbar = viewBinding.validationBrowserProgressbar;

        initWebViewClient();
        initWebChromeClient();
        initWebSettingsAndJavascriptInterface();

        // webview is ready now, just tell session client to bind
        if (sonicSessionClient != null) {
            sonicSessionClient.bindWebView(mWebView);
            sonicSessionClient.clientReady();
        } else {
            // default mode
            mWebView.loadUrl(url);
        }

    }

    private void initSonic() {
        // step 1: Initialize sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(getApplication()), new SonicConfig.Builder().build());
        }

        // step 2: Create SonicSession
        if (!TextUtils.isEmpty(url)) {
            sonicSession = SonicEngine.getInstance().createSession(url, new SonicSessionConfig.Builder().build());
        }
        //open
        if (null != sonicSession) {
            sonicSessionClient = new SonicSessionClientImpl();
            sonicSession.bindClient(sonicSessionClient);
        }
    }

    private void initWebChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    validationBrowserProgressbar.setVisibility(View.GONE); //Load the page bar disappear
                } else {
                    validationBrowserProgressbar.setVisibility(View.VISIBLE); //Began to load the web display progress bar
                    validationBrowserProgressbar.setProgress(newProgress); //Value set schedule
                }
            }
        });
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettingsAndJavascriptInterface() {
        //step 4:bind javaScript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.removeJavascriptInterface(SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME);
        Intent intent = getIntent();
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis());
        mWebView.addJavascriptInterface(new SonicJavaScriptInterface(sonicSessionClient, intent), SONIC_INTERFACE_NAME);

        // init webview settings
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
    }

    private void initWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();
                if (!TextUtils.isEmpty(url) && url.startsWith("https://wx.tenpay.com/cgi-bin")) {
                    String backUrl = uri.getQueryParameter("redirect_url");
                    redirectUrl = backUrl;
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebView.HitTestResult hitTestResult = view.getHitTestResult();
                if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                    view.loadUrl(url);
                    return true;
                } else {
                    if (!TextUtils.isEmpty(url)) {
                        if (BuildConfig.DEBUG) {
                            Log.d("OK", "shouldOverrideUrlLoading: "+url);
                        }

                        //app渠道跳转钱包失败，走H5渠道
                        if (url.startsWith("asiabill://payment")) {
                            String returnUrlStr="returnurl=";
                            String currentUrl="";
                            if (url.contains(returnUrlStr)){
                                currentUrl=url.substring(url.indexOf(returnUrlStr) + returnUrlStr.length());
                            }
                            if (!TextUtils.isEmpty(currentUrl)) {
                                mWebView.loadUrl(currentUrl);
                                return true;
                            }
                        }
                        //唤起微信app
                        if (url.startsWith("weixin://wap/pay")) {
                            isWeiXinPay = true;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                startActivity(intent);
                                return true;
                            }catch ( Exception e){
                                showToast("The relevant application does not exist");
                            }
                        } else if(url.startsWith("https://wx.tenpay.com")) {
                            if (("4.4.3".equals(Build.VERSION.RELEASE)) ||
                                    ("4.4.4".equals(Build.VERSION.RELEASE))) {
                                //兼容这两个版本设置referer无效的问题
                                if (num < 1) {
                                    view.loadDataWithBaseURL(TestRequestData.Companion.getWxH5AuthorizationUrl(),
                                            "<script>window.location.href=\"$url\";</script>",
                                            "text/html", "utf-8", null);
                                    num++;
                                    return true;
                                }
                            } else {
                                HashMap<String, String> extraHeaders = new HashMap<>();
                                extraHeaders.put("Referer",TestRequestData.Companion.getWxH5AuthorizationUrl());
                                if (num < 1) {
                                    //second reload
                                    view.loadUrl(url, extraHeaders);
                                    num++;
                                    return true;
                                }
                            }
                        }else {
                            try {
                                if (url.startsWith("https://") || url.startsWith("http://")) {
                                    // Can be either applinkUrl or normalUrl.
                                    Uri uri=Uri.parse(url);
                                    String appIdentifier = String.format("%s", uri.getQueryParameter("appIdentifier"));
                                    String urlType = String.format("%s", uri.getQueryParameter("urlType"));
                                    if (urlType.equals("applinkUrl") || urlType.equals("schemeUrl")){
                                        if (loadUrl != null && !TextUtils.isEmpty(loadUrl)) {
                                            view.clearHistory();
                                            return true;
                                        }
                                        String jumpUrl = url;
                                        String appIdentifierUrl = String.format("%s", uri.getQueryParameter("appIdentifierUrl"));
                                        if (!TextUtils.isEmpty(appIdentifierUrl) && !appIdentifierUrl.equals("null")) {
                                            jumpUrl = URLDecoder.decode(appIdentifierUrl, "UTF-8");
                                        }
                                        if (openWithPackageName(jumpUrl, appIdentifier)) {

                                        }
                                    }else{
                                        if (gainResultStrFromHtml(url)) {
                                            resolveUrl();
                                            return true;
                                        }
                                    }
                                } else {
                                    // schemeUrl
                                    if (loadUrl != null && !TextUtils.isEmpty(loadUrl)) {
                                        view.clearHistory();
                                        return true;
                                    }
                                    if (openWithPackageName(url, "")) {
                                        return true;
                                    }
                                }
                            } catch (Exception e) {
                                if (BuildConfig.DEBUG) {
                                    Log.d("OK", String.format("Exception:%s", e.toString()));
                                }
                            }
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (sonicSession != null) {
                    sonicSession.getSessionClient().pageFinish(url);
                }
            }

        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private boolean openWithPackageName(String url, String packageName) {
         try {
             PackageManager packageManager = getPackageManager();
             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
             if (!TextUtils.isEmpty(packageName)) {
                 intent.setPackage(packageName);
             }
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
             boolean isValid = !activities.isEmpty();
             if (isValid) {
                 this.loadUrl=url;
                 isLocalJumpPay = true;
                 startActivity(intent);
                 return true;
             }else{
                 return false;
             }
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Results url judgment
     *
     * @param url
     * @return
     */
    boolean gainResultStrFromHtml(String url) {
        return url.contains(Constant.PAYMENT_ID_STR)
                && url.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                && url.contains(Constant.SOURCE_REDIRECT_SLUG_STR);
    }

    /**
     * Parsing redirection route
     *
     */
    private void resolveUrl() {
        if (isLocalJumpPay){
            return;
        }
        if (TextUtils.isEmpty(paymentId) || TextUtils.isEmpty(clientSecret)) {
            completeThreeDCheckout(Constant.PAYMENT_DEFAULT_MESSAGE, null);
            return;
        }
        retrievePayment(paymentId,clientSecret);
    }

    /**
     * retrieve Payment
     * @param paymentId
     * @param clientSecret
     */
    private void retrievePayment(String paymentId, String clientSecret) {
        showLoadingDialog();
        moneyCollect.retrievePayment(paymentId, clientSecret,
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment payment) {
                        dismissLoadingDialog();
                        boolean wxPaymentResultStatus = false;
                        String resultStr = Constant.PAYMENT_PENDING_MESSAGE;
                        if (payment!=null) {
                            String status = payment.getStatus();
                            if (status.equals(Constant.PAYMENT_SUCCEEDED)) {
                                wxPaymentResultStatus=true;
                                resultStr = "";
                            } else if (status.equals(Constant.PAYMENT_FAILED)) {
                                wxPaymentResultStatus=true;
                                resultStr = payment.getErrorMessage();
                            } else if (status.equals(Constant.PAYMENT_UN_CAPTURED)) {
                                resultStr = Constant.PAYMENT_UN_CAPTURED_MESSAGE;
                            } else if (status.equals(Constant.PAYMENT_PENDING)) {
                                resultStr = Constant.PAYMENT_PENDING_MESSAGE;
                            } else if (status.equals(Constant.PAYMENT_CANCELED)) {
                                wxPaymentResultStatus=true;
                                resultStr = Constant.PAYMENT_CANCELED_MESSAGE;
                            }
                        }
                        if(isWeiXinPay){
                            if (wxPaymentResultStatus==true){
                                completeThreeDCheckout(resultStr, payment);
                            }
                        }else {
                            completeThreeDCheckout(resultStr, payment);
                        }
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        if(!isWeiXinPay) {
                            completeThreeDCheckout(e.getMessage(),null);
                        }
                    }
                });
    }

    private void completeThreeDCheckout(String result,Payment resultPayment){
        Intent intent = new Intent();
        intent.putExtra(Constant.WEB_RESULT_TAG, result);
        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, resultPayment);
        setResult(Constant.WEB_RESULT_CODE,intent);
        finish();
    }


    /**
     * click on the return key trigger
     */
    @Override
    public void onBackPressed() {
        giveUpDialog(this, true, this.getString(R.string.payment_browser_confirm_title));
    }

    /**
     * When user triggers the return key to show this dialog
     */
    private void giveUpDialog(Context context, boolean isBackPress, String title) {
        if (context == null) {
            return;
        }
        if (comfirmDialog == null) {
            comfirmDialog = new MoneyCollectCustomPopupWindow.Builder(context)
                    .setContentView(R.layout.dialog_browser_confirm)
                    .setLayoutWidth(LinearLayout.LayoutParams.MATCH_PARENT)
                    .setLayoutHeight(LinearLayout.LayoutParams.WRAP_CONTENT)
                    .build();
        }
        setBackAlpha(0.6f);
        int local[] = new int[2];
        viewBinding.getRoot().getLocationOnScreen(local);
        comfirmDialog.showAtLocation(viewBinding.getRoot(), Gravity.CENTER, local[0], local[1] - 100);

        TextView noTv = (TextView) comfirmDialog.getItemView(R.id.payment_confirm_no_tv);
        TextView titleTv = (TextView) comfirmDialog.getItemView(R.id.payment_confirm_title_tv);
        View lineView = comfirmDialog.getItemView(R.id.payment_confirm_view2);
        titleTv.setText(title);
        if (isBackPress) {
            noTv.setVisibility(View.VISIBLE);
            lineView.setVisibility(View.VISIBLE);
        } else {
            noTv.setVisibility(View.GONE);
            lineView.setVisibility(View.GONE);
        }

        comfirmDialog.setOnClickListener(R.id.payment_browser_yes_tv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackAlpha(1.0f);
                comfirmDialog.dismiss();
                retrievePayment(paymentId, clientSecret);
            }
        });
        comfirmDialog.setOnClickListener(R.id.payment_confirm_no_tv, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackAlpha(1.0f);
                comfirmDialog.dismiss();
            }
        });
        if (comfirmDialog.getOnDismissListener() != null) {
            comfirmDialog.getOnDismissListener().setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setBackAlpha(1.0f);
                }
            });
        } else {
            setBackAlpha(1.0f);
        }
    }

    public void setBackAlpha(float mAlpha) {
        if (popWindow == null) {
            popWindow = getWindow();
        }
        if (popWl == null) {
            popWl = popWindow.getAttributes();
        }
        popWl.alpha = mAlpha;
        popWindow.setAttributes(popWl);
    }

    @Override
    protected void onDestroy() {
        if (null != sonicSession) {
            sonicSession.destroy();
            sonicSession = null;
        }
        if (popWindow != null) {
            popWindow = null;
        }
        if (popWl != null) {
            popWl = null;
        }
        // The webView clear the cache and destroyed
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.clearHistory();
            mWebView.removeAllViewsInLayout();
            mWebView.removeAllViews();
            mWebView.setWebViewClient(null);
            mWebView.destroy();
            mWebView = null;
        }

        if (confirmDialog != null) {
            confirmDialog.dismiss();
            confirmDialog.releaseCustomPop();
            confirmDialog = null;
        }
        if (getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        super.onDestroy();
    }
}
