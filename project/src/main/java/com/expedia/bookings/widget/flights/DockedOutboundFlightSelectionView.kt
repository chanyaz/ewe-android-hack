package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.SelectedOutboundFlightViewModel

class DockedOutboundFlightSelectionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val airlineNameTextView by bindView<TextView>(R.id.airline_name)
    val arrivalDepartureTimeTextView by bindView<TextView>(R.id.arrival_departure_time)

    var viewModel: SelectedOutboundFlightViewModel by notNullAndObservable<SelectedOutboundFlightViewModel> { vm ->
        vm.airlineNameObservable.subscribeText(airlineNameTextView)
        vm.arrivalDepartureTimeObservable.subscribeText(arrivalDepartureTimeTextView)
    }

    init {
        View.inflate(context, R.layout.widget_docked_outbound_flight_selection, this)
    }
}
