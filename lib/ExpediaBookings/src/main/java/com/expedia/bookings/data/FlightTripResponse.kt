package com.expedia.bookings.data

import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct

abstract class FlightTripResponse : TripResponse() {
    open var newTrip: TripDetails? = null
    open lateinit var details: FlightTripDetails
    var totalPriceIncludingFees: Money? = null
    var selectedCardFees: Money? = null
    var fareFamilyList: FareFamilies? = null
    val createTripStatus = CreateTripError.NULL
    var isFareFamilyUpgraded = false

    fun getSelectedInsuranceProduct(): InsuranceProduct? = getOffer().selectedInsuranceProduct

    fun getAvailableInsuranceProducts(): List<InsuranceProduct> = getOffer().availableInsuranceProducts

    abstract fun getOffer(): FlightTripDetails.FlightOffer

    override fun getOldPrice(): Money? {
        if (details.oldOffer == null) {
            return null
        }
        return details.oldOffer.totalPriceWithInsurance ?: details.oldOffer.totalPrice
    }

    data class FareFamilies (
        val productKey: String,
        val fareFamilyDetails: Array<FareFamilyDetails>
    )

    data class FareFamilyDetails (
        val fareFamilyName: String,
        val fareFamilyCode: String,
        val cabinClass: String,
        val totalPrice: Money,
        val deltaTotalPrice: Money,
        val deltaPositive: Boolean = false,
        val fareFamilyComponents: HashMap<String, HashMap<String, String>>
    )

    enum class CreateTripError {
        FARE_FAMILY_PRICE_CHANGE,
        SUCCESS,
        PRICE_CHANGE,
        FARE_FAMILY_UNAVAILABLE,
        NULL
    }
}
