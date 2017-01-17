package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.vm.AbstractFlightOverviewViewModel
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    override val showBundlePriceSubject = BehaviorSubject.create(false)
    override val showSeatClassAndBookingCode = BehaviorSubject.create(true)

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.averageTotalPricePerTicket.formattedMoneyFromAmountAndCurrencyCode
    }

    override fun showFlightDistance(selectedFlight: FlightLeg): Boolean {
        return !selectedFlight.totalTravelDistance.isNullOrEmpty() && Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)
    }
}
