package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.flights.DockedOutboundFlightV2ViewModel

class DockedOutboundFlightWidgetV2(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val airlineNameTextView by bindView<TextView>(R.id.airline_name)
    val arrivalDepartureTimeTextView by bindView<TextView>(R.id.arrival_departure_time)
    val pricePerPersonTextView by bindView<TextView>(R.id.price_per_person)

    var viewModel: DockedOutboundFlightV2ViewModel by notNullAndObservable<DockedOutboundFlightV2ViewModel> { vm ->
        vm.airlineNameObservable.subscribeText(airlineNameTextView)
        vm.arrivalDepartureTimeObservable.subscribeText(arrivalDepartureTimeTextView)
        vm.pricePerPersonObservable.subscribeText(pricePerPersonTextView)

    }
    init {
        View.inflate(context, R.layout.widget_docked_outbound_flight_selection_v2, this)
    }

}