package com.expedia.bookings.widget.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.flights.FlightViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class FlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, val isRoundTripSearchSubject: BehaviorSubject<Boolean>,
                             val isOutboundSearch: Boolean, val flightCabinClass: String?) : AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearchSubject) {

    override fun adjustPosition(): Int {
        isCrossSellPackageOnFSR = showCrossSellPackageBannerCell()
        val allHeadersCount = (if (showAllFlightsHeader()) 2 else 1)
        return (if (isCrossSellPackageOnFSR) (allHeadersCount + 1) else allHeadersCount)
    }

    override fun shouldAdjustPricingMessagingForAirlinePaymentMethodFee(): Boolean {
        return PointOfSale.getPointOfSale().shouldAdjustPricingMessagingForAirlinePaymentMethodFee()
    }

    override fun showAllFlightsHeader(): Boolean {
        return false
    }

    override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): FlightViewModel {
        return FlightViewModel(context, flightLeg)
    }

    private fun shouldShowCrossSellPackageBanner() = (PointOfSale.getPointOfSale().isCrossSellPackageOnFSR() &&
            FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR, R.string.preference_cross_sell_package_on_fsr))

    private fun showCrossSellPackageBannerCell(): Boolean {
        return (shouldShowCrossSellPackageBanner() && isRoundTripSearchSubject.value && isOutboundSearch &&
                (flightCabinClass == null || flightCabinClass == FlightServiceClassType.CabinCode.COACH.name))
    }
}
