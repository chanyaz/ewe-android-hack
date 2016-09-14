package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.shared.AbstractWebViewWidget

class PaymentFeeInfoWebView(context: Context, attrs: AttributeSet) : AbstractWebViewWidget(context, attrs) {
    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.flights_flight_overview_payment_fees)
    }
}
