package com.expedia.bookings.itin.triplist

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R

class CancelledTripListView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    init {
        View.inflate(context, R.layout.trip_folders_cancelled_tab, this)
    }
}