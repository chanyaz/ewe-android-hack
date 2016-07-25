package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R


class HotelCrossSellWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.widget_hotel_cross_sell, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

}
