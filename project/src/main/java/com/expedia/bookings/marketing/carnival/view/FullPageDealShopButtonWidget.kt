package com.expedia.bookings.marketing.carnival.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.marketing.carnival.FullPageDealViewModel
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView

class FullPageDealShopButtonWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var fullPageDealViewModel: FullPageDealViewModel
    private val shopButton: LinearLayout by bindView(R.id.full_page_deal_shop_button)
    private val shopButtonText: TextView by bindView(R.id.shop_button_text)

    init {
        View.inflate(context, R.layout.full_page_deal_shop_button, this)
        val carnivalMessage = (context as AppCompatActivity).intent.getParcelableExtra(Constants.CARNIVAL_MESSAGE_DATA) as CarnivalMessage
        fullPageDealViewModel = FullPageDealViewModel(carnivalMessage)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        setShopButtonText()
        handleShopButtonAnimationAndClick(fullPageDealViewModel.deeplink)
    }

    private fun setShopButtonText() {
        if (!fullPageDealViewModel.shopButtonText.isNullOrEmpty() && !fullPageDealViewModel.deeplink.isNullOrEmpty()) {
            shopButtonText.text = fullPageDealViewModel.shopButtonText
        } else if (fullPageDealViewModel.deeplink.isNullOrEmpty()) {
            shopButtonText.text = context.getString(R.string.ok)
        } else {
            shopButtonText.text = context.getString(R.string.see_deal)
        }
    }

    private fun handleShopButtonAnimationAndClick(deeplink: String?) {
        shopButton.visibility = View.GONE
        slideShopButtonInAfterSeconds(1)
        setShopButtonClickListener(deeplink)
    }

    private fun setShopButtonClickListener(deeplink: String?) {
        shopButton.setOnClickListener({
            if (!deeplink.isNullOrEmpty()) {
                val deeplinkUri = Uri.parse(deeplink)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = deeplinkUri
                context.startActivity(intent)
            } else {
                shopButtonText.text = context.getString(R.string.ok)
                (context as AppCompatActivity).finish()
            }
        })
    }

    private fun slideShopButtonInAfterSeconds(seconds: Long) {
        shopButton.postDelayed({
            if (!(context as AppCompatActivity).isFinishing) {
                AnimUtils.slideUp(shopButton)
                shopButton.visibility = View.VISIBLE
            }
        }, 1000 * seconds)
    }
}
