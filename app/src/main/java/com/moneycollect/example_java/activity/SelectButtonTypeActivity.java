package com.moneycollect.example_java.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.moneycollect.android.model.MoneyCollectButtonViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.ui.view.MoneyCollectButtonView;
import com.moneycollect.android.utils.MoneyCollectButtonUtils;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivitySelectButtonTypeLayoutBinding;
import com.moneycollect.example_java.BaseExampleActivity;

/**
 * {@link SelectButtonTypeActivity} show the use of  {@link MoneyCollectButtonView}
 */
public class SelectButtonTypeActivity extends BaseExampleActivity implements View.OnClickListener{

    private ActivitySelectButtonTypeLayoutBinding viewBinding;

    /***  [MoneyCollectButtonView]*/
    private MoneyCollectButtonView buttonWidget;

    // PaymentModel (PAY)
    MoneyCollectPaymentModel moneyCollectPaymentModel = MoneyCollectPaymentModel.PAY;

    private Button startHoldingAnimButton;
    private Button startCompleteAnimButton;
    private Button stopAnimButton;
    private Button settingEnableButton;
    private Button settingUnEnableButton;

    //loading active
    private boolean isLoadingAnimStatus = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySelectButtonTypeLayoutBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initUi();
    }

    private void initUi() {
        startHoldingAnimButton=viewBinding.startHoldingAnim;
        startCompleteAnimButton=viewBinding.startCompleteAnim;
        stopAnimButton=viewBinding.stopAnim;
        settingEnableButton=viewBinding.settingButtonEnable;
        settingUnEnableButton=viewBinding.settingButtonUnenable;

        startHoldingAnimButton.setOnClickListener(this);
        startCompleteAnimButton.setOnClickListener(this);
        stopAnimButton.setOnClickListener(this);
        settingEnableButton.setOnClickListener(this);
        settingUnEnableButton.setOnClickListener(this);

        buttonWidget=viewBinding.buttonWidget;

        MoneyCollectButtonViewParams params= new MoneyCollectButtonViewParams.Builder()
                .activity(this)
                .moneyCollectPaymentModel(moneyCollectPaymentModel)
                .confirmButtonStatus(true)
                .confirmButtonText(getResources().getString(R.string.payment_select_button_example))
                .confirmButtonTextSize(getResources().getDimension(R.dimen.text_size_5sp))
                .confirmButtonAlpha(1.0f)
                .completeAnimTimeOut(3000L)
                .build();
        buttonWidget.setMoneyCollectButtonViewParams(params);

        if (buttonWidget.getCardConfirmButton()!=null) {
            //set button onClick
            buttonWidget.getCardConfirmButton().setOnClickListener(this);
        }
    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        if (view!=null) {
            if (!MoneyCollectButtonUtils.INSTANCE.isFastDoubleClick(view.getId(), 800)) {
                if (buttonWidget.getCardConfirmButton()!=null && view.getId() == buttonWidget.getCardConfirmButton().getId()){
                    isLoadingAnimStatus=true;
                    buttonWidget.setMoneyCollectButtonViewContext(this);
                    buttonWidget.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                    buttonWidgetAnimStart();
                }else if (view.getId() == R.id.start_holding_anim){
                    isLoadingAnimStatus=true;
                    buttonWidget.setMoneyCollectButtonViewContext(null);
                    buttonWidget.setMoneyCollectButtonViewModel(moneyCollectPaymentModel);
                    buttonWidget.showAnimByPaymentHolding();
                }else if (view.getId() == R.id.start_complete_anim){
                    isLoadingAnimStatus=true;
                    buttonWidget.setMoneyCollectButtonViewContext(null);
                    buttonWidget.setMoneyCollectButtonViewModel( moneyCollectPaymentModel);
                    buttonWidget.showAnimByPaymentComplete();
                }else if (view.getId() == R.id.stop_anim){
                    isLoadingAnimStatus=false;
                    buttonWidget.stopPaymentAnim();
                }else if (view.getId() == R.id.setting_button_enable){
                    if (!isLoadingAnimStatus){
                        buttonWidget.setCardConfirmButtonStatus(true);
                    }else{
                        showToast(getString(R.string.stop_anim_message_str));
                    }
                }else if (view.getId() == R.id.setting_button_unenable){
                    if (!isLoadingAnimStatus) {
                        buttonWidget.setCardConfirmButtonStatus(false);
                    }else{
                        showToast(getString(R.string.stop_anim_message_str));
                    }
                }
            }
        }
    }

    /**
     * Combination of holding and complete two animation use
     */
    private void buttonWidgetAnimStart(){
        Animation animation = AnimationUtils.loadAnimation(SelectButtonTypeActivity.this, R.anim.mc_button_scale_smaller);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonWidget.showAnimByPaymentHolding();
                new CountDownTimer(3000,10){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        if (isLoadingAnimStatus) {
                            buttonWidget.showAnimByPaymentComplete();
                        }
                    }
                }.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (buttonWidget.getCardConfirmButton()!=null) {
            buttonWidget.getCardConfirmButton().startAnimation(animation);
        }
    }
}
