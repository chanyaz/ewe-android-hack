package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class PaymentFeeInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    val airlinesChargePaymentFees = PointOfSale.getPointOfSale().shouldShowAirlinePaymentMethodFeeMessage()

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = context.getString(if (airlinesChargePaymentFees) R.string.Airline_fee else R.string.flights_flight_overview_payment_fees)
    }
}
