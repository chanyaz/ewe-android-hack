package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibilityInvisible
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel

public class PackageFlightOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val bundlePriceTextView: TextView by bindView(R.id.bundle_price)
    val selectFlightButton: Button by bindView(R.id.select_flight_button)
    val urgencyMessagingText: TextView by bindView(R.id.flight_overview_urgency_messaging)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)

    var vm: FlightOverviewViewModel by notNullAndObservable {
        vm.bundlePriceObserver.subscribeText(bundlePriceTextView)
        vm.urgencyMessagingObserver.subscribeTextAndVisibilityInvisible(urgencyMessagingText)
        vm.totalDurationObserver.subscribeText(totalDurationText)
        selectFlightButton.subscribeOnClick(vm.selectFlightClickObserver)
        vm.selectedFlightLeg.subscribe { selectedFlight ->
            var segmentbreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentbreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover))
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentbreakdowns)
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_flight_overview, this)
        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
    }
}