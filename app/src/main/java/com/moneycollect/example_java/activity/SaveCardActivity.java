package com.moneycollect.example_java.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.moneycollect.example_java.R;
import com.moneycollect.example_java.databinding.ActivityChooseSavedCardBinding;
import com.moneycollect.example_java.BaseExampleActivity;
import com.moneycollect.example_java.Constant;
import com.moneycollect.example_java.fragment.AddCardFragment;
import com.moneycollect.example_java.fragment.SaveCardFragment;

/**
 * {@link SaveCardActivity} contain  {@link SaveCardFragment} and {@link AddCardFragment}
 * Support them to switch to each other
 */
public class SaveCardActivity extends BaseExampleActivity {
    private static String CURRENT_PAYMENT="SavePaymentFragment" ;   //current fragment tag
    private static String SAVE_PAYMENT="SavePaymentFragment";  //save fragment tag
    private static String ADD_PAYMENT="AddPaymentFragment";   //add fragment tag

    private ActivityChooseSavedCardBinding viewBinding;

    private FragmentContainerView bottomContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        viewBinding = ActivityChooseSavedCardBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        if (getWindow()!=null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        initUi();
    }

    private void initUi() {
        bottomContainer= viewBinding.bottomContainer;
        switchContent(SAVE_PAYMENT);
    }


    /**
     * switch content
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void switchContent(String tag) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        if (fragmentManager!=null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (transaction != null) {
                for (Fragment fragment : fragmentManager.getFragments()) {
                    if (!fragment.getTag().equals(tag)) {
                        transaction.hide(fragment);
                    }
                }
                Fragment to = null;
                Bundle bundle = null;
                if (getIntent() != null) {
                    bundle = getIntent().getBundleExtra(Constant.CURRENT_PAYMENT_BUNDLE);
                }
                if (tag.equals(SAVE_PAYMENT)) {
                    to = new SaveCardFragment();
                    CURRENT_PAYMENT = SAVE_PAYMENT;
                } else if (tag.equals(ADD_PAYMENT)) {
                    to = new AddCardFragment();
                    CURRENT_PAYMENT = ADD_PAYMENT;
                }

                transaction.addToBackStack(tag)
                        .setCustomAnimations(R.animator.animator_enter, R.animator.animator_exit, R.animator.animator_enter, R.animator.animator_exit);
                if (to != null) {
                    if (bundle!=null) {
                        to.setArguments(bundle);
                    }
                    if (!to.isAdded()) {
                        transaction.add(bottomContainer.getId(), to, tag).commit();
                    } else {
                        transaction.show(to).commit();
                    }
                }
            }
        }
    }

    /**
     * click on the return key trigger
     */
    @Override
    public void onBackPressed() {
        if (CURRENT_PAYMENT.equals(SAVE_PAYMENT)){
            finish();
        }else if (CURRENT_PAYMENT.equals(ADD_PAYMENT)){
            switchContent(SAVE_PAYMENT);
        }
    }
}
