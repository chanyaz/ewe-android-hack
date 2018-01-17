package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightOverviewViewModel
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.create(false)
    override val showEarnMessage = BehaviorSubject.create<Boolean>()
    override val showSeatClassAndBookingCode = BehaviorSubject.create(true)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.averageTotalPricePerTicket.formattedMoneyFromAmountAndCurrencyCode
    }

    override fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean {
        return selectedFlight.isBasicEconomy
    }

    override fun shouldShowDeltaPositive(): Boolean {
        return false
    }

    private fun getBundleLabelText(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel?.loyaltyInfo?.earn?.getEarnMessage(context, true) ?: ""
    }

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
            earnMessage.onNext(getBundleLabelText(selectedFlight))
            showEarnMessage.onNext(
                    Strings.isNotEmpty(earnMessage.value) && (PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights)
            )
        }
    }
}
