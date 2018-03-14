package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase

class DepositTermsInfoWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val container: ViewGroup by bindView(R.id.container)
    val payLaterCurrencyText: TextView by bindView(R.id.etp_pay_later_currency_text)
    val noChargeText: TextView by bindView(R.id.no_charges_text)
    val depositTermsFirstText: TextView by bindView(R.id.deposit_terms_first_text)
    val depositTermsSecondText: TextView by bindView(R.id.deposit_terms_second_text)
    val depositPolicyFirstView: LinearLayout by bindView(R.id.deposit_terms_first_view)
    val depositPolicySecondView: LinearLayout by bindView(R.id.deposit_terms_second_view)
    val depositExceedInfoView: android.widget.LinearLayout by bindView(com.expedia.bookings.R.id.deposit_exceed_info)
    val freeCancellationMessageView: android.widget.LinearLayout by bindView(com.expedia.bookings.R.id.free_cancellation_deposit_messaging)

    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val toolBarHeight by lazy { Ui.getToolbarSize(context) }

    init {
        View.inflate(getContext(), R.layout.widget_deposit_terms_info, this)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        toolbar.setNavigationOnClickListener {
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        container.setPadding(0, toolBarHeight + statusBarHeight, 0, 0)
    }

    fun setText(values: Pair<String, HotelOffersResponse.HotelRoomResponse>) {
        val hotelCountryCode = values.first
        val hotelCountrycurrency = CurrencyUtils.currencyForLocale(hotelCountryCode)
        val offer = values.second.payLaterOffer ?: values.second
        val isDepositRequired = offer.depositRequired
        val depositPolicyFirst = offer.depositPolicyAtIndex(0)
        val depositPolicySecond = offer.depositPolicyAtIndex(1)

        depositPolicyFirstView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        depositExceedInfoView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        depositPolicyFirstView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        depositPolicySecondView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        freeCancellationMessageView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE

        if (isDepositRequired) {
            depositTermsFirstText.text = depositPolicyFirst
            depositTermsSecondText.text = depositPolicySecond
        }

        payLaterCurrencyText.text = resources.getString(R.string.etp_pay_later_currency_text_TEMPLATE, hotelCountrycurrency)
        noChargeText.text = Phrase.from(context, R.string.no_charge_text_TEMPLATE).put("brand", BuildConfig.brand).format()
    }
}
