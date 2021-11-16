package com.moneycollect.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import com.moneycollect.android.R


@SuppressLint("UseCompatLoadingForDrawables")
fun setCardImg(context: Context?, iv: ImageView?, cardBrand: String?) {
    when {
        cardBrand!!.contains("Visa") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_visa)
        }
        cardBrand.contains("Master") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_mastercard)
        }
        cardBrand.contains("Ame") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_ae)
        }
        cardBrand.contains("JCB") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_jcb)
        }
        cardBrand.contains("Dinne") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_dinner)
        }
        cardBrand.contains("Discover") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_discover)
        }
        cardBrand.contains("Maestro") -> {
            iv!!.background = context?.resources?.getDrawable(R.drawable.mc_card_maestro)
        }
    }
}