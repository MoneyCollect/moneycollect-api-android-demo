package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.moneycollect.android.model.response.Payment
import com.moneycollect.android.model.webview.SonicJavaScriptInterface
import com.moneycollect.android.model.webview.SonicRuntimeImpl
import com.moneycollect.android.model.webview.SonicSessionClientImpl
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.view.MoneyCollectCustomPopupWindow
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.databinding.ActivityValidationBrowserBinding

import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSession
import com.tencent.sonic.sdk.SonicSessionConfig

/**
 * payment process need 3 d verification, done through [ValidationWebActivity]
 */
@Suppress("DEPRECATION")
class ValidationWebActivity : BaseExampleActivity(){

    private var viewBinding: ActivityValidationBrowserBinding?=null

    private var sonicSession: SonicSession? = null
    var sonicSessionClient: SonicSessionClientImpl? = null

    private var webView: WebView? = null
    private var validationBrowserProgressbar: ProgressBar? = null

    /***  [MoneyCollectCustomPopupWindow]*/
    private var confirmDialog: MoneyCollectCustomPopupWindow? = null

    private var popWindow: Window? = null
    private var popWl: WindowManager.LayoutParams? = null

    private var url:String?=null

    //paymentId for pay
    private var paymentId:String?=null

    //paymentId for pay
    private var clientSecret:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        url = intent.getStringExtra(Constant.VALIDATION_PARAM_URL)
        paymentId = intent.getStringExtra(Constant.VALIDATION_PAYMENT_ID)
        clientSecret = intent.getStringExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET)
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(paymentId) || TextUtils.isEmpty(clientSecret)) {
            completeThreeDCheckout(Constant.PAYMENT_DEFAULT_MESSAGE,null)
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        initSonic()
        initUi()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        webView = viewBinding?.webview
        validationBrowserProgressbar = viewBinding?.validationBrowserProgressbar

        initWebViewClient()
        initWebChromeClient()
        initWebSettingsAndJavascriptInterface()

        // step 5: web is ready now, just tell session client to bind
        if (sonicSessionClient != null) {
            sonicSessionClient?.bindWebView(webView)
            sonicSessionClient?.clientReady()
        } else { // default mode
            url?.let { webView?.loadUrl(it) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettingsAndJavascriptInterface() {
        //step 4:bind javaScript
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webView?.removeJavascriptInterface(SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME)
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis())
        webView?.addJavascriptInterface(SonicJavaScriptInterface(sonicSessionClient, intent),
            SONIC_INTERFACE_NAME)

        // init web settings
        webSettings?.allowContentAccess = true
        webSettings?.databaseEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.setAppCacheEnabled(true)
        webSettings?.savePassword = false
        webSettings?.saveFormData = false
        webSettings?.useWideViewPort = true
        webSettings?.loadWithOverviewMode = true
    }

    private fun initWebChromeClient() {
        webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    validationBrowserProgressbar?.visibility = View.GONE //Load the page bar disappear
                } else {
                    validationBrowserProgressbar?.visibility = View.VISIBLE //Began to load the web display progress bar
                    validationBrowserProgressbar?.progress = newProgress //Value set schedule
                }
            }
        }
    }

    private fun initWebViewClient() {
        webView?.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.proceed() //Accept the certificate
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val hitTestResult = view?.hitTestResult
                if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                    url?.let { view?.loadUrl(it) }
                    return true
                } else {
                    url?.let {
                        if (gainStrFromHtml(url)) {
                            resolveUrl()
                            return true
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (sonicSession != null) {
                    sonicSession?.sessionClient?.pageFinish(url)
                }
            }
        }
    }

    private fun initSonic() {
        // step 1: Initialize sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(SonicRuntimeImpl(application), SonicConfig.Builder().build())
        }
        // step 2: Create SonicSession
        sonicSession = url?.let {
            SonicEngine.getInstance().createSession(it, SonicSessionConfig.Builder().build())
        }
        //open
        if (null != sonicSession) {
            sonicSession?.bindClient(SonicSessionClientImpl().also { sonicSessionClient = it })
        }
    }

    /**
     * Results url judgment
     * @param url
     * @return
     */
    fun gainStrFromHtml(url: String): Boolean {
        return url.contains(Constant.PAYMENT_ID_STR)
                && url.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                && url.contains(Constant.SOURCE_REDIRECT_SLUG_STR)
    }

    /**
     * Parsing redirection route
     *
     */
    private fun resolveUrl(){
        retrievePayment()
    }

    /**
     * retrieve Payment
     */
    private fun retrievePayment() {
        showLoadingDialog()
        paymentId?.let {
            moneyCollect.retrievePayment(it, clientSecret,
                object : ApiResultCallback<Payment> {
                    override fun onSuccess(result: Payment) {
                        //	Status: succeeded,uncaptured,pending,failed,canceled
                        dismissLoadingDialog()
                        val resultStr: String? = when (result.status) {
                            Constant.PAYMENT_SUCCEEDED -> {
                                ""
                            }
                            Constant.PAYMENT_FAILED -> {
                                result.errorMessage
                            }
                            Constant.PAYMENT_UN_CAPTURED -> {
                                Constant.PAYMENT_UN_CAPTURED_MESSAGE
                            }
                            Constant.PAYMENT_PENDING -> {
                                Constant.PAYMENT_PENDING_MESSAGE
                            }
                            Constant.PAYMENT_CANCELED -> {
                                Constant.PAYMENT_CANCELED_MESSAGE
                            }
                            else -> {
                                Constant.PAYMENT_PENDING_MESSAGE
                            }
                        }
                        completeThreeDCheckout(resultStr,result)
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        completeThreeDCheckout(e.message,null)
                    }
                })
        }
    }

    private fun completeThreeDCheckout(result: String?, resultPayment: Payment?) {
        val intent = Intent()
        intent.putExtra(Constant.WEB_RESULT_TAG, result)
        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, resultPayment)
        setResult(Constant.WEB_RESULT_CODE,intent)
        finish()
    }

    /**
     * When user triggers the return key to show this dialog
     */
    private fun giveUpDialog(context: Context?, isBackPress: Boolean, title: String?) {
        if (context == null) {
            return
        }
        if (confirmDialog == null) {
            confirmDialog = MoneyCollectCustomPopupWindow.Builder(context)
                .setContentView(R.layout.dialog_browser_confirm)
                .setLayoutWidth(LinearLayout.LayoutParams.MATCH_PARENT)
                .setLayoutHeight(LinearLayout.LayoutParams.WRAP_CONTENT)
                .build()
        }
        setBackAlpha(0.6f)
        val local = IntArray(2)
        viewBinding?.root?.getLocationOnScreen(local)
        confirmDialog?.showAtLocation(viewBinding?.root,
            Gravity.CENTER,
            local[0],
            local[1] - 100)
        val noTv = confirmDialog?.getItemView(R.id.payment_confirm_no_tv) as TextView
        val titleTv = confirmDialog?.getItemView(R.id.payment_confirm_title_tv) as TextView
        val lineView: View = confirmDialog?.getItemView(R.id.payment_confirm_view2) as View
        titleTv.text = title
        if (isBackPress) {
            noTv.visibility = View.VISIBLE
            lineView.visibility = View.VISIBLE
        } else {
            noTv.visibility = View.GONE
            lineView.visibility = View.GONE
        }
        confirmDialog?.setOnClickListener(R.id.payment_browser_yes_tv, View.OnClickListener {
            setBackAlpha(1.0f)
            retrievePayment()
        })
        confirmDialog?.setOnClickListener(R.id.payment_confirm_no_tv, View.OnClickListener {
            setBackAlpha(1.0f)
            confirmDialog?.dismiss()
        })
        if (confirmDialog?.onDismissListener!= null) {
            confirmDialog?.onDismissListener?.setOnDismissListener { setBackAlpha(1.0f) }
        } else {
            setBackAlpha(1.0f)
        }
    }

    private fun setBackAlpha(mAlpha: Float) {
        if (popWindow == null) {
            popWindow = window
        }
        if (popWl == null) {
            popWl = popWindow?.attributes
        }
        popWl?.alpha = mAlpha
        popWindow?.attributes = popWl
    }

    companion object{
        const val SONIC_INTERFACE_NAME = "sonic"
        const val SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME = "searchBoxJavaBridge_"
    }

    /**
     * click on the return key trigger
     */
    override fun onBackPressed() {
        giveUpDialog(this@ValidationWebActivity,
            true,
            this.getString(R.string.payment_browser_confirm_title))
    }

    override fun onDestroy() {
        if (null != sonicSession) {
            sonicSession!!.destroy()
            sonicSession = null
        }
        if (popWindow != null) {
            popWindow = null
        }
        if (popWl != null) {
            popWl = null
        }
        // The webView clear the cache and destroyed
        if (webView != null) {
            webView?.stopLoading()
            webView?.clearHistory()
            webView?.removeAllViewsInLayout()
            webView?.removeAllViews()
            webView?.destroy()
            webView = null
        }
        if (confirmDialog != null) {
            confirmDialog?.dismiss()
            confirmDialog?.releaseCustomPop()
            confirmDialog = null
        }
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
        super.onDestroy()
    }
}