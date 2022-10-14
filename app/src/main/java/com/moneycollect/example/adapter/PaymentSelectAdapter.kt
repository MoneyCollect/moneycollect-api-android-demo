package com.moneycollect.example.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.moneycollect.android.model.response.PaymentMethod
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.utils.*

class PaymentSelectAdapter constructor(
    private val context: Context,
) : RecyclerView.Adapter<PaymentSelectAdapter.ExamplesViewHolder>() {

    private var itemClickListener: IKotlinCustomItemClickListener? = null

    private var currentPosition = 0

    var items = TestRequestData.testAllBankList


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ExamplesViewHolder {
        val root = LayoutInflater.from(context)
            .inflate(R.layout.item_payment_select_layout, viewGroup, false)
        return ExamplesViewHolder(root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(examplesViewHolder: ExamplesViewHolder, position: Int) {
        val itemView = examplesViewHolder.itemView

        val paymentMethod=items[position]
        val checkbox=itemView.findViewById<MaterialCheckBox>(R.id.layout_adapter_child_cb)
        val cardTv=itemView.findViewById<TextView>(R.id.layout_adapter_child_tv)
        val cardImage=itemView.findViewById<ImageView>(R.id.layout_adapter_child_cardiv)

        if (currentPosition==position) {
            checkbox.isChecked = true
            checkbox.buttonTintList =
                ColorStateList.valueOf(context.resources.getColor(com.moneycollect.android.R.color.mc_color_bluebtn))
        } else {
            checkbox.isChecked = false
            checkbox.buttonTintList =
                ColorStateList.valueOf(context.resources.getColor(com.moneycollect.android.R.color.mc_color_graybtn))
        }

        cardTv.text=getCardTypeStr(paymentMethod.type)
        setCardImg(cardImage, paymentMethod.type)

        // click event
        itemView.setOnClickListener {
            itemClickListener!!.onItemClickListener(position)
        }
    }

    fun getItem(i: Int): PaymentMethod {
        if (i <= items.size) {
            return items[i]
        }
        return PaymentMethod()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // set
    fun setCurrentPosition(position: Int) {
        this.currentPosition = position
        notifyDataSetChanged()
    }

    // set
    fun setOnKotlinItemClickListener(itemClickListener: IKotlinCustomItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    /**
     *    interface custom
     */
    interface IKotlinCustomItemClickListener {
        fun onItemClickListener(position: Int)
    }

    class ExamplesViewHolder constructor(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)


    fun getCardTypeStr(cardType:String?):String {
        return when (cardType) {
            CheckoutCreditCardCurrency.CREDIT_CARD.code -> {
                CheckoutCreditCardCurrency.CREDIT_CARD.code
            }
            CheckoutLocalCurrency.Atome.code -> {
                CheckoutLocalCurrency.Atome.name
            }
            CheckoutLocalCurrency.TrueMoney.code -> {
                CheckoutLocalCurrency.TrueMoney.name
            }
            CheckoutLocalCurrency.DANA.code -> {
                CheckoutLocalCurrency.DANA.name
            }
            CheckoutLocalCurrency.GCash.code -> {
                CheckoutLocalCurrency.GCash.name
            }
            CheckoutLocalCurrency.TNG.code -> {
                CheckoutLocalCurrency.TNG.name
            }
            CheckoutLocalCurrency.KAKAO_PAY.code -> {
                kakaoPayName
            }
            CheckoutLocalCurrency.Klarna.code -> {
                CheckoutLocalCurrency.Klarna.name
            }
            CheckoutLocalCurrency.POLi.code -> {
                CheckoutLocalCurrency.POLi.name
            }
            CheckoutLocalCurrency.MyBank.code -> {
                CheckoutLocalCurrency.MyBank.name
            }
            CheckoutLocalCurrency.EPS.code -> {
                CheckoutLocalCurrency.EPS.name
            }
            CheckoutLocalCurrency.Przelewy24.code -> {
                CheckoutLocalCurrency.Przelewy24.name
            }
            CheckoutLocalCurrency.Bancontact.code -> {
                CheckoutLocalCurrency.Bancontact.name
            }
            CheckoutLocalCurrency.Ideal.code -> {
                CheckoutLocalCurrency.Ideal.name
            }
            CheckoutLocalCurrency.Giropay.code -> {
                CheckoutLocalCurrency.Giropay.name
            }
            CheckoutLocalCurrency.Sofort.code -> {
                CheckoutLocalCurrency.Sofort.name
            }
            CheckoutLocalCurrency.AlipayHK.code -> {
                CheckoutLocalCurrency.AlipayHK.name
            }
            CheckoutLocalCurrency.Alipay.code -> {
                CheckoutLocalCurrency.Alipay.name
            }
            CheckoutLocalCurrency.WECHAT_PAY.code -> {
                wechatPayName
            }
            else ->{
                if (cardType!=null && !TextUtils.isEmpty(cardType)){
                    cardType
                }else{
                    ""
                }
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setCardImg(iv: ImageView?, cardBrand: String?) {
        when {
            cardBrand!!.contains(CheckoutCardPayCurrency.VISA.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_visa)
            }
            cardBrand.contains(CheckoutCardPayCurrency.MASTER.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_mastercard)
            }
            cardBrand.contains(CheckoutCardPayCurrency.AME.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_ae)
            }
            cardBrand.contains(CheckoutCardPayCurrency.JCB.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_jcb)
            }
            cardBrand.contains(CheckoutCardPayCurrency.DINNE.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_dinner)
            }
            cardBrand.contains(CheckoutCardPayCurrency.DISCOVER.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_discover)
            }
            cardBrand.contains(CheckoutCardPayCurrency.MAESTRO.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_maestro)
            }
            cardBrand.contains(CheckoutLocalCurrency.KAKAO_PAY.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_kakaopay)
            }

            cardBrand.contains(CheckoutLocalCurrency.Atome.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_atome)
            }
            cardBrand.contains(CheckoutLocalCurrency.TNG.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_tng)
            }
            cardBrand.contains(CheckoutLocalCurrency.GCash.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_gcash)
            }
            cardBrand.contains(CheckoutLocalCurrency.DANA.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_dana)
            }
            cardBrand.contains(CheckoutLocalCurrency.TrueMoney.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_truemoney)
            }

            cardBrand.contains(CheckoutLocalCurrency.Klarna.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_klarna)
            }
            cardBrand.contains(CheckoutLocalCurrency.POLi.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_poli)
            }
            cardBrand.contains(CheckoutLocalCurrency.MyBank.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_mybank)
            }
            cardBrand.contains(CheckoutLocalCurrency.EPS.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_eps)
            }
            cardBrand.contains(CheckoutLocalCurrency.Przelewy24.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_przelewy24)
            }
            cardBrand.contains(CheckoutLocalCurrency.Bancontact.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_bancontact)
            }
            cardBrand.contains(CheckoutLocalCurrency.Ideal.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_ideal)
            }
            cardBrand.contains(CheckoutLocalCurrency.Giropay.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_giropay)
            }
            cardBrand.contains(CheckoutLocalCurrency.Sofort.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_sofort)
            }
            cardBrand.contains(CheckoutLocalCurrency.AlipayHK.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_alipayhk)
            }
            cardBrand.contains(CheckoutLocalCurrency.Alipay.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_alipay)
            }
            cardBrand.contains(CheckoutLocalCurrency.WECHAT_PAY.code) -> {
                iv?.background = context.resources.getDrawable(R.drawable.mc_card_wechat)
            }
            else ->{
                if (cardBrand.contains(CheckoutCreditCardCurrency.CREDIT_CARD.code)) {
                    iv?.background = context.resources.getDrawable(R.drawable.mc_card_credit)
                }else{
                    iv?.background = context.resources.getDrawable(R.drawable.mc_card_local_default)
                }
            }
        }
    }
}