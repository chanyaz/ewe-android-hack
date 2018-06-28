package com.expedia.bookings.flights.vm

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.getEarnMessage
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.isFlightsUrgencyMeassagingEnabled
import com.expedia.bookings.utils.isRichContentShowAmenityEnabled
import com.expedia.bookings.utils.isRichContentShowRouteScoreEnabled
import com.expedia.vm.AbstractFlightOverviewViewModel
import io.reactivex.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.createDefault<Boolean>(false)
    override val showEarnMessage = BehaviorSubject.create<Boolean>()

    override fun shouldShowSeatingClassAndBookingCode(): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
    }

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.averageTotalPricePerTicket.formattedMoneyFromAmountAndCurrencyCode
    }

    override fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean {
        return selectedFlight.isBasicEconomy
    }

    override fun shouldShowUrgencyMessaging(): Boolean {
        return isFlightsUrgencyMeassagingEnabled(context)
    }

    override fun shouldShowDeltaPositive(): Boolean {
        return false
    }

    //no conversion needed for flights
    override fun convertTooltipInfo(selectedFlight: FlightLeg): List<FlightLeg.BasicEconomyTooltipInfo> {
        return selectedFlight.basicEconomyTooltipInfo
    }

    override fun shouldShowRichContentAmenity(): Boolean {
        return isRichContentShowAmenityEnabled()
    }

    override fun shouldShowRichContentRouteScore(): Boolean {
        return isRichContentShowRouteScoreEnabled()
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
            if (shouldShowUrgencyMessaging()) {
                bottomUrgencyMessageSubject.onNext(FlightV2Utils.getSeatsLeftUrgencyMessage(context, selectedFlight))
            }
        }
    }
}
