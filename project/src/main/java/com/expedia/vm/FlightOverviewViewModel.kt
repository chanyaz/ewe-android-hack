package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(val context: Context, val showBundlePrice: Boolean) {

    val selectedFlightLegSubject = BehaviorSubject.create<FlightLeg>()
    val bundlePriceSubject = BehaviorSubject.create<String>()
    val showBundlePriceSubject = BehaviorSubject.create(showBundlePrice)
    val urgencyMessagingSubject = BehaviorSubject.create<String>()
    val totalDurationSubject = BehaviorSubject.create<CharSequence>()
    val baggageFeeURLSubject = BehaviorSubject.create<String>()
    val selectedFlightClickedSubject = BehaviorSubject.create<FlightLeg>()

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
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
            urgencyMessagingSubject.onNext(urgencyMessage)

            totalDurationSubject.onNext(PackageFlightUtils.getStylizedFlightDurationString(context, selectedFlight, R.color.packages_total_duration_text))

            var perPersonPrice = Phrase.from(context.resources.getString(R.string.package_flight_overview_per_person_TEMPLATE))
                    .put("money", selectedFlight.packageOfferModel.price.packageTotalPriceFormatted)
                    .format().toString()
            bundlePriceSubject.onNext(perPersonPrice)
            baggageFeeURLSubject.onNext(selectedFlight.baggageFeesUrl)
        }
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        selectedFlightClickedSubject.onNext(selectedFlightLegSubject.value)
    }
}

