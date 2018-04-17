package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.PageSummaryData
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PageSummaryDataTest {

    private lateinit var pageSummaryData: PageSummaryData
    private lateinit var legacyResponse: HotelSearchResponse

    @Before
    fun before() {
        pageSummaryData = NewHotelSearchResponseTestUtils.createPageSummaryData()
        legacyResponse = HotelSearchResponse()
    }

    @Test
    fun testPopulateLegacySearchResponseCityName() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals("cityName", legacyResponse.searchRegionCity)
    }

    @Test
    fun testPopulateLegacySearchResponseRegionName() {
        pageSummaryData.cityName = ""
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals("regionName", legacyResponse.searchRegionCity)
    }

    @Test
    fun testPopulateLegacySearchResponseRegionId() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals("regionId", legacyResponse.searchRegionId)
    }

    @Test
    fun testPopulateLegacySearchResponsePageViewBeaconPixelUrl() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals("pageViewBeaconPixelUrl", legacyResponse.pageViewBeaconPixelUrl)
    }

    @Test
    fun testPopulateLegacySearchResponsePricingScheme() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, legacyResponse.userPriceType)
    }

    @Test
    fun testPopulateLegacySearchResponseNullPricingScheme() {
        pageSummaryData.pricingScheme = null
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(HotelRate.UserPriceType.UNKNOWN, legacyResponse.userPriceType)
    }

    @Test
    fun testPopulateLegacySearchResponsePriceFilters() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(2, legacyResponse.priceOptions.size)
        assertEquals(0, legacyResponse.priceOptions[0].minPrice)
        assertEquals(0, legacyResponse.priceOptions[0].maxPrice)
        assertEquals(100, legacyResponse.priceOptions[1].minPrice)
        assertEquals(100, legacyResponse.priceOptions[1].maxPrice)
    }

    @Test
    fun testPopulateLegacySearchResponsePriceFiltersDontHandleZeroMaxPrice() {
        pageSummaryData.priceFilters?.minPrice = 0
        pageSummaryData.priceFilters?.maxPrice = 0
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertTrue(legacyResponse.priceOptions.isEmpty())
    }

    @Test
    fun testPopulateLegacySearchResponsePriceFiltersFixMinGreaterThanMax() {
        pageSummaryData.priceFilters?.minPrice = 100
        pageSummaryData.priceFilters?.maxPrice = 10
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(2, legacyResponse.priceOptions.size)
        assertEquals(0, legacyResponse.priceOptions[0].minPrice)
        assertEquals(0, legacyResponse.priceOptions[0].maxPrice)
        assertEquals(100, legacyResponse.priceOptions[1].minPrice)
        assertEquals(100, legacyResponse.priceOptions[1].maxPrice)
    }

    @Test
    fun testPopulateLegacySearchResponseNullPriceFilters() {
        pageSummaryData.priceFilters = null
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertTrue(legacyResponse.priceOptions.isEmpty())
    }

    @Test
    fun testPopulateLegacySearchResponseAmenityFilters() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(8, legacyResponse.amenityFilterOptions.size)
        legacyResponse.amenityFilterOptions.containsKey("7")
        legacyResponse.amenityFilterOptions.containsKey("14")
        legacyResponse.amenityFilterOptions.containsKey("16")
        legacyResponse.amenityFilterOptions.containsKey("17")
        legacyResponse.amenityFilterOptions.containsKey("19")
        legacyResponse.amenityFilterOptions.containsKey("27")
        legacyResponse.amenityFilterOptions.containsKey("30")
        legacyResponse.amenityFilterOptions.containsKey("66")
    }

    @Test
    fun testPopulateLegacySearchResponseNullAmenityFilters() {
        pageSummaryData.amenityFilters = null
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertTrue(legacyResponse.amenityFilterOptions.isEmpty())
    }

    @Test
    fun testPopulateLegacySearchResponseNeighborhoodFilters() {
        pageSummaryData.populateLegacySearchResponse(legacyResponse)
        assertEquals(pageSummaryData.neighborhoodFilters, legacyResponse.allNeighborhoodsInSearchRegion)

        assertEquals(pageSummaryData.neighborhoodFilters.size, legacyResponse.neighborhoodsMap.size)
        pageSummaryData.neighborhoodFilters.forEach { neighborhood ->
            assertEquals(neighborhood, legacyResponse.neighborhoodsMap[neighborhood.id])
        }
    }
}
