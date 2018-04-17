package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.ApiError
import com.google.gson.annotations.SerializedName

class NewHotelSearchResponse {
    var errors: List<ErrorInfo> = emptyList()
    @SerializedName("pageData")
    var pageSummaryData: PageSummaryData? = null
    var hotels: List<HotelInfo> = emptyList()
    var source: String = ""

    fun convertToLegacySearchResponse(): HotelSearchResponse {
        val legacyResponse = HotelSearchResponse()

        if (errors.isNotEmpty()) {
            legacyResponse.errors = ArrayList<ApiError>()
            errors.forEach { error ->
                legacyResponse.errors.add(error.convertToApiError())
            }
        }

        pageSummaryData?.populateLegacySearchResponse(legacyResponse)

        populateHotelData(legacyResponse)
        if (legacyResponse.userPriceType == HotelRate.UserPriceType.UNKNOWN && legacyResponse.hotelList.first()?.lowRateInfo?.getUserPriceType() != null) {
            legacyResponse.userPriceType = legacyResponse.hotelList.first()?.lowRateInfo?.getUserPriceType()
        }

        legacyResponse.allNeighborhoodsInSearchRegion.forEach { neighborhood ->
            neighborhood.score = neighborhood.hotels.count()
        }

        return legacyResponse
    }

    private fun populateHotelData(legacyResponse: HotelSearchResponse) {
        var hasPinnedHotel: Boolean? = null
        hotels.forEachIndexed { i, hotel ->
            val legacyHotel = hotel.convertToLegacyHotel()
            legacyHotel.sortIndex = i
            pageSummaryData?.let { pageSummaryData ->
                legacyHotel.city = pageSummaryData.cityName
            }

            legacyResponse.hotelList.add(legacyHotel)
            if (!legacyHotel.locationId.isNullOrBlank() && legacyResponse.neighborhoodsMap.containsKey(legacyHotel.locationId)) {
                legacyResponse.neighborhoodsMap[legacyHotel.locationId]?.hotels?.add(legacyHotel)
            }
            if (hotel.isPinned) {
                hasPinnedHotel = true
            }
        }

        legacyResponse.setHasPinnedHotel(hasPinnedHotel)
    }
}
