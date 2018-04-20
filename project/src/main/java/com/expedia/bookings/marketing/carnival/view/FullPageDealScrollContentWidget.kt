package com.expedia.bookings.marketing.carnival.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.marketing.carnival.FullPageDealViewModel
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView

class FullPageDealScrollContentWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val dealInstructionsView: TextView by bindView(R.id.deal_instructions)
    private val promoCodeView: TextView by bindView(R.id.promo_code)
    private val offerDetailsTitleView: TextView by bindView(R.id.details_title)
    private val termsTitleView: TextView by bindView(R.id.terms_title)
    private val termsDescriptionView: TextView by bindView(R.id.terms_description)
    private val copyToClipboardButton: CardView by bindView(R.id.copy_code_button)
    private val copyToClipboardText: TextView by bindView(R.id.in_app_cta_shop_deal_text_view)
    private val offerDetailsDescriptionView: TextView by bindView(R.id.offer_details_description)
    val titleView: TextView by bindView(R.id.deal_title)
    private val detailsHorizontalLineView: View by bindView(R.id.details_horizontal_line)
    private val termsHorizontalLineView: View by bindView(R.id.terms_horizontal_line)
    private var fullPageDealViewModel: FullPageDealViewModel

    init {
        View.inflate(context, R.layout.full_page_deal_scroll_content, this)
        val carnivalMessage = (context as AppCompatActivity).intent.getParcelableExtra(Constants.CARNIVAL_MESSAGE_DATA) as CarnivalMessage
        fullPageDealViewModel = FullPageDealViewModel(carnivalMessage)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        titleView.text = fullPageDealViewModel.dealTitle
        dealInstructionsView.text = fullPageDealViewModel.dealInstructions
        promoCodeView.text = fullPageDealViewModel.promoCodeText
        offerDetailsTitleView.text = fullPageDealViewModel.detailsTitle
        offerDetailsDescriptionView.text = fullPageDealViewModel.detailsDescription
        termsTitleView.text = fullPageDealViewModel.termsTitle
        termsDescriptionView.text = fullPageDealViewModel.termsDescription

        if (titleView.text.isNullOrEmpty()) {
            titleView.visibility = View.GONE
        }

        if (dealInstructionsView.text.isNullOrEmpty()) {
            dealInstructionsView.visibility = View.GONE
        }

        if (promoCodeView.text.isNullOrEmpty()) {
            promoCodeView.visibility = View.GONE
            copyToClipboardButton.visibility = View.GONE
        }

        if (offerDetailsTitleView.text.isNullOrEmpty() || offerDetailsDescriptionView.text.isNullOrEmpty()) {
            offerDetailsTitleView.visibility = View.GONE
        }

        if (offerDetailsDescriptionView.text.isNullOrEmpty()) {
            offerDetailsDescriptionView.visibility = View.GONE
            detailsHorizontalLineView.visibility = View.GONE
        }

        if (termsTitleView.text.isNullOrEmpty() || termsDescriptionView.text.isNullOrEmpty()) {
            termsTitleView.visibility = View.GONE
        }

        if (termsDescriptionView.text.isNullOrEmpty()) {
            termsDescriptionView.visibility = View.GONE
            termsHorizontalLineView.visibility = View.GONE
        }

        handleCopyToClipboardClick()
    }

    private fun handleCopyToClipboardClick() {
        copyToClipboardButton.setOnClickListener({
            updateButtonAppearanceToClickedState()
            copyToClipBoard(promoCodeView.text.toString())
        })
    }

    private fun updateButtonAppearanceToClickedState() {
        copyToClipboardButton.setBackgroundColor(ContextCompat.getColor((context as AppCompatActivity), R.color.transparent))
        copyToClipboardButton.elevation = 0f
        copyToClipboardText.setTextColor(ContextCompat.getColor((context as AppCompatActivity), R.color.success_green))
        copyToClipboardText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.deals_checkmark, 0, 0, 0)
        copyToClipboardText.text = (context as AppCompatActivity).getString(R.string.full_page_deal_code_copied)
    }

    private fun copyToClipBoard(text: String) {
        ClipboardUtils.setText((context as AppCompatActivity), text)
    }
}
