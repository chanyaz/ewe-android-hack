package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.widget.shared.AbstractWebViewWidget

class BaggageFeeInfoWidget(context: Context, attrs: AttributeSet) : AbstractWebViewWidget(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.package_flight_overview_baggage_fees)
    }
}
