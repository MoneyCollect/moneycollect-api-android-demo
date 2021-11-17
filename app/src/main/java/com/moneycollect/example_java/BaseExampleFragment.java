package com.moneycollect.example_java;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moneycollect.android.MoneyCollect;
import com.moneycollect.android.MoneyCollectFactory;
import com.moneycollect.example_java.utils.LoadingDialog;

/**
 * The parent class Fragment, the other Fragment to inherit it
 */
public class BaseExampleFragment extends Fragment {

    private LoadingDialog requestLoadingDialog;

    public MoneyCollect moneyCollect;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyCollect = new MoneyCollectFactory(getActivity().getApplication()).create();
    }

    /**
     * display tip
     */
    public void showToast(String description){
        Toast.makeText(getActivity().getApplicationContext(), description, Toast.LENGTH_SHORT).show();
    }

    /**
     * Is requesting interface display dialog
     */
    public void showLoadingDialog(){
        if (requestLoadingDialog == null) {
            requestLoadingDialog = LoadingDialog.getInstance(getActivity());
            if (requestLoadingDialog != null) {
                requestLoadingDialog.showDialog();
                requestLoadingDialog.setCanceledOnTouchOutside(false);
                requestLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                });
            }
        } else {
            requestLoadingDialog.show();
        }
    }

    /**
     * The hidden dialog when request interface
     */
    public void dismissLoadingDialog() {
        if (requestLoadingDialog != null) {
            requestLoadingDialog.hideDialog();
        }
    }

    @Override
    public void onDestroy() {
        if (requestLoadingDialog != null) {
            requestLoadingDialog.dismiss();
            requestLoadingDialog.onDetachedFromWindow();
            requestLoadingDialog = null;
        }
        super.onDestroy();
    }
}
