package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightViewModel

open class FlightViewModel(context: Context, flightLeg: FlightLeg, val isOutboundSearch: Boolean = true) : AbstractFlightViewModel(context, flightLeg) {
    val showDeltaPricing = AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsDeltaPricing)
    override fun price(): String {
        val priceToShow = StringBuilder()
        var price = flightLeg.packageOfferModel.price.averageTotalPricePerTicket
        if(!isOutboundSearch && showDeltaPricing) {
            if(flightLeg.packageOfferModel.price.deltaPositive) {
                priceToShow.append("+ ")
            }
            price = flightLeg.packageOfferModel.price.deltaPrice
        }
        return priceToShow.append(Money.getFormattedMoneyFromAmountAndCurrencyCode(price.roundedAmount, price.currencyCode, Money.F_NO_DECIMAL)).toString()
    }

    override fun getUrgencyMessageVisibility(seatsLeft : String): Boolean {
        return Strings.isNotEmpty(seatsLeft)
    }

    override fun getFlightCabinPreferenceVisibility(): Boolean {
        return Strings.isNotEmpty(updateflightCabinPreferenceObservable.value)
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights
    }

    override fun getFlightDetailCardContDescriptionStringID(): Int {
        return R.string.flight_detail_card_cont_desc_without_price_diff_TEMPLATE
    }
}

