package com.moneycollect.example_java.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.moneycollect.android.model.response.PaymentMethod;
import com.moneycollect.example_java.R;
import com.moneycollect.example_java.TestRequestData;
import com.moneycollect.example_java.utils.CurrencyUtils;
import java.util.ArrayList;


public class PaymentSelectAdapter extends RecyclerView.Adapter<PaymentSelectAdapter.ExamplesViewHolder>{
    private Context context;

    private IKotlinCustomItemClickListener itemClickListener;

    private int currentPosition = 0;

    public ArrayList<PaymentMethod> items = TestRequestData.Companion.getTestAllBankList();

    public PaymentSelectAdapter(Context context) {
        this.context = context;
    }



    public PaymentMethod getItem(int i) {
        if (i <= items.size()) {
            return items.get(i);
        }
        return new PaymentMethod();
    }

    @NonNull
    @Override
    public ExamplesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context)
                .inflate(R.layout.item_payment_select_layout, parent, false);
        return new ExamplesViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamplesViewHolder holder, int position) {
        View itemView = holder.itemView;

        PaymentMethod paymentMethod=items.get(position);
        MaterialCheckBox checkbox=itemView.findViewById(R.id.layout_adapter_child_cb);
        TextView cardTv=itemView.findViewById(R.id.layout_adapter_child_tv);
        ImageView cardImage=itemView.findViewById(R.id.layout_adapter_child_cardiv);

        if (currentPosition==position) {
            checkbox.setChecked(true);
            checkbox.setButtonTintList(ColorStateList.valueOf(context.getResources().getColor(com.moneycollect.android.R.color.mc_color_bluebtn)));
        } else {
            checkbox.setChecked(false);
            checkbox.setButtonTintList(ColorStateList.valueOf(context.getResources().getColor(com.moneycollect.android.R.color.mc_color_graybtn)));
        }

        cardTv.setText(getCardTypeStr(paymentMethod.type));
        setCardImg(cardImage, paymentMethod.type);

        // click event
        itemView.setOnClickListener(v -> {
            itemClickListener.onItemClickListener(position);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    // set
    public void setCurrentPosition(int position) {
        this.currentPosition = position;
        notifyDataSetChanged();
    }

    public ArrayList<PaymentMethod> getItems() {
        return  items;
    }

    /**
     *    interface custom
     */
    public interface IKotlinCustomItemClickListener {
        void onItemClickListener(int position);
    }

    // set
    public void setOnKotlinItemClickListener(IKotlinCustomItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    class ExamplesViewHolder extends RecyclerView.ViewHolder{
        public ExamplesViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    private String getCardTypeStr(String cardType) {
        String type="";
        if (cardType.equals(CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode())){
            type = CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Atome.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Atome.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.TrueMoney.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.TrueMoney.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.DANA.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.DANA.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.GCash.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.GCash.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.TNG.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.TNG.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.KAKAO_PAY.getCode())){
            type = CurrencyUtils.kakaoPayName;
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Klarna.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Klarna.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.POLi.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.POLi.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.MyBank.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.MyBank.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.EPS.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.EPS.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Przelewy24.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Przelewy24.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Bancontact.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Bancontact.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Ideal.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Ideal.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Giropay.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Giropay.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Sofort.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Sofort.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.AlipayHK.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.AlipayHK.name();
        } else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.Alipay.getCode())){
            type = CurrencyUtils.CheckoutLocalCurrency.Alipay.name();
        }else if (cardType.equals(CurrencyUtils.CheckoutLocalCurrency.WECHAT_PAY.getCode())){
            type = CurrencyUtils.wechatPayName;
        } else {
            if (cardType!=null && !TextUtils.isEmpty(cardType)){
                type = cardType;
            }
        }
        return type;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setCardImg(ImageView cardImage, String cardType) {
        if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.VISA.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_visa));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.MASTER.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_mastercard));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.AME.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_ae));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.JCB.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_jcb));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.DINNE.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_dinner));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.DISCOVER.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_discover));
        }else if (cardType.contains(CurrencyUtils.CheckoutCardPayCurrency.MAESTRO.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_maestro));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Atome.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_atome));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.TrueMoney.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_truemoney));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.DANA.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_dana));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.GCash.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_gcash));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.TNG.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_tng));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.KAKAO_PAY.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_kakaopay));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Klarna.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_klarna));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.POLi.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_poli));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.MyBank.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_mybank));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.EPS.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_eps));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Przelewy24.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_przelewy24));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Bancontact.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_bancontact));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Ideal.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_ideal));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Giropay.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_giropay));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Sofort.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_sofort));
        } else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.AlipayHK.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_alipayhk));
        }  else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.Alipay.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_alipay));
        }else if (cardType.contains(CurrencyUtils.CheckoutLocalCurrency.WECHAT_PAY.getCode())){
            cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_wechat));
        } else {
            if (cardType.contains(CurrencyUtils.CheckoutCreditCardCurrency.CREDIT_CARD.getCode())){
                cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_credit));
            }else {
                cardImage.setBackground(context.getResources().getDrawable(R.drawable.mc_card_local_default));
            }
        }
    }
}
