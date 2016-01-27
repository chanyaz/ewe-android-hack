package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.FlightOverviewViewModel

public class PackageFlightOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val flightTimeTextView: TextView by bindView(R.id.flight_overview_time)
    val selectFlightButton: Button by bindView(R.id.select_flight_button)
    val flightAirlineTextView: TextView by bindView(R.id.flight_overview_airline)
    val flightAirportsTextView: TextView by bindView(R.id.flight_overview_dest_origin)
    val flightDurationTextView: TextView by bindView(R.id.flight_overview_duration)

    var viewmodel: FlightOverviewViewModel by notNullAndObservable {
        viewmodel.flightTimeObserver.subscribeText(flightTimeTextView)
        viewmodel.flightAirlineObserver.subscribeText(flightAirlineTextView)
        viewmodel.flightAirportsObserver.subscribeText(flightAirportsTextView)
        viewmodel.flightDurationObserver.subscribeText(flightDurationTextView)
        selectFlightButton.subscribeOnClick(viewmodel.selectFlightClickObserver)
    }

    init {
        View.inflate(getContext(), R.layout.widget_flight_overview, this)
    }
}