package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject

abstract class AbstractFlightOverviewViewModel(val context: Context) {
    val selectedFlightLegSubject = BehaviorSubject.create<FlightLeg>()
    val bundlePriceSubject = BehaviorSubject.create<String>()
    val urgencyMessagingSubject = BehaviorSubject.create<String>()
    val totalDurationSubject = BehaviorSubject.create<CharSequence>()
    val baggageFeeURLSubject = BehaviorSubject.create<String>()
    val selectedFlightClickedSubject = BehaviorSubject.create<FlightLeg>()
    var numberOfTravelers = BehaviorSubject.create<Int>(0)

    abstract val showBundlePriceSubject: BehaviorSubject<Boolean>

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
            updateUrgencyMessage(selectedFlight)

            totalDurationSubject.onNext(PackageFlightUtils.getStylizedFlightDurationString(context, selectedFlight, R.color.packages_total_duration_text))

            var perPersonPrice = Phrase.from(context.resources.getString(R.string.package_flight_overview_per_person_TEMPLATE))
                    .put("money", selectedFlight.packageOfferModel.price.pricePerPersonFormatted)
                    .format().toString()
            bundlePriceSubject.onNext(perPersonPrice)
            baggageFeeURLSubject.onNext(selectedFlight.baggageFeesUrl)
        }
    }

    fun updateUrgencyMessage(selectedFlight: FlightLeg) {
        var urgencyMessage = StringBuilder()
        if (selectedFlight.packageOfferModel.urgencyMessage != null) {
            val ticketsLeft = selectedFlight.packageOfferModel.urgencyMessage.ticketsLeft
            if (ticketsLeft > 0 && ticketsLeft < 5) {
                urgencyMessage.append(Phrase.from(context.resources
                        .getQuantityString(R.plurals.package_flight_overview_urgency_message_TEMPLATE, ticketsLeft.toInt()))
                        .put("seats", ticketsLeft)
                        .format().toString())
            }
        }
        if (urgencyMessage.isNotBlank()) {
            urgencyMessage.append(", ")
        }
        val pricePerPerson = pricePerPersonString(selectedFlight)
        var pricePerPersonMessage = if (numberOfTravelers.value == 1) pricePerPerson
        else Phrase.from(context.resources.getString(R.string.flight_details_price_per_person_TEMPLATE))
                .put("price", pricePerPerson)
                .format().toString()
        if (selectedFlight.packageOfferModel.price.deltaPositive) {
            urgencyMessage.append("+" + pricePerPersonMessage)
        } else {
            urgencyMessage.append(pricePerPersonMessage)
        }
        urgencyMessagingSubject.onNext(urgencyMessage.toString())
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        selectedFlightClickedSubject.onNext(selectedFlightLegSubject.value)
    }

    abstract fun pricePerPersonString(selectedFlight: FlightLeg): String
}