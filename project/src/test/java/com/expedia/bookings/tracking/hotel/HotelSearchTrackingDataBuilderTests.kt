package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelSearchTrackingDataBuilderTests {

    lateinit var trackingDataBuilder: HotelSearchTrackingDataBuilder

    @Before
    fun setup() {
        trackingDataBuilder = HotelSearchTrackingDataBuilder()
    }

    @Test
    fun testHappyPathBuild() {
        val params = createSearchParams()
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertEquals("178248", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("6", trackingData.numberOfResults)
        assertTrue(trackingData.hasSponsoredListingPresent)
        assertEquals("639.51", trackingData.lowestHotelTotalPrice)
        assertEquals(6, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    @Test
    fun testOneSoldOutResponseBuild() {
        val params = createSearchParams(adults = 2, shopWithPoints = true)
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_one_sold_out_response.json")
        response.hotelList.first().isSoldOut = true
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertEquals("800024", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(4, trackingData.numberOfGuests)
        assertEquals(2, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("1", trackingData.numberOfResults)
        assertFalse(trackingData.hasSponsoredListingPresent)
        assertEquals("929.38", trackingData.lowestHotelTotalPrice)
        assertEquals(1, trackingData.hotels.count())

        assertTrue(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertTrue(trackingData.hasSoldOutHotel)
    }

    @Test
    fun testEmptyResponseBuild() {
        val params = createSearchParams(checkIn = LocalDate().plusDays(2), checkOut = LocalDate().plusDays(5))
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_no_response.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertNull(trackingData.city)
        assertNull(trackingData.stateProvinceCode)
        assertNull(trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertNull(trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(2), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(5), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("2", trackingData.searchWindowDays)
        assertEquals(3, trackingData.duration)

        assertFalse(trackingData.resultsReturned)
        assertNull(trackingData.numberOfResults)
        assertFalse(trackingData.hasSponsoredListingPresent)
        assertNull(trackingData.lowestHotelTotalPrice)
        assertEquals(0, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    @Test
    fun testPinnedBuild() {
        val params = createSearchParams()
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_pinned.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertEquals("178248", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("6", trackingData.numberOfResults)
        assertTrue(trackingData.hasSponsoredListingPresent)
        assertEquals("639.51", trackingData.lowestHotelTotalPrice)
        assertEquals(6, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertTrue(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    @Test
    fun testNoPinnedBuild() {
        val params = createSearchParams()
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_no_pinned.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertEquals("178248", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("6", trackingData.numberOfResults)
        assertTrue(trackingData.hasSponsoredListingPresent)
        assertEquals("639.51", trackingData.lowestHotelTotalPrice)
        assertEquals(6, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    @Test
    fun testCurrentLocationBuild() {
        val suggestion = createSuggestion(gaiaId = null)
        val params = createSearchParams(suggestion = suggestion)
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("Current Location", trackingData.region)
        assertEquals("fullName", trackingData.freeFormRegion)
        assertEquals("178248", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("6", trackingData.numberOfResults)
        assertTrue(trackingData.hasSponsoredListingPresent)
        assertEquals("639.51", trackingData.lowestHotelTotalPrice)
        assertEquals(6, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    @Test
    fun tesNoFullNameBuild() {
        val suggestion = createSuggestion(fullName = null)
        val params = createSearchParams(suggestion = suggestion)
        val response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")
        trackingDataBuilder.searchParams(params)
        trackingDataBuilder.searchResponse(response)

        val trackingData = trackingDataBuilder.build()

        assertEquals("Chicago", trackingData.city)
        assertEquals("IL", trackingData.stateProvinceCode)
        assertEquals("USA", trackingData.countryCode)

        assertEquals("gaiaId", trackingData.region)
        assertNull(trackingData.freeFormRegion)
        assertEquals("178248", trackingData.searchRegionId)

        assertEquals(LocalDate().plusDays(1), trackingData.checkInDate)
        assertEquals(LocalDate().plusDays(2), trackingData.checkoutDate)

        assertEquals(3, trackingData.numberOfGuests)
        assertEquals(1, trackingData.numberOfAdults)
        assertEquals(2, trackingData.numberOfChildren)

        assertEquals("1", trackingData.searchWindowDays)
        assertEquals(1, trackingData.duration)

        assertTrue(trackingData.resultsReturned)
        assertEquals("6", trackingData.numberOfResults)
        assertTrue(trackingData.hasSponsoredListingPresent)
        assertEquals("639.51", trackingData.lowestHotelTotalPrice)
        assertEquals(6, trackingData.hotels.count())

        assertFalse(trackingData.swpEnabled)

        assertFalse(trackingData.hasPinnedHotel)
        assertFalse(trackingData.pinnedHotelSoldOut)

        assertFalse(trackingData.hasSoldOutHotel)
    }

    private fun createSuggestion(gaiaId: String? = "gaiaId",
                                 displayName: String? = "displayName", fullName: String? = "fullName", shortName: String? = "shortName",
                                 lat: Double =  41.8781, lng: Double =  -87.6298): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = displayName
        suggestion.regionNames.fullName = fullName
        suggestion.regionNames.shortName = shortName
        suggestion.coordinates = SuggestionV4.LatLng()
        suggestion.coordinates.lat = lat
        suggestion.coordinates.lng =lng

        return suggestion
    }

    private fun createSearchParams(suggestion: SuggestionV4 = createSuggestion(),
                                   checkIn: LocalDate = LocalDate().plusDays(1), checkOut: LocalDate = LocalDate().plusDays(2),
                                   adults: Int = 1, children: List<Int> = listOf(4, 5),
                                   shopWithPoints: Boolean = false, filterUnavailable: Boolean = true): HotelSearchParams {
        val params = HotelSearchParams(suggestion,
                checkIn, checkOut,
                adults, children,
                shopWithPoints)

        return params
    }

    private fun getHotelSearchResponse(filePath: String) : HotelSearchResponse {
        val resourceReader = JSONResourceReader(filePath)
        val searchResponse = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return searchResponse
    }
}
