package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class ConfirmationSummaryCardView (context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    val numberOfTravelers : TextView by bindView(R.id.number_travelers)
    val pointsAdded : TextView by bindView(R.id.points_rewards)
    val tripPrice: TextView by bindView(R.id.trip_total_cash)

    init {
        View.inflate(context, R.layout.flight_confirmation_summary_card, this)
    }
}