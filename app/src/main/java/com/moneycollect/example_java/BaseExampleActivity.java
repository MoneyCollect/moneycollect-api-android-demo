package com.moneycollect.example_java;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.moneycollect.android.MoneyCollect;
import com.moneycollect.android.MoneyCollectFactory;
import com.moneycollect.example_java.utils.LoadingDialog;

/**
 * The parent class Activity, the other Activity to inherit it
 */
public class BaseExampleActivity extends AppCompatActivity {

    private LoadingDialog requestLoadingDialog;

    public MoneyCollect moneyCollect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyCollect = new MoneyCollectFactory(getApplication()).create();
    }

    /**
     * display tip
     */
    public void showToast(String description){
        Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
    }

    /**
     * Is requesting interface display dialog
     */
    public void showLoadingDialog(){
        if (requestLoadingDialog == null) {
            requestLoadingDialog = LoadingDialog.getInstance(this);
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
    protected void onDestroy() {
        if (requestLoadingDialog != null) {
            requestLoadingDialog.dismiss();
            requestLoadingDialog.onDetachedFromWindow();
            requestLoadingDialog = null;
        }
        super.onDestroy();
    }
}
