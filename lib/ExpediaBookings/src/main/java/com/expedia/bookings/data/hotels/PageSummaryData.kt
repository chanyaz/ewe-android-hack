package com.expedia.bookings.data.hotels

class PageSummaryData {
    var availableHotelCount: Int = 0
    var totalHotelCount: Int = 0
    var regionName: String = ""
    var regionId: String = ""
    var guests: Int = 0
    var cityName: String = ""
    var countryName: String = ""
    var pricingScheme: PriceScheme? = null
    var priceFilters: PriceOption? = null
    var amenityFilters: AmenityFilters? = null
    var neighborhoodFilters: List<Neighborhood> = emptyList()

    data class PriceScheme(val priceType: PriceType, val taxIncluded: Boolean, val feeIncluded: Boolean)

    enum class PriceType {
        TOTAL,
        PER_NIGHT
    }
}
