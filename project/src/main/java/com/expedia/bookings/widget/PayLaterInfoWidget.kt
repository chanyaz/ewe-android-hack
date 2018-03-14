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
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase

class PayLaterInfoWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val container: ViewGroup by bindView(R.id.container)
    val payNowRateText: TextView by bindView(R.id.etp_pay_now_charges_text)
    val payNowCurrencyText: TextView by bindView(R.id.etp_pay_now_currency_text)
    val payLaterCurrencyText: TextView by bindView(R.id.etp_pay_later_currency_text)
    val noChargeText: TextView by bindView(R.id.no_charges_text)
    val depositTermsFirstText: TextView by bindView(R.id.deposit_terms_first_text)
    val depositTermsSecondText: TextView by bindView(R.id.deposit_terms_second_text)
    val earnText: TextView by bindView(R.id.etp_earn_text)
    val depositExceedInfoView: LinearLayout by bindView(R.id.deposit_exceed_info_view)
    val payLaterPaymentInfo: LinearLayout by bindView(R.id.pay_later_payment_info_view)
    val depositPolicyFirstView: LinearLayout by bindView(R.id.deposit_terms_first_view)
    val depositPolicySecondView: LinearLayout by bindView(R.id.deposit_terms_second_view)
    val earnTextLayout: LinearLayout by bindView(R.id.earn_text_layout)

    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val toolBarHeight by lazy { Ui.getToolbarSize(context) }

    init {
        View.inflate(getContext(), R.layout.widget_pay_later_info, this)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        toolbar.setNavigationOnClickListener {
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        container.setPadding(0, toolBarHeight + statusBarHeight, 0, 0)
        earnText.text = resources.getString(R.string.etp_pay_now_earn_text)
        earnTextLayout.visibility = if (earnText.text.equals("")) View.GONE else View.VISIBLE
    }

    fun setText(values: Pair<String, List<HotelOffersResponse.HotelRoomResponse>>) {
        val userCountryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
        val currency = CurrencyUtils.currencyForLocale(userCountryCode)
        val hotelCountryCode = values.first
        val hotelCountryCurrency = CurrencyUtils.currencyForLocale(hotelCountryCode)
        var isDepositRequired = false
        var payLaterOffer: HotelOffersResponse.HotelRoomResponse? = null

        for (room in values.second) {
            if (room.payLaterOffer != null) {
                isDepositRequired = room.payLaterOffer.depositRequired
                if (isDepositRequired) {
                    payLaterOffer = room.payLaterOffer
                    break
                }
            }
        }

        depositPolicyFirstView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        depositExceedInfoView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        payLaterPaymentInfo.visibility = if (isDepositRequired) View.GONE else View.VISIBLE
        depositPolicyFirstView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE
        depositPolicySecondView.visibility = if (isDepositRequired) View.VISIBLE else View.GONE

        if (isDepositRequired) {
            depositTermsFirstText.text = payLaterOffer?.depositPolicyAtIndex(0)
            depositTermsSecondText.text = payLaterOffer?.depositPolicyAtIndex(1)
        }

        payNowRateText.text = Phrase.from(context, R.string.etp_pay_now_charges_text_TEMPLATE).put("brand", BuildConfig.brand).format()
        payNowCurrencyText.text = resources.getString(R.string.etp_pay_now_currency_text_TEMPLATE, currency)
        payLaterCurrencyText.text = resources.getString(R.string.etp_pay_later_currency_text_TEMPLATE, hotelCountryCurrency)
        noChargeText.text = Phrase.from(context, R.string.no_charge_text_TEMPLATE).put("brand", BuildConfig.brand).format()
    }
}
