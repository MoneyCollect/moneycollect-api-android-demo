package com.moneycollect.example_java.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;

import com.moneycollect.example_java.R;

public class CardImageUtils {
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setCardImg(Context context, ImageView iv, String cardBrand) {
        if (cardBrand.contains("Visa")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_visa));
        }else  if (cardBrand.contains("Master")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_mastercard));
        }else if (cardBrand.contains("Ame")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_ae));
        }else if (cardBrand.contains("JCB")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_jcb));
        }else if (cardBrand.contains("Dinne")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_dinner));
        }else if (cardBrand.contains("Discover")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_discover));
        }else if (cardBrand.contains("Maestro")){
            iv.setBackground(context.getResources().getDrawable(R.drawable.mc_card_maestro));
        }
    }
}
