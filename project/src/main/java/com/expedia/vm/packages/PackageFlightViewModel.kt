package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Strings
import com.expedia.vm.AbstractFlightViewModel

class PackageFlightViewModel(context: Context, flightLeg: FlightLeg) : AbstractFlightViewModel(context, flightLeg) {
    override fun price(): String {
        if (flightLeg.packageOfferModel.price.deltaPositive) {
            return ("+" + flightLeg.packageOfferModel.price.differentialPriceFormatted)
        } else {
            return flightLeg.packageOfferModel.price.differentialPriceFormatted
        }
    }

    override fun getUrgencyMessageVisibility(seatsLeft : String): Boolean {
        return false
    }

    override fun getFlightCabinPreferenceVisibility(): Boolean {
        return false
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForPackages
    }

    override fun getFlightDetailCardContDescriptionStringID(): Int {
        return R.string.flight_detail_card_cont_desc_with_price_diff_TEMPLATE
    }
}
