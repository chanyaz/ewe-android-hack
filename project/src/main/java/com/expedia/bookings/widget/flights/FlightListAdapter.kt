package com.expedia.bookings.widget.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.flights.FlightViewModel
import rx.subjects.PublishSubject

open class FlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, flightSearchViewModel: FlightSearchViewModel) : AbstractFlightListAdapter(context, flightSelectedSubject, flightSearchViewModel) {

    override fun adjustPosition(): Int {
        return if (showAllFlightsHeader()) 2 else 1
    }

    override fun isAirlinesChargePaymentMethodFee(): Boolean {
        return PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()
    }

    override fun showAllFlightsHeader(): Boolean {
        return false
    }

    override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): FlightViewModel {
        return FlightViewModel(context, flightLeg)
    }
}
