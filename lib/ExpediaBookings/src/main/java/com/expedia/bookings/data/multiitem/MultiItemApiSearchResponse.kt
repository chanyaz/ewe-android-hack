package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel
import java.util.ArrayList

data class MultiItemApiSearchResponse(
        val offers: List<MultiItemOffer>,
        val hotels: Map<String, HotelOffer>,
        val flights: Map<String, FlightOffer>,
        val flightLegs: Map<String, MultiItemFlightLeg>,
//        val cars: Map<String, CarOffer>?,
        val errors: List<MultiItemError>?
//        val messageInfo: MessageInfo?,
) : BundleSearchResponse {

    private lateinit var sortedHotels: List<Hotel>
    private lateinit var sortedFlights: List<FlightLeg>
    private var currentSelectedOffer: PackageOfferModel? = null

    fun setup(): BundleSearchResponse {
        sortedHotels = ArrayList()
        sortedFlights = ArrayList()

        offers.map { offer ->
            (offer.packagedOffers + offer.searchedOffer).map { (productType, productKey) ->
                when (productType) {
                    ProductType.Air -> {
                        //TODO
                    }
                    ProductType.Hotel -> {
                        sortedHotels += Hotel.convertMultiItemHotel(hotels[productKey], offer)
                    }
                    else -> {
                    }
                }
            }

        }
        return this
    }

    override fun getHotelCheckInDate(): String {
        return hotels.values.elementAt(0).checkInDate
    }

    override fun getHotelCheckOutDate(): String {
        return hotels.values.elementAt(0).checkOutDate
    }

    override fun getHotelResultsCount(): Int {
        return getHotels().size
    }

    override fun getHotels(): List<Hotel> {
        return sortedHotels
    }

    override fun getFlightLegs(): List<FlightLeg> {
        return sortedFlights
    }

    override fun hasSponsoredHotelListing(): Boolean {
        return false
    }

    override fun getCurrencyCode(): String {
        return offers[0].price.totalPrice.currency
    }

    override fun getCurrentOfferModel(): PackageOfferModel {
        return currentSelectedOffer!!
    }

    override fun setCurrentOfferModel(offerModel: PackageOfferModel) {
        currentSelectedOffer = offerModel
    }

    override fun hasErrors(): Boolean {
        return false
    }

    override val firstError: PackageApiError.Code
        get() {
            throw RuntimeException("No errors to get!")
        }
}