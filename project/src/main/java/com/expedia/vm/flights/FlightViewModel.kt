package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.vm.AbstractFlightViewModel
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.data.Db

class FlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        return flightLeg.packageOfferModel.price.averageTotalPricePerTicket.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL)
    }

    override fun getUrgencyMessageVisibilty(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightUrgencyMessage)
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights
    }

    override fun getRoundTripMessageVisibilty(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppMaterialFlightSearchRoundTripMessage)
    }
}
