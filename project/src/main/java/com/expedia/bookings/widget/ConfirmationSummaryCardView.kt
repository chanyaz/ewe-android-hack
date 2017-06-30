package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class ConfirmationSummaryCardView (context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    val numberOfTravelers : TextView by bindView(R.id.trip_number_of_travelers)
    val pointsEarned: TextView by bindView(R.id.trip_points_earned)
    val tripPrice: TextView by bindView(R.id.trip_total_cost)
    val totalDistance: TextView by bindView(R.id.total_travel_distance)

    init {
        View.inflate(context, R.layout.flight_confirmation_summary_card, this)
    }
}