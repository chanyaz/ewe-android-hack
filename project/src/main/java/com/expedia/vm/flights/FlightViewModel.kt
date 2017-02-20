package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightViewModel

class FlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        return flightLeg.packageOfferModel.price.averageTotalPricePerTicket.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL)
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
}
