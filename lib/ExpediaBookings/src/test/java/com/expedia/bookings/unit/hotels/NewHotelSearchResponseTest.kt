package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelInfo
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.NewHotelSearchResponse
import com.expedia.bookings.data.hotels.PriceScheme
import com.expedia.bookings.data.hotels.PriceType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewHotelSearchResponseTest {

    private lateinit var hotelResponse: NewHotelSearchResponse

    @Before
    fun before() {
        hotelResponse = NewHotelSearchResponseTestUtils.createNewHotelSearchResponse()
    }

    @Test
    fun testConvertToLegacySearchResponseErrors() {
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertEquals(2, legacyResponse.errors.size)
    }

    @Test
    fun testConvertToLegacySearchResponseNoErrors() {
        hotelResponse.errors = emptyList()
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertNull(legacyResponse.errors)
    }

    @Test
    fun testConvertToLegacySearchResponseHotelsIndex() {
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertEquals(5, legacyResponse.hotelList.size)
        legacyResponse.hotelList.forEachIndexed { i, hotel ->
            assertEquals(i, hotel.sortIndex)
        }
    }

    @Test
    fun testConvertToLegacySearchResponsePageSummaryDataCityName() {
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        legacyResponse.hotelList.forEach { hotel ->
            assertEquals("cityName", hotel.city)
        }
    }

    @Test
    fun testConvertToLegacySearchResponseNullPageSummaryDataCityName() {
        hotelResponse.pageSummaryData = null
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        legacyResponse.hotelList.forEach { hotel ->
            assertNull(hotel.city)
        }
    }

    @Test
    fun testConvertToLegacySearchResponseFallBackPageDataUserPriceTypeToFirstHotel() {
        hotelResponse.pageSummaryData?.pricingScheme = null
        hotelResponse.hotels.first().price = NewHotelSearchResponseTestUtils.createHotelPriceInfo(pricingScheme = PriceScheme(PriceType.TOTAL, true, true))
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertEquals(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, legacyResponse.userPriceType)
    }

    @Test
    fun testConvertToLegacySearchResponseNeighborhoodMap() {
        val hotels = ArrayList(hotelResponse.hotels)
        hotels.add(HotelInfo())
        hotelResponse.hotels = hotels
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertEquals(4, legacyResponse.neighborhoodsMap.size)
        legacyResponse.hotelList.forEach { hotel ->
            if (!hotel.locationId.isNullOrBlank() && legacyResponse.neighborhoodsMap.containsKey(hotel.locationId)) {
                assertEquals(1, legacyResponse.neighborhoodsMap[hotel.locationId]?.hotels?.size)
                assertEquals(hotel, legacyResponse.neighborhoodsMap[hotel.locationId]?.hotels?.first())
                assertEquals(1, legacyResponse.neighborhoodsMap[hotel.locationId]?.score)
            }
        }
    }

    @Test
    fun testConvertToLegacySearchResponseNotIsPinned() {
        val hotels = ArrayList(hotelResponse.hotels)
        hotelResponse.hotels = hotels
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertFalse(legacyResponse.isPinnedSearch)
        assertFalse(legacyResponse.hasPinnedHotel())
    }

    @Test
    fun testConvertToLegacySearchResponseIsPinned() {
        val hotels = ArrayList(hotelResponse.hotels)
        hotels.add(HotelInfo().apply { isPinned = true })
        hotelResponse.hotels = hotels
        val legacyResponse = hotelResponse.convertToLegacySearchResponse()
        assertTrue(legacyResponse.isPinnedSearch)
        assertTrue(legacyResponse.hasPinnedHotel())
    }
}
