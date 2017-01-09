package com.expedia.bookings.data

import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct

abstract class FlightTripResponse : TripResponse() {
    open var newTrip: TripDetails? = null
    open lateinit var details: FlightTripDetails
    var totalPriceIncludingFees: Money? = null
    var selectedCardFees: Money? = null

    fun getSelectedInsuranceProduct() : InsuranceProduct? = getOffer().selectedInsuranceProduct

    fun getAvailableInsuranceProducts(): List<InsuranceProduct> = getOffer().availableInsuranceProducts

    abstract fun getOffer(): FlightTripDetails.FlightOffer

    override fun getOldPrice(): Money? {
        if (details.oldOffer == null) {
            return null
        }
        return details.oldOffer.totalPriceWithInsurance ?: details.oldOffer.totalPrice
    }

}
