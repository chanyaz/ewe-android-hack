package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightViewModel
import java.math.BigDecimal
import java.util.Locale

class FlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        //TODO remove point of sale and special characters check and consume only rounded amount after api starts sending thsi field without any special characters
        val price = flightLeg.packageOfferModel.price.averageTotalPricePerTicket
        if (PointOfSale.getPointOfSale().getTwoLetterCountryCode().toUpperCase(Locale.US).contains(Regex("FR"))) {
            return Money.getFormattedMoneyFromAmountAndCurrencyCode(BigDecimal(StrUtils.removeSpecialCharactersFromRoundedAmount(price.roundedAmount)), price.currencyCode, Money.F_NO_DECIMAL)
        } else {
            return return price.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL)
        }
    }

    override fun getUrgencyMessageVisibility(seatsLeft : String): Boolean {
        return Strings.isNotEmpty(seatsLeft) && Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightUrgencyMessage)
    }

    override fun getFlightCabinPreferenceVisibility(): Boolean {
        return Strings.isNotEmpty(flightCabinPreferences) &&
                Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightPremiumClass)
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights
    }

    override fun getRoundTripMessageVisibilty(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppMaterialFlightSearchRoundTripMessage)
    }

    override fun isShowingFlightPriceDifference(): Boolean {
        return false
    }
}
