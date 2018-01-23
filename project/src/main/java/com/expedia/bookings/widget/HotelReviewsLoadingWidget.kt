package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R

class HotelReviewsLoadingWidget(context: Context) : FrameLayout(context) {
    init {
        View.inflate(context, R.layout.hotel_reviews_loading_widget, this)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}
