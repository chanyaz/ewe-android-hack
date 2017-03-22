package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class PaymentFeeInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = context.getString(R.string.flights_flight_overview_payment_fees)
    }
}
