package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R

public class HotelCheckoutSummaryWidget(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    init {
        View.inflate(getContext(), R.layout.hotel_checkout_summary_widget, this)
    }

}
