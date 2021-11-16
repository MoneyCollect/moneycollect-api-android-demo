package com.moneycollect.example.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.moneycollect.android.model.enumeration.MoneyCollectPaymentModel
import com.moneycollect.example.BaseExampleActivity
import com.moneycollect.example.Constant
import com.moneycollect.example.R
import com.moneycollect.example.TestRequestData
import com.moneycollect.example.databinding.ActivityPayCardBinding
import com.moneycollect.example.fragment.AddWithPaymentFragment
import com.moneycollect.example.fragment.SaveWithPaymentCardFragment

/**
 * [PayCardActivity] contain [SaveWithPaymentCardFragment] and [AddWithPaymentFragment],Support them to switch to each other
 */
class PayCardActivity : BaseExampleActivity(){

    private var viewBinding: ActivityPayCardBinding?=null

    private var bottomContainer : FragmentContainerView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        viewBinding = ActivityPayCardBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        initUi()
    }

    private fun initUi() {
        bottomContainer= viewBinding!!.bottomContainer
        switchContent(SAVE_PAYMENT)
    }

    /**
     * switch content
     */
     @SuppressLint("UseCompatLoadingForDrawables")
     fun switchContent(tag: String?) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        for (fragment in supportFragmentManager.fragments) {
            if (!fragment.tag.equals(tag)) {
                transaction.hide(fragment)
            }
        }
         var to:Fragment?=null
         val bundle = intent.extras?.getBundle(Constant.CURRENT_PAYMENT_BUNDLE)
         when (tag) {
             SAVE_PAYMENT -> {
                 to = SaveWithPaymentCardFragment()
                 CURRENT_PAYMENT = SAVE_PAYMENT
             }
             ADD_PAYMENT -> {
                 to = AddWithPaymentFragment()
                 CURRENT_PAYMENT = ADD_PAYMENT
             }
         }

         to?.arguments=bundle

         transaction.addToBackStack(tag)
             .setCustomAnimations(R.animator.animator_enter,
                 R.animator.animator_exit,
                 R.animator.animator_enter,
                 R.animator.animator_exit)
         if (to != null) {
             if (!to.isAdded) {
                 transaction.add(bottomContainer!!.id, to, tag).commit()
             } else {
                 transaction.show(to).commit()
             }
         }
    }

    companion object{
        var CURRENT_PAYMENT:String="SaveWithPaymentCardFragment"    //current fragment tag
        const val SAVE_PAYMENT:String="SaveWithPaymentCardFragment"  //save fragment tag
        const val ADD_PAYMENT:String="AddWithPaymentFragment"   //add fragment tag
    }

    /**
     * click on the return key trigger
     */
    override fun onBackPressed() {
        when (CURRENT_PAYMENT) {
            SAVE_PAYMENT -> {
                this@PayCardActivity.finish()
            }
            ADD_PAYMENT -> {
                switchContent(SAVE_PAYMENT)
            }
        }
    }
}