package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
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
import com.moneycollect.example.BuildConfig
import com.moneycollect.example.Constant
import com.moneycollect.example.Constant.Companion.PAYMENT_LOCAL_SCHEME_URL
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData.Companion.wxH5AuthorizationUrl
import com.moneycollect.example.databinding.ActivityValidationBrowserBinding
import com.tencent.sonic.sdk.SonicConfig
import com.tencent.sonic.sdk.SonicEngine
import com.tencent.sonic.sdk.SonicSession
import com.tencent.sonic.sdk.SonicSessionConfig
import java.net.URLDecoder


/**
 * local payment process bank verification, done through [ValidationLocalWebActivity]
 */
@Suppress("DEPRECATION")
class ValidationLocalWebActivity : BaseExampleActivity(){

    private var viewBinding: ActivityValidationBrowserBinding?=null

    private var sonicSession: SonicSession? = null
    var sonicSessionClient: SonicSessionClientImpl? = null

    private var mWebView: WebView? = null
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

    private  var loadUrl: String? = null

    private  var redirectUrl: String? = null

    private var isWeiXinPay = false

    private var isLocalJumpPay = false

    var num = 0 //控制wxUrl加载的次数


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        url = intent.getStringExtra(Constant.VALIDATION_PARAM_URL)
        paymentId = intent.getStringExtra(Constant.VALIDATION_PAYMENT_ID)
        clientSecret = intent.getStringExtra(Constant.VALIDATION_PAYMENT_CLIENTSECRET)
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        initSonic()
        initUi()
    }


    override fun onResume() {
        super.onResume()
        if (isWeiXinPay && !TextUtils.isEmpty(redirectUrl)) {
            mWebView?.resumeTimers()
            mWebView?.onResume()
            isWeiXinPay = false
            redirectUrl?.let { mWebView?.loadUrl(it) }
        }

        if (isLocalJumpPay) {
            isLocalJumpPay = false
            if (!TextUtils.isEmpty(PAYMENT_LOCAL_SCHEME_URL)) {
                mWebView?.loadUrl(PAYMENT_LOCAL_SCHEME_URL)
                Handler().postDelayed({ PAYMENT_LOCAL_SCHEME_URL = "" }, 200)
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initUi() {
        viewBinding = ActivityValidationBrowserBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        mWebView = viewBinding?.webview
        validationBrowserProgressbar = viewBinding?.validationBrowserProgressbar

        initWebViewClient()
        initWebChromeClient()
        initWebSettingsAndJavascriptInterface()

        // webview is ready now, just tell session client to bind
        if (sonicSessionClient != null) {
            sonicSessionClient?.bindWebView(mWebView)
            sonicSessionClient?.clientReady()
        } else { // default mode
            url?.let { mWebView?.loadUrl(it) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettingsAndJavascriptInterface() {
        //step 4:bind javaScript
        val webSettings = mWebView?.settings
        webSettings?.javaScriptEnabled = true
        mWebView?.removeJavascriptInterface(SEARCH_BOX_JAVA_BRIDGE_INTERFACE_NAME)
        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis())
        mWebView?.addJavascriptInterface(SonicJavaScriptInterface(sonicSessionClient, intent),
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
        mWebView?.webChromeClient = object : WebChromeClient() {
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
        mWebView?.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                handler?.proceed() //Accept the certificate
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
            ): WebResourceResponse? {
                val uri = request?.url
                val url = uri.toString()
                if (!TextUtils.isEmpty(url) && url.startsWith("https://wx.tenpay.com/cgi-bin")) {
                    val backUrl = uri?.getQueryParameter("redirect_url")
                    redirectUrl = backUrl
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val hitTestResult = view?.hitTestResult
                if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                    url?.let { view?.loadUrl(it) }
                    return true
                } else {
                    url?.let {
                        if (BuildConfig.DEBUG) {
                            Log.d("OK", "shouldOverrideUrlLoading: $url")
                        }
                        //app渠道跳转钱包失败，走H5渠道
                        if (url.startsWith("asiabill://payment")) {
                            val returnUrlStr="returnurl="
                            var currentUrl=""
                            if (url.contains(returnUrlStr)){
                                currentUrl=url.substring(url.indexOf(returnUrlStr) + returnUrlStr.length,
                                    url.length)
                            }
                            if (!TextUtils.isEmpty(currentUrl)) {
                                mWebView?.loadUrl(currentUrl)
                                return true
                            }
                        }

                        when {
                            //唤起微信app
                            url.startsWith("weixin://wap/pay") -> {
                                isWeiXinPay = true
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    startActivity(intent)
                                }catch (e: Exception){
                                    showToast("The relevant application does not exist")
                                }
                                return true
                            }
                            //微信支付信息url加上Referer并重新加载获取唤起url
                            url.startsWith("https://wx.tenpay.com") -> {
                                when (Build.VERSION.RELEASE) {
                                    "4.4.3", "4.4.4" -> {
                                        //兼容这两个版本设置referer无效的问题
                                        if (num < 1) {
                                            view?.loadDataWithBaseURL(wxH5AuthorizationUrl,
                                                "<script>window.location.href=\"$url\";</script>",
                                                "text/html", "utf-8", null)
                                            num++
                                            return true
                                        }
                                    }
                                    else -> {
                                        val extraHeaders = HashMap<String, String>()
                                        extraHeaders["Referer"] = wxH5AuthorizationUrl
                                        if (num < 1) {
                                            //second reload
                                            view?.loadUrl(url, extraHeaders)
                                            num++
                                            return true
                                        }
                                    }
                                }
                            }
                            else -> {
                                try {
                                    when {
                                        url.startsWith("https://") || url.startsWith("http://") -> {
                                            // Can be either applinkUrl schemeUrl normalUrl.
                                            val uri=Uri.parse(url)
                                            val appIdentifier: String = uri.getQueryParameter("appIdentifier").toString()
                                            val urlType: String = uri.getQueryParameter("urlType").toString()

                                            when (urlType) {
                                                "applinkUrl", "schemeUrl" -> {
                                                    if (this@ValidationLocalWebActivity.loadUrl != null && !TextUtils.isEmpty(
                                                            this@ValidationLocalWebActivity.loadUrl)
                                                    ) {
                                                        view?.clearHistory()
                                                        return true
                                                    }
                                                    var jumpUrl = url
                                                    val appIdentifierUrl: String = uri.getQueryParameter("appIdentifierUrl").toString()
                                                    when {
                                                        !TextUtils.isEmpty(appIdentifierUrl) && appIdentifierUrl != "null" -> {
                                                            jumpUrl = URLDecoder.decode(appIdentifierUrl, "UTF-8")
                                                        }
                                                    }
                                                    if (openWithPackageName(jumpUrl, appIdentifier)) {

                                                    }
                                                }
                                                else -> {
                                                    //Normal jump, in line with the results link to retrieve payment status
                                                    if (gainResultStrFromHtml(url)) {
                                                        resolveUrl()
                                                        return true
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            // schemeUrl
                                            if (this@ValidationLocalWebActivity.loadUrl != null && !TextUtils.isEmpty(
                                                    this@ValidationLocalWebActivity.loadUrl)
                                            ) {
                                                view?.clearHistory()
                                                return true
                                            }
                                            if (openWithPackageName(url, "")) {
                                                return true
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (BuildConfig.DEBUG) {
                                        Log.d("OK", String.format("Exception:%s", e.toString()))
                                    }
                                }
                            }
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

    /**
     * open other app
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun openWithPackageName(url: String?, packageName: String):Boolean{
        return try {
            val packageManager: PackageManager = packageManager
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (!TextUtils.isEmpty(packageName)) {
                intent.setPackage(packageName)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val activities = packageManager.queryIntentActivities(intent, 0)
            val isValid = activities.isNotEmpty()
            return if (isValid) {
                this.loadUrl=url
                isLocalJumpPay = true
                startActivity(intent)
                true
            }else{
                false
            }
        } catch (e: Exception) {
            false
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
    fun gainResultStrFromHtml(url: String): Boolean {
        return url.contains(Constant.PAYMENT_ID_STR)
                && url.contains(Constant.PAYMENT_CLIENT_SECRET_STR)
                && url.contains(Constant.SOURCE_REDIRECT_SLUG_STR)
    }


    /**
     * 加密货币和alipay url判断
     * @param url
     * @return
     */
    fun gainStrFromH5Coin(url: String): Boolean {
        return url.contains("kakaopay:") || url.contains(
            "kakaotalk:") || url.contains("alipayconnect:")
                || url.contains("truemoney:")
                || url.contains("dana:")
                || url.contains("gcash:")
                || url.contains("tng:")
    }

    /**
     * Parsing redirection route
     *
     */
    private fun resolveUrl(){
        if (isLocalJumpPay) {
            return
        }
        if (TextUtils.isEmpty(paymentId) || TextUtils.isEmpty(clientSecret)) {
            completeThreeDCheckout(Constant.PAYMENT_DEFAULT_MESSAGE, null)
            return
        }
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
                        var wxPaymentResultStatus = false
                        val resultStr: String? = when (result.status) {
                            Constant.PAYMENT_SUCCEEDED -> {
                                wxPaymentResultStatus=true
                                ""
                            }
                            Constant.PAYMENT_FAILED -> {
                                wxPaymentResultStatus=true
                                result.errorMessage
                            }
                            Constant.PAYMENT_UN_CAPTURED -> {
                                Constant.PAYMENT_UN_CAPTURED_MESSAGE
                            }
                            Constant.PAYMENT_PENDING -> {
                                Constant.PAYMENT_PENDING_MESSAGE
                            }
                            Constant.PAYMENT_CANCELED -> {
                                wxPaymentResultStatus=true
                                Constant.PAYMENT_CANCELED_MESSAGE
                            }
                            else -> {
                                Constant.PAYMENT_PENDING_MESSAGE
                            }
                        }

                        if(isWeiXinPay){
                            if (wxPaymentResultStatus){
                                completeThreeDCheckout(resultStr, result)
                            }
                        }else {
                            completeThreeDCheckout(resultStr, result)
                        }

                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        if(!isWeiXinPay) {
                            completeThreeDCheckout(e.message, null)
                        }
                    }
                })
        }
    }

    private fun completeThreeDCheckout(result: String?, resultPayment: Payment?) {
        val intent = Intent()
        intent.putExtra(Constant.WEB_RESULT_TAG, result)
        intent.putExtra(Constant.PAYMENT_RESULT_PAYMENT, resultPayment)
        setResult(Constant.WEB_RESULT_CODE, intent)
        this@ValidationLocalWebActivity.finish()

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
        giveUpDialog(this@ValidationLocalWebActivity,
            true,
            this.getString(R.string.payment_browser_confirm_title))
    }

    override fun onDestroy() {
        if (null != sonicSession) {
            sonicSession?.destroy()
            sonicSession = null
        }
        if (popWindow != null) {
            popWindow = null
        }
        if (popWl != null) {
            popWl = null
        }
        // The webView clear the cache and destroyed
        if (mWebView != null) {
            mWebView?.stopLoading()
            mWebView?.clearHistory()
            mWebView?.removeAllViewsInLayout()
            mWebView?.removeAllViews()
            mWebView?.destroy()
            mWebView = null
        }
        if (confirmDialog != null) {
            confirmDialog?.dismiss()
            confirmDialog?.releaseCustomPop()
            confirmDialog = null
        }
        if (window != null) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
        super.onDestroy()
    }
}