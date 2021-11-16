package com.moneycollect.example.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moneycollect.android.model.MoneyCollectCardListViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.android.net.net.ApiResultCallback
import com.moneycollect.android.ui.imp.MoneyCollectCardCallBackInterface
import com.moneycollect.example.activity.SaveCardActivity
import com.moneycollect.android.ui.view.MoneyCollectCardListView
import com.moneycollect.example.*
import com.moneycollect.example.databinding.FragmentSaveLayoutBinding
import java.util.ArrayList

/**
 * [SaveCardFragment]
 * Show the payment list, Provide payment to the user, also can choose to add a new card, or jump [AddCardFragment]
 */
class SaveCardFragment : BaseExampleFragment(),View.OnClickListener, MoneyCollectCardCallBackInterface {

    private var viewBinding: FragmentSaveLayoutBinding?=null

    /***  [MoneyCollectCardListView]*/
    var cardListLayout: MoneyCollectCardListView?=null

    // PaymentModel (ATTACH_PAYMENT_METHOD)
    private var currentModel:MoneyCollectPaymentModel=MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD

    //customerId
    var customerId: String?=null

    //groupList params
    private var groupList: ArrayList<PaymentMethod> = ArrayList<PaymentMethod>()
    //childList params
    private var childList: ArrayList<ArrayList<PaymentMethod>> = ArrayList<ArrayList<PaymentMethod>>()

    //loading active
    private var isRequestLoading=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding =  FragmentSaveLayoutBinding.inflate(layoutInflater,container,false)
        initUi()
        return viewBinding?.root
    }

    private fun initUi() {
        currentModel= arguments?.getSerializable(Constant.CURRENT_PAYMENT_MODEL) as MoneyCollectPaymentModel
        customerId= arguments?.getString(Constant.CUSTOMER_ID_TAG)
        cardListLayout= viewBinding?.mcCardList

        val moneyCollectCardListViewParams = MoneyCollectCardListViewParams.Builder()
            .activity(activity)
            .moneyCollectPaymentModel(currentModel)
            .addMoneyCollectCardCallBackInterface(this)
            .addClickListener(this)
            .build()
        cardListLayout?.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams)

        selectAllPaymentMethods()
    }


    /**
     * select all paymentMethods
     */
    private fun selectAllPaymentMethods() {
        if (TextUtils.isEmpty(customerId)){
            cardListLayout?.visibility=View.VISIBLE
            cardListLayout?.setPaymentButtonAndFootViewStatus(true)
            return
        }
        customerId?.let {
            isRequestLoading=true
            showLoadingDialog()
            moneyCollect.selectAllPaymentMethods(it,
                object : ApiResultCallback<Any> {
                    override fun onSuccess(result: Any) {
                        dismissLoadingDialog()
                        cardListLayout?.visibility=View.VISIBLE
                        when (result) {
                            is ArrayList<*> -> {
                                when {
                                    result.isNotEmpty()-> {
                                        childList.add(result as ArrayList<PaymentMethod>)
                                        when {
                                            childList.isNotEmpty() -> {
                                                groupList.add(childList[0][0])
                                                cardListLayout?.setDataList(groupList,childList)
                                            }
                                            else -> {
                                                cardListLayout?.changeFootViewVisible(true)
                                            }
                                        }
                                    }
                                    else -> {
                                        cardListLayout?.changeFootViewVisible(true)
                                    }
                                }
                            }
                            else -> {
                                cardListLayout?.changeFootViewVisible(true)
                            }
                        }
                        isRequestLoading=false
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        cardListLayout?.visibility=View.VISIBLE
                        cardListLayout?.changeFootViewVisible(true)
                        isRequestLoading=false
                    }
                })
        }
    }


    companion object{
        const val ADD_PAYMENT:String="AddPaymentFragment"  //add fragment tag
    }

    /**
     * click event
     */
    override fun onClick(view: View?) {
        if (view!=null) {
            if (view.id == cardListLayout?.getToolbarBackIcon()?.id && cardListLayout?.isRequestLoading==false && !isRequestLoading) {
                activity?.finish()
            }else if (view.id == cardListLayout?.getChildFootView()?.id && cardListLayout?.isRequestLoading==false && !isRequestLoading) {
                activity?.let {
                    (it as? SaveCardActivity)?.switchContent(ADD_PAYMENT)
                }
            }
        }
    }

    /**
     * Users in the payment list selected paymentMethod
     */
    override fun selectPaymentMethod(paymentMethod: PaymentMethod?) {
        if (null != paymentMethod) {
            val intent = Intent()
            intent.putExtra(Constant.SAVE_PAYMENT_METHOD, paymentMethod)
            activity?.setResult(Constant.SAVE_RESULT_CODE, intent)
            activity?.finish()
        }
    }
}