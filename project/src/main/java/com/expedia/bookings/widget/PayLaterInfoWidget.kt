package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase
import kotlin.properties.Delegates

public class PayLaterInfoWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val container: ViewGroup by bindView(R.id.container)
    val payNowRateText: TextView by bindView(R.id.etp_pay_now_charges_text)
    val payNowCurrencyText: TextView by bindView(R.id.etp_pay_now_currency_text)
    val payLaterCurrencyText: TextView by bindView(R.id.etp_pay_later_currency_text)

    val statusBarHeight by Delegates.lazy { Ui.getStatusBarHeight(context) }
    val toolBarHeight by Delegates.lazy { Ui.getToolbarSize(context) }

    init {
        View.inflate(getContext(), R.layout.widget_pay_later_info, this)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        toolbar.setNavigationOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        container.setPadding(0, toolBarHeight + statusBarHeight, 0, 0)
    }

    fun setText(hotelCountryCode: String) {
        val userCountryCode = PointOfSale.getPointOfSale().getThreeLetterCountryCode()
        val currency = CurrencyUtils.currencyForLocale(userCountryCode)
        payNowRateText.setText(Phrase.from(getContext(), R.string.etp_pay_now_charges_text_TEMPLATE).put("brand", BuildConfig.brand).format())
        payNowCurrencyText.setText(getResources().getString(R.string.etp_pay_now_currency_text_TEMPLATE, currency))
        payLaterCurrencyText.setText(getResources().getString(R.string.etp_pay_later_currency_text_TEMPLATE, hotelCountryCode))
    }

}
