package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.moneycollect.example.R;
import com.moneycollect.example.databinding.ActivityValidationBrowserBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;

import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionConfig;

import org.jetbrains.annotations.NotNull;

/**
 * payment process need 3 d verification, done through [ValidationWebActivity]
 */
public class ValidationWebActivity extends BaseExampleActivity {

    private ActivityValidationBrowserBinding viewBinding;

    private SonicSession sonicSession;
    SonicSessionClientImpl sonicSessionClient;

    private WebView webView;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        url = getIntent().getStringExtra(Constant.VALIDATION_PARAM_URL);
        paymentId = getIntent().getStringExtra(Constant.VALIDATION_PAYMENT_ID);
        clientSecret = getIntent().getStringExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET);
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(paymentId) || TextUtils.isEmpty(clientSecret)) {
            completeThreeDCheckout(Constant.PAYMENT_DEFAULT_MESSAGE,null);
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        initSonic();
        initUi();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        webView = viewBinding.webview;
        validationBrowserProgressbar = viewBinding.validationBrowserProgressbar;

        initWebViewClient();
        initWebChromeClient();
        initWebSettingsAndJavascriptInterface();

        // step 5: webview is ready now, just tell session client to bind
        if (sonicSessionClient != null) {
            sonicSessionClient.bindWebView(webView);
            sonicSessionClient.clientReady();
        } else {
            // default mode
            webView.loadUrl(url);
        }
    }

    private void initSonic() {
        // step 1: Initialize sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(getApplication()), new SonicConfig.Builder().build());
        }

        SonicSessionClientImpl sonicSessionClient = null;

        // step 2: Create SonicSession
        sonicSession = SonicEngine.getInstance().createSession(url, new SonicSessionConfig.Builder().build());
        //open
        if (null != sonicSession) {
            sonicSession.bindClient(sonicSessionClient = new SonicSessionClientImpl());
        }
    }

    private void initWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    validationBrowserProgressbar.setVisibility(View.GONE); //Load the page bar disappear
                } else {
                    validationBrowserProgressbar.setVisibility(View.VISIBLE); //Began to load the web display progress bar
                    validationBrowserProgressbar.setProgress(newProgress); //Value set schedule
                }
            }

//            public boolean onConsoleMessage(ConsoleMessage cm) {
//                Log.d("retrofitBack", "ConsoleMessage: "+cm.message()+"\n"
//                        +"lineNumber: " + cm.lineNumber() +"\n"
//                        +"sourceId: " + cm.sourceId());
//                return true;
//            }

        });
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettingsAndJavascriptInterface() {
        //step 4:bind javaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.removeJavascriptInterface(SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME);
        Intent intent = getIntent();
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis());
        webView.addJavascriptInterface(new SonicJavaScriptInterface(sonicSessionClient, intent), SONIC_INTERFACE_NAME);

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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebView.HitTestResult hitTestResult = view.getHitTestResult();
                if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                    view.loadUrl(url);
                    return true;
                } else {
                    if (gainStrFromHtml(url)) {
                        resolveUrl();
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (sonicSession != null) {
                    sonicSession.getSessionClient().pageFinish(url);
                }
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (sonicSession != null) {
                    //step 6: Call sessionClient.requestResource when host allow the application
                    // to return the local data .
                    return (WebResourceResponse) sonicSession.getSessionClient().requestResource(url);
                }
                return null;
            }

        });
    }

    /**
     * Results url judgment
     *
     * @param url
     * @return
     */
    boolean gainStrFromHtml(String url) {
        return url.contains(Constant.PAYMENT_ID_STR)
                && url.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                && url.contains(Constant.SOURCE_REDIRECT_SLUG_STR);
    }

    /**
     * Parsing redirection route
     *
     */
    private void resolveUrl() {
        retrievePayment();
    }

    /**
     * retrieve Payment
     */
    private void retrievePayment() {
        showLoadingDialog();
        moneyCollect.retrievePayment(paymentId, clientSecret,
                new ApiResultCallback<Payment>() {
                    @Override
                    public void onSuccess(@NotNull Payment payment) {
                        dismissLoadingDialog();
                        String resultStr;
                        String status = payment.getStatus();
                        if (status.equals(Constant.PAYMENT_SUCCEEDED)) {
                            resultStr = "";
                        } else if (status.equals(Constant.PAYMENT_FAILED)) {
                            resultStr = payment.getErrorMessage();
                        } else if (status.equals(Constant.PAYMENT_UN_CAPTURED)) {
                            resultStr = Constant.PAYMENT_UN_CAPTURED_MESSAGE;
                        } else if (status.equals(Constant.PAYMENT_PENDING)) {
                            resultStr = Constant.PAYMENT_PENDING_MESSAGE;
                        } else if (status.equals(Constant.PAYMENT_CANCELED)) {
                            resultStr = Constant.PAYMENT_CANCELED_MESSAGE;
                        } else {
                            resultStr = Constant.PAYMENT_PENDING_MESSAGE;
                        }
                        completeThreeDCheckout(resultStr,payment);
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        dismissLoadingDialog();
                        completeThreeDCheckout(e.getMessage(),null);
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
                retrievePayment();
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
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.removeAllViewsInLayout();
            webView.removeAllViews();
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
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
