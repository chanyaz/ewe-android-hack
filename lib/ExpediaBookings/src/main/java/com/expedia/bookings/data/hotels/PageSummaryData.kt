package com.expedia.bookings.data.hotels

class PageSummaryData {
    var regionName: String = ""
    var regionId: String = ""
    var cityName: String = ""
    var pricingScheme: PriceScheme? = null
    var pageViewBeaconPixelUrl: String = ""
    var priceFilters: PriceOption? = null
    var amenityFilters: AmenityFilters? = null
    var neighborhoodFilters: List<Neighborhood> = emptyList()

    fun populateLegacySearchResponse(legacyResponse: HotelSearchResponse) {
        legacyResponse.searchRegionCity = if (cityName.isNotBlank()) cityName else regionName
        legacyResponse.searchRegionId = regionId
        legacyResponse.userPriceType = pricingScheme?.convertToUserPriceType() ?: HotelRate.UserPriceType.UNKNOWN
        legacyResponse.pageViewBeaconPixelUrl = pageViewBeaconPixelUrl
        populateLegacyPriceOptions(legacyResponse)
        populateLegacyAmenity(legacyResponse)
        legacyResponse.allNeighborhoodsInSearchRegion = neighborhoodFilters
        neighborhoodFilters.map { neighborhood ->
            legacyResponse.neighborhoodsMap[neighborhood.id] = neighborhood
        }
    }

    private fun populateLegacyPriceOptions(legacyResponse: HotelSearchResponse) {
        priceFilters?.let { priceFilters ->
            if (priceFilters.minPrice > priceFilters.maxPrice) {
                priceFilters.maxPrice = priceFilters.minPrice.also { priceFilters.minPrice = priceFilters.maxPrice }
            }
            if (priceFilters.maxPrice > 0) {
                val minPrice = PriceOption()
                minPrice.minPrice = 0
                minPrice.maxPrice = 0
                val maxPrice = PriceOption()
                maxPrice.minPrice = priceFilters.maxPrice
                maxPrice.maxPrice = priceFilters.maxPrice
                legacyResponse.priceOptions = listOf(minPrice, maxPrice)
            }
        }
    }

    private fun populateLegacyAmenity(legacyResponse: HotelSearchResponse) {
        amenityFilters?.let { amenityFilters ->
            val amenityMap = HashMap<String, HotelSearchResponse.AmenityOptions>()
            amenityFilters.amenityOptionList.forEach { amenity ->
                AmenityFilters.mapToLegacyId(amenity.id)?.let { amenityId ->
                    amenityMap[amenityId] = HotelSearchResponse.AmenityOptions()
                }
            }
            amenityFilters.accessibilityOptionList.forEach { amenity ->
                AmenityFilters.mapToLegacyId(amenity.id)?.let { amenityId ->
                    amenityMap[amenityId] = HotelSearchResponse.AmenityOptions()
                }
            }

            legacyResponse.amenityFilterOptions = amenityMap
        }
    }
}
