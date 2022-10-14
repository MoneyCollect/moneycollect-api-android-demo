package com.moneycollect.example_java.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.moneycollect.android.model.MoneyCollectCardListViewParams;
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.android.net.net.ApiResultCallback;
import com.moneycollect.android.ui.imp.MoneyCollectCardCallBackInterface;
import com.moneycollect.android.ui.view.MoneyCollectCardListView;
import com.moneycollect.example_java.databinding.FragmentSaveLayoutBinding;
import com.moneycollect.example_java.BaseExampleFragment;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.activity.SaveCardActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * {@link SaveCardFragment}
 * Show the payment list, Provide payment to the user, also can choose to add a new card, or jump {@link AddCardFragment}
 */
public class SaveCardFragment extends BaseExampleFragment implements View.OnClickListener, MoneyCollectCardCallBackInterface {

    private FragmentSaveLayoutBinding viewBinding;

    /***  [MoneyCollectCardListView]*/
    private MoneyCollectCardListView cardListLayout;

    // PaymentModel (ATTACH_PAYMENT_METHOD)
    private MoneyCollectPaymentModel currentModel= MoneyCollectPaymentModel.ATTACH_PAYMENT_METHOD;

    //customerId for pay
    private String customerId;

    //groupList params
    private  ArrayList<PaymentMethod>  groupList =new ArrayList<>();
    //childList params
    private  ArrayList<ArrayList<PaymentMethod>>  childList=new  ArrayList<>();

    //loading active
    private boolean isRequestLoading=false;

    private static String ADD_PAYMENT="AddPaymentFragment";

    @androidx.annotation.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        viewBinding =  FragmentSaveLayoutBinding.inflate(getLayoutInflater(), container, false);
        initUi();
        return viewBinding.getRoot();
    }

    private void initUi() {
        if (getArguments() != null) {
            currentModel = (MoneyCollectPaymentModel) getArguments().getSerializable(Constant.CURRENT_PAYMENT_MODEL);
            customerId = getArguments().getString(Constant.CUSTOMER_ID_TAG);
        }
        cardListLayout= viewBinding.mcCardList;
        MoneyCollectCardListViewParams moneyCollectCardListViewParams = new MoneyCollectCardListViewParams.Builder()
                .activity(getActivity())
                .moneyCollectPaymentModel(currentModel)
                .addMoneyCollectCardCallBackInterface(this)
                .addClickListener(this)
                .build();
        cardListLayout.setMoneyCollectCardListViewParams(moneyCollectCardListViewParams);
        selectAllPaymentMethods();
    }

    /**
     * select all paymentMethods
     */
    private void selectAllPaymentMethods() {
        if (TextUtils.isEmpty(customerId)){
            cardListLayout.setVisibility(View.VISIBLE);
            cardListLayout.changeFootViewVisible(true);
            return;
        }
        isRequestLoading=true;
        showLoadingDialog();
        moneyCollect.selectAllPaymentMethods(customerId, new ApiResultCallback<Object>() {
            @Override
            public void onSuccess(@NotNull Object result) {
                dismissLoadingDialog();
                cardListLayout.setVisibility(View.VISIBLE);
                if (result instanceof ArrayList){
                    if (((ArrayList) result).size()>0){
                        childList.add((ArrayList<PaymentMethod>) result);
                        if (childList.size()>0){
                            groupList.add(childList.get(0).get(0));
                            cardListLayout.setDataList(groupList,childList);
                        }else {
                            cardListLayout.changeFootViewVisible(true);
                        }
                    }else {
                        cardListLayout.changeFootViewVisible(true);

                    }
                }else {
                    cardListLayout.changeFootViewVisible(true);
                }
                isRequestLoading=false;
            }

            @Override
            public void onError(@NotNull Exception e) {
                dismissLoadingDialog();
                cardListLayout.setVisibility(View.VISIBLE);
                cardListLayout.changeFootViewVisible(true);
                isRequestLoading=false;
            }
        });

    }

    /**
     * click event
     */
    @Override
    public void onClick(View view) {
        Activity activity = getActivity();
        if (view!=null && activity != null) {
            if (view.getId() == cardListLayout.getToolbarBackIcon().getId() && !cardListLayout.isRequestLoading() && !isRequestLoading) {
                activity.finish();
            }else  if (view.getId() == cardListLayout.getChildFootView().getId() && !cardListLayout.isRequestLoading() && !isRequestLoading) {
                if(activity instanceof SaveCardActivity){
                    ((SaveCardActivity)activity).switchContent(ADD_PAYMENT);
                }
            }
        }
    }

    /**
     * Users in the payment list selected paymentMethod
     */
    @Override
    public void selectPaymentMethod(@Nullable PaymentMethod paymentMethod) {
        Activity activity = getActivity();
        if (null != paymentMethod && activity != null) {
            Intent intent = new Intent();
            intent.putExtra(Constant.SAVE_PAYMENT_METHOD, paymentMethod);
            activity.setResult(Constant.SAVE_RESULT_CODE, intent);
            activity.finish();
        }
    }
}
