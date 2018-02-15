package com.expedia.vm

import android.content.Context
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class AbstractFlightOverviewViewModel(val context: Context) {
    val selectedFlightLegSubject = BehaviorSubject.create<FlightLeg>()
    val bundlePriceSubject = BehaviorSubject.create<String>()
    val urgencyMessagingSubject = BehaviorSubject.create<String>()
    val showBasicEconomyTooltip = PublishSubject.create<Boolean>()
    val basicEconomyMessagingToolTipInfo = PublishSubject.create<List<FlightLeg.BasicEconomyTooltipInfo>>()
    val totalDurationSubject = BehaviorSubject.create<CharSequence>()
    val totalDurationContDescSubject = BehaviorSubject.create<String>()
    val baggageFeeURLSubject = BehaviorSubject.create<String>()
    val selectedFlightClickedSubject = BehaviorSubject.create<FlightLeg>()
    val chargesObFeesTextSubject = PublishSubject.create<String>()
    val airlineFeesWarningTextSubject = PublishSubject.create<String>()
    var numberOfTravelers = BehaviorSubject.createDefault<Int>(0)
    val obFeeDetailsUrlObservable = PublishSubject.create<String>()
    val e3EndpointUrl = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl
    val earnMessage = BehaviorSubject.create<String>()

    abstract val showBundlePriceSubject: BehaviorSubject<Boolean>
    abstract val showEarnMessage: BehaviorSubject<Boolean>

    abstract fun shouldShowSeatingClassAndBookingCode(): Boolean
    abstract fun convertTooltipInfo(selectedFlight: FlightLeg): List<FlightLeg.BasicEconomyTooltipInfo>
    abstract fun shouldShowDeltaPositive(): Boolean
    abstract fun pricePerPersonString(selectedFlight: FlightLeg): String
    abstract fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
            updateUrgencyMessage(selectedFlight)

            totalDurationSubject.onNext(FlightV2Utils.getStylizedFlightDurationString(context, selectedFlight, R.color.packages_total_duration_text))
            totalDurationContDescSubject.onNext(FlightV2Utils.getFlightLegDurationContentDescription(context, selectedFlight))

            val perPersonPrice = Phrase.from(context.resources.getString(R.string.package_flight_overview_per_person_TEMPLATE))
                    .put("money", selectedFlight.packageOfferModel.price.pricePerPersonFormatted)
                    .format().toString()
            bundlePriceSubject.onNext(perPersonPrice)
            baggageFeeURLSubject.onNext(selectedFlight.baggageFeesUrl)
            showBasicEconomyTooltip.onNext(shouldShowBasicEconomyMessage(selectedFlight))
        }

        ObservableOld.zip(showBasicEconomyTooltip, selectedFlightLegSubject, { showBasicEconomyTooltip, selectedFlightLeg ->
            if (showBasicEconomyTooltip) {
                basicEconomyMessagingToolTipInfo.onNext(convertTooltipInfo(selectedFlightLeg))
            }
        }).subscribe()
    }

    fun updateUrgencyMessage(selectedFlight: FlightLeg) {
        val urgencyMessage = StringBuilder()
        if (selectedFlight.packageOfferModel.urgencyMessage != null) {
            val ticketsLeft = selectedFlight.packageOfferModel.urgencyMessage.ticketsLeft
            if (ticketsLeft in 1..5) {
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
        val pricePerPersonMessage = if (numberOfTravelers.value == 1) {
            pricePerPerson
        } else {
            Phrase.from(context.resources.getString(R.string.flight_details_price_per_person_TEMPLATE))
                    .put("price", pricePerPerson)
                    .format().toString()
        }
        if (selectedFlight.packageOfferModel.price.deltaPositive && shouldShowDeltaPositive()) {
            urgencyMessage.append("+" + pricePerPersonMessage)
        } else {
            urgencyMessage.append(pricePerPersonMessage)
        }
        urgencyMessagingSubject.onNext(urgencyMessage.toString())
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        selectedFlightClickedSubject.onNext(selectedFlightLegSubject.value)
    }
}
