package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(val context: Context) {

    val selectedFlightLeg = BehaviorSubject.create<FlightLeg>()
    val bundlePriceObserver = BehaviorSubject.create<String>()
    val urgencyMessagingObserver = BehaviorSubject.create<String>()
    val totalDurationObserver = BehaviorSubject.create<String>()
    val baggageFeeURLObserver = BehaviorSubject.create<String>()

    val selectedFlightClicked = BehaviorSubject.create<FlightLeg>()

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
            baggageFeeURLObserver.onNext(selectedFlight.baggageFeesUrl)
        }
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        selectedFlightClicked.onNext(selectedFlightLeg.value)
    }
}

