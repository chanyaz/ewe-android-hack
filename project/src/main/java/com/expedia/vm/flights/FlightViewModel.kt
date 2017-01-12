package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.vm.AbstractFlightViewModel
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings

class FlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        return flightLeg.packageOfferModel.price.averageTotalPricePerTicket.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL)
    }

    override fun getUrgencyMessageVisibilty(): Boolean {
        return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightUrgencyMessage,
                R.string.preference_enable_urgency_messaging_on_flights)
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights
    }
}
