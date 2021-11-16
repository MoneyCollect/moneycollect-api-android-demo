package com.moneycollect.example.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import com.moneycollect.android.model.MoneyCollectButtonViewParams
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.android.ui.view.MoneyCollectButtonView
import com.moneycollect.android.utils.MoneyCollectButtonUtils
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.R
import com.moneycollect.example.databinding.ActivitySelectButtonTypeLayoutBinding

/**
 * [SelectButtonTypeActivity] show the use of  MoneyCollectButtonView
 */
class SelectButtonTypeActivity : BaseExampleActivity(),View.OnClickListener{

    private var viewBinding: ActivitySelectButtonTypeLayoutBinding?=null

    /***  [MoneyCollectButtonView]*/
    private var buttonWidget: MoneyCollectButtonView?=null

    // PaymentModel (PAY)
    var moneyCollectPaymentModel: MoneyCollectPaymentModel =MoneyCollectPaymentModel.PAY

    private var startHoldingAnimButton: Button?=null
    private var startCompleteAnimButton: Button?=null
    private var stopAnimButton: Button?=null
    private var settingEnableButton: Button?=null
    private var settingUnEnableButton: Button?=null

    //loading active
    private var isLoadingAnimStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySelectButtonTypeLayoutBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        initUi()
    }

    private fun initUi() {
        startHoldingAnimButton=viewBinding?.startHoldingAnim
        startCompleteAnimButton=viewBinding?.startCompleteAnim
        stopAnimButton=viewBinding?.stopAnim
        settingEnableButton=viewBinding?.settingButtonEnable
        settingUnEnableButton=viewBinding?.settingButtonUnenable

        startHoldingAnimButton?.setOnClickListener(this)
        startCompleteAnimButton?.setOnClickListener(this)
        stopAnimButton?.setOnClickListener(this)
        settingEnableButton?.setOnClickListener(this)
        settingUnEnableButton?.setOnClickListener(this)

        buttonWidget=viewBinding?.buttonWidget


        val params= MoneyCollectButtonViewParams.Builder()
            .activity(this)
            .moneyCollectPaymentModel(moneyCollectPaymentModel)
            .confirmButtonStatus(true)
            .confirmButtonText(resources.getString(R.string.payment_select_button_example))
            .confirmButtonTextSize(resources.getDimension(R.dimen.text_size_5sp))
            .confirmButtonAlpha(1.0f)
            .completeAnimTimeOut(3000L)
            .build()
        buttonWidget?.setMoneyCollectButtonViewParams(params)
        buttonWidget?.cardConfirmButton?.setOnClickListener(this)  //set button onClick
    }

    /**
     * click event
     */
    override fun onClick(view:View?) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.isFastDoubleClick(view.id, 800)) {
                when (view.id) {
                    buttonWidget?.cardConfirmButton?.id -> {
                        isLoadingAnimStatus=true
                        buttonWidget?.setMoneyCollectButtonViewContext(this@SelectButtonTypeActivity)
                        buttonWidget?.setMoneyCollectButtonViewModel( moneyCollectPaymentModel)
                        buttonWidgetAnimStart()
                    }
                    R.id.start_holding_anim -> {
                        isLoadingAnimStatus=true
                        buttonWidget?.setMoneyCollectButtonViewContext(null)
                        buttonWidget?.setMoneyCollectButtonViewModel( moneyCollectPaymentModel)
                        buttonWidget?.showAnimByPaymentHolding()
                    }
                    R.id.start_complete_anim -> {
                        isLoadingAnimStatus=true
                        buttonWidget?.setMoneyCollectButtonViewContext(null)
                        buttonWidget?.setMoneyCollectButtonViewModel( moneyCollectPaymentModel)
                        buttonWidget?.showAnimByPaymentComplete()
                    }
                    R.id.stop_anim -> {
                        isLoadingAnimStatus=false
                        buttonWidget?.stopPaymentAnim()
                    }
                    R.id.setting_button_enable -> {
                        if (!isLoadingAnimStatus){
                            buttonWidget?.setCardConfirmButtonStatus(true)
                        }else{
                            showToast(getString(R.string.stop_anim_message_str))
                        }
                    }
                    R.id.setting_button_unenable -> {
                        if (!isLoadingAnimStatus) {
                            buttonWidget?.setCardConfirmButtonStatus(false)
                        }else{
                            showToast(getString(R.string.stop_anim_message_str))
                        }
                    }
                }
            }
        }
    }

    /**
     * Combination of holding and complete two animation use
     */
    private fun buttonWidgetAnimStart(){
        buttonWidget?.cardConfirmButton?.startAnimation(
            AnimationUtils.loadAnimation(
                this@SelectButtonTypeActivity,
                R.anim.mc_button_scale_smaller
            ).also { animation ->
                animation.setAnimationListener(
                    object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {

                        }

                        override fun onAnimationEnd(p0: Animation?) {
                            buttonWidget?.showAnimByPaymentHolding()
                            object : CountDownTimer(3000, 10) {
                                override fun onTick(millisUntilFinished: Long) {
                                }

                                override fun onFinish() {
                                    if (isLoadingAnimStatus) {
                                        buttonWidget?.showAnimByPaymentComplete()
                                    }
                                }
                            }.start()
                        }

                        override fun onAnimationRepeat(p0: Animation?) {
                        }
                    }
                )
            }
        )
    }
}