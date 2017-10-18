package com.expedia.bookings.hotel.widget.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R

class HotelCompareEmptyView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.hotel_compare_empty_view, this)
    }
}