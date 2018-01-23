package com.expedia.bookings.presenter.trips

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R

class ItinFetchProgressWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    init {
        View.inflate(context, R.layout.fetching_itins_progress_widget, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }
}
