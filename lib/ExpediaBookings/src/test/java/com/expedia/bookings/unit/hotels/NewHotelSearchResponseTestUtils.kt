package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.AmenityFilters
import com.expedia.bookings.data.hotels.ErrorInfo
import com.expedia.bookings.data.hotels.HotelInfo
import com.expedia.bookings.data.hotels.HotelPriceInfo
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.data.hotels.NewHotelSearchResponse
import com.expedia.bookings.data.hotels.PageSummaryData
import com.expedia.bookings.data.hotels.PriceOption
import com.expedia.bookings.data.hotels.PriceScheme
import com.expedia.bookings.data.hotels.PriceType

class NewHotelSearchResponseTestUtils {

    companion object {
        fun createHotelInfo(index: Int = 0): HotelInfo {
            return HotelInfo().apply {
                id = "id$index"
                localizedHotelName = "localizedHotelName$index"
                isPinned = false
                isSponsored = false
                hasFreeCancel = false
                hasPayLater = false
                roomsLeftAtThisPrice = 0
                starRating = 4.3f
                guestRating = 3.7f
                price = createHotelPriceInfo()
                vip = false
                imageUrl = "imageUrl$index"
                lowResImageUrl = "lowResImageUrl$index"
                latLong = listOf(41.882199, -87.640492)
                regionName = "regionName$index"
                regionId = "regionId$index"
                directDistance = HotelInfo.ProximityDistance(1.0, HotelInfo.DistanceUnit.Miles)
                neighborhoodId = "neighborhoodId$index"
                neighborhoodName = "neighborhoodName$index"
                impressionTrackingUrl = "impressionTrackingUrl$index"
                clickTrackingUrl = "clickTrackingUrl$index"
                isAvailable = true
            }
        }

        fun createHotelPriceInfo(displayPrice: Float? = 100f,
                                 strikeThroughPrice: Float? = 200f,
                                 discountPercentage: Float? = 50f,
                                 currencySymbol: String? = "$",
                                 currencyCode: String? = "USD",
                                 pricingScheme: PriceScheme? = PriceScheme(PriceType.PER_NIGHT, false, false)): HotelPriceInfo {
            return HotelPriceInfo(
                    displayPrice = displayPrice,
                    strikeThroughPrice = strikeThroughPrice,
                    discountPercentage = discountPercentage,
                    currencySymbol = currencySymbol,
                    currencyCode = currencyCode,
                    pricingScheme = pricingScheme)
        }

        fun createErrorInfo(index: Int = 0): ErrorInfo {
            return ErrorInfo().apply {
                message = "message$index"
                localizedMessage = "localizedMessage$index"
                errors = listOf("errors1", "errors2")
            }
        }

        fun createPageSummaryData(): PageSummaryData {
            return PageSummaryData().apply {
                regionName = "regionName"
                regionId = "regionId"
                cityName = "cityName"
                pageViewBeaconPixelUrl = "pageViewBeaconPixelUrl"
                pricingScheme = PriceScheme(PriceType.PER_NIGHT, false, false)
                priceFilters = PriceOption().apply {
                    minPrice = 10
                    maxPrice = 100
                }
                amenityFilters = createAmenityFilters()
                neighborhoodFilters = createNeighborhoodFilters()
            }
        }

        fun createNewHotelSearchResponse(): NewHotelSearchResponse {
            return NewHotelSearchResponse().apply {
                errors = listOf(createErrorInfo(0), createErrorInfo(1))
                pageSummaryData = createPageSummaryData()
                hotels = listOf(createHotelInfo(0),
                        createHotelInfo(1),
                        createHotelInfo(2),
                        createHotelInfo(3),
                        createHotelInfo(4))
                source = "lsss"
            }
        }

        private fun createAmenityFilters(): AmenityFilters {
            val amenityFilters = AmenityFilters()
            amenityFilters.amenityOptionList = listOf(
                    AmenityFilters.Amenity("pool", "pool"),
                    AmenityFilters.Amenity("childPool", "childPool"),
                    AmenityFilters.Amenity("freeParking", "freeParking"),
                    AmenityFilters.Amenity("freeBreakfast", "freeBreakfast"),
                    AmenityFilters.Amenity("petsAllowed", "petsAllowed"),
                    AmenityFilters.Amenity("random", "random")
            )
            amenityFilters.accessibilityOptionList = listOf(
                    AmenityFilters.AccessibilityOption("highSpeedInternet", "highSpeedInternet"),
                    AmenityFilters.AccessibilityOption("airConditioning", "airConditioning"),
                    AmenityFilters.AccessibilityOption("allInclusive", "allInclusive"),
                    AmenityFilters.AccessibilityOption("freeAirportTransport", "freeAirportTransport"),
                    AmenityFilters.AccessibilityOption("random", "random")
            )
            return amenityFilters
        }

        private fun createNeighborhoodFilters(): List<Neighborhood> {
            return List(4, { i ->
                Neighborhood().apply {
                    name = "neighborhoodName$i"
                    id = "neighborhoodId$i"
                }
            })
        }
    }
}
