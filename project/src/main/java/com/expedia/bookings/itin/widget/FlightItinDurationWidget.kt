package com.expedia.bookings.itin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinDurationViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeTextAndVisibility

class FlightItinDurationWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val flightDurationTextview: TextView by bindView(R.id.flight_itin_total_duration_textview)

    var viewModel: FlightItinDurationViewModel by notNullAndObservable {
        viewModel.totalDurationSubject.subscribeTextAndVisibility(flightDurationTextview)
        viewModel.totalDurationContDescSubject.subscribeContentDescription(flightDurationTextview)
    }

    init {
        View.inflate(context, R.layout.widget_itin_flight_duration, this)
    }
}


