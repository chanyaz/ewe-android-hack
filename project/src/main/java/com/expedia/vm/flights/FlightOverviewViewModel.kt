package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightOverviewViewModel
import io.reactivex.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.createDefault<Boolean>(false)
    override val showEarnMessage = BehaviorSubject.create<Boolean>()
    override val showSeatClassAndBookingCode = BehaviorSubject.createDefault<Boolean>(true)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.averageTotalPricePerTicket.formattedMoneyFromAmountAndCurrencyCode
    }

    override fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean {
        return selectedFlight.isBasicEconomy
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
