# Accept a payment

Mobile Sdk发起交易示例图

![3张-1@2x](https://user-images.githubusercontent.com/92731686/141933450-8daa8efa-1648-4410-b0bf-97d6735d9da5.png)

### Set up MoneyConnectServer-sideClient-side

First, you need a MoneyConnect account. [Register now](https://portal.moneycollect.com/registerr).

**<h1>1. Set up Server-side</h1>**
客户端想要访问大部分MoneyConnectServer API，需要商户后台通过私钥发起请求，下载github上模拟商户后台服务接口代码[Download now](https://github.com/MoneyCollect/moneycollect-api-android-demo/tree/mcappserver).


> **<h3> 1.1 替换MobilePayController.java文件里面的公钥私钥成自己的<h3>**
```
//Your account PUBLIC_SECRET("Bearer "+PUBLIC_SECRET)
private static final String PUBLIC_SECRET = "Bearer live_pu_OGJ0EidwEg4GjymEiRD7cUBk7IQIYmhwhJlUM****";
//Your account PRIVATE_SECRET("Bearer "+PRVATE_SECRET)
private static final String PRIVATE_SECRET = "Bearer live_pr_OGJ0EidwEg4GjymEiRD4MRxBCo0OumdH6URv****";
```
代码中公钥和私钥格式是（"Bearer "+PUBLIC_SECRET）

> **<h3> 1.2 修改端口号（默认写死9898）<h3>**
```
server.port=9898
```
商户把代码中的公钥和私钥替换成自己的然后开启服务代码默认端口9099可修改 （商户后台接口地址为本机ip:9898）

**<h1>2. Set up Client-side</h1>**

导入MoneyCollect android sdk，然后初始化sdk
> **<h3> 2.1 Add configuration in project build.gradle<h3>**
 ```
 repositories {
    jcenter()
    maven{ url "https://raw.githubusercontent.com/MoneyCollect/moneycollect-api-android-demo/mcsdk" }
  }
```
> **<h3> 2.2 Add viewbinding and the MoneyCollect library to the app’s main module build.gradle<h3>**

 ```
 buildFeatures{
         viewBinding = true
    }
 ```
 
 ```
 dependencies {
    //The specific version number will be determined according to your needs
    implementation "com.moneycollect.payment:android_mc:0.0.1"
 }
 ```

> **<h3> 2.3 初始化sdk<h3>**

在项目application里面初始化MoneyCollect android sdk()


```
/**
* context: Context,            (上下文环境)
* publishableKey: String,      (公钥)
* customerServerUrl: String?   (标题1中的本机ip:9898)
**/
MoneyCollectSdk.init(this, "live_pu_OGJ0EidwEg4GjymEiRD7cUBk7IQIYmhwhJlUM****","http://192.168.2.100:9898/")

 ```

**<h1>3. 构建发起交易数据参数,然后开启支付activity</h1>**
商户构建好交易请求参数，点击Checkout按钮开启支付activity。（TestRequestData为数据常量类，请查看[moneycollect-api-android-demo](https://github.com/MoneyCollect/moneycollect-api-android-demo)）

```
// ...
class PaymentSheetDemoActivity: AppCompatActivity() {

    //RequestCreatePayment Object
    var testRequestPayment = TestRequestData.testRequestPayment
    //RequestConfirmPayment Object
    var testConfirmPayment = TestRequestData.testConfirmPayment
    //RequestPaymentMethod Object
    var testRequestPaymentMethod = TestRequestData.testRequestPaymentMethod
    //support payment credit card
    var testBankIvList = TestRequestData.testBankIvList
    //customerId
    var customerId = TestRequestData.customerId
    // ...
    fun presentPaymentSheet() {
        //PayCardActivity contain SaveWithPaymentCardFragment and AddWithPaymentFragment,Support them to switch to each other
        var intent = Intent(this, PayCardActivity::class.java)
        //Bundle Object
        var bundle = Bundle()
        //pass currentPaymentModel
        bundle.putSerializable(Constant.CURRENT_PAYMENT_MODEL, currentPaymentModel)
        //pass RequestCreatePayment
        bundle.putParcelable(Constant.CREATE_PAYMENT_REQUEST_TAG, testRequestPayment)
        //pass RequestConfirmPayment
        bundle.putParcelable(Constant.CONFIRM_PAYMENT_REQUEST_TAG, testConfirmPayment)
        //pass customerId
        bundle.putString(Constant.CUSTOMER_ID_TAG, TestRequestData.customerId)
        //pass default RequestPaymentMethod
        bundle?.putParcelable(Constant.CREATE_PAYMENT_METHOD_REQUEST_TAG, testRequestPaymentMethod)
        //pass default supportBankList
        bundle?.putSerializable(Constant.SUPPORT_BANK_LIST_TAG, testBankIvList)
        intent.putExtra(CURRENT_PAYMENT_BUNDLE, bundle)
        //start payment
        startActivityLauncher.launch(intent)
    }

    private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }
}
```


当客户完点击Pay Now完成付款后，将关闭支付activity返回到PaymentSheetDemoActivity，并将支付结果payment回调
```
// ...
class PaymentSheetDemoActivity: AppCompatActivity() {

     private val startActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Constant.PAYMENT_RESULT_CODE) {
                // resultPayment
                var payment =
                    it.data?.getParcelableExtra<Payment>(Constant.PAYMENT_RESULT_PAYMENT)
                if (payment != null) {
                    when (payment.status) {
                        Constant.PAYMENT_SUCCEEDED -> {
                            Log.e(TAG, Constant.PAYMENT_SUCCEEDED)
                        }
                        Constant.PAYMENT_FAILED -> {
                            payment?.errorMessage?.let { it1 ->
                                Log.e(TAG, it1)
                            }
                        }
                        Constant.PAYMENT_UN_CAPTURED -> {
                            Log.e(TAG, Constant.PAYMENT_UN_CAPTURED_MESSAGE)
                        }
                        Constant.PAYMENT_PENDING -> {
                            Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE)
                        }
                        Constant.PAYMENT_CANCELED -> {
                            Log.e(TAG, Constant.PAYMENT_CANCELED_MESSAGE)
                        }
                        else -> {
                            Log.e(TAG, Constant.PAYMENT_PENDING_MESSAGE)
                        }
                    }
                }
            }

        }
}
```


**<h1>4. Additional testing resources</h1>**
There are several test cards you can use to make sure your integration is ready for production. Use them with any CVC, postal code, and future expiration date.

|  Card Number| Brand  |DESCRIPTION          |
| :------------- | :------------- | :-------------- |
| 4242 4242 4242 4242    | Visa            | Succeeds and immediately processes the payment. |
| 3566 0020 2036 0505    | JCBA            | Succeeds and immediately processes the payment. |
| 6011 1111 1111 1117    | Discover        | Succeeds and immediately processes the payment. |
| 3782 8224 6310 0052    | American Express| Succeeds and immediately processes the payment. |
| 5555 5555 5555 4444    | Mastercard      | Succeeds and immediately processes the payment. |
| 4000 0025 0000 3155    | Visa            | 3D Secure 2 authentication . |
| 4000 0000 0000 0077    | Visa            | Always fails with a decline code of `declined`. |
