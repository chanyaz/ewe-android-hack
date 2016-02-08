package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightOverviewViewModel(val context: Context) {

    val selectedFlightLeg = BehaviorSubject.create<FlightLeg>()
    val bundlePriceObserver = BehaviorSubject.create<String>()
    val urgencyMessagingObserver = BehaviorSubject.create<String>()
    val totalDurationObserver = BehaviorSubject.create<String>()

    init {
        selectedFlightLeg.subscribe { selectedFlight ->
            var urgencyMessage = ""
            if (selectedFlight.packageOfferModel.urgencyMessage != null) {
                urgencyMessage = Phrase.from(context.resources.getString(R.string.package_flight_overview_urgency_message_TEMPLATE))
                        .put("seats", selectedFlight.packageOfferModel.urgencyMessage.ticketsLeft)
                        .format().toString()
            }
            if (urgencyMessage.isNotBlank()) {
                urgencyMessage += ", "
            }
            if (selectedFlight.packageOfferModel.price.deltaPositive) {
                urgencyMessage += "+" + selectedFlight.packageOfferModel.price.differentialPriceFormatted
            } else {
                urgencyMessage += selectedFlight.packageOfferModel.price.differentialPriceFormatted
            }
            urgencyMessagingObserver.onNext(urgencyMessage)
            var totalDuration = Phrase.from(context.resources.getString(R.string.package_flight_overview_total_duration_TEMPLATE))
                    .put("duration", PackageFlightUtils.getFlightDurationString(context, selectedFlight))
                    .format().toString()
            totalDurationObserver.onNext(totalDuration)
            var perPersonPrice = Phrase.from(context.resources.getString(R.string.package_flight_overview_per_person_TEMPLATE))
                    .put("money", selectedFlight.packageOfferModel.price.packageTotalPriceFormatted)
                    .format().toString()
            bundlePriceObserver.onNext(perPersonPrice)
        }
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        val params = Db.getPackageParams()
        val flight = selectedFlightLeg.value
        params.flightType = Constants.PACKAGE_FLIGHT_TYPE
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid
        if (flight.outbound) Db.setPackageSelectedOutboundFlight(flight) else Db.setPackageSelectedInboundFlight(flight)
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }
}

data class FlightSegmentBreakdown(val segment: FlightLeg.FlightSegment, val hasLayover: Boolean)

class FlightSegmentBreakdownViewModel(val context: Context) {
    val addSegmentRowsObserver = PublishSubject.create<List<FlightSegmentBreakdown>>()
}
