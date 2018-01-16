package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateNotTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.testutils.JSONResourceReader
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class OmnitureTrackingHotelTest {

    private lateinit var context: Context
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var hotelOffersResponse: HotelOffersResponse
    private lateinit var pageLoadTimeData: PageUsableData

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        hotelOffersResponse = createHotelOffersResponse()
        pageLoadTimeData = createPageUsableData()
    }

    @Test
    fun testGetSearchResultsHotelProductStrings() {
        var hotels = ArrayList<Hotel>(0)
        val hotel0 = Hotel()
        hotel0.hotelId = "0"
        val hotel0LowRateInfo = HotelRate()
        hotel0LowRateInfo.airAttached = false
        hotel0LowRateInfo.strikethroughPriceToShowUsers = 100.0.toFloat()
        hotel0LowRateInfo.priceToShowUsers = 50.0.toFloat()
        hotel0.lowRateInfo = hotel0LowRateInfo
        hotel0.isSponsoredListing = false
        hotel0.isMemberDeal = false
        val hotel0ExpectedString = ";Hotel:0;;;;eVar39=1|eVar30=100-50"

        val hotel1 = Hotel()
        hotel1.hotelId = "1"
        val hotel1LowRateInfo = HotelRate()
        hotel1LowRateInfo.airAttached = true
        hotel1LowRateInfo.strikethroughPriceToShowUsers = 0.5.toFloat()
        hotel1LowRateInfo.priceToShowUsers = 2.49.toFloat()
        hotel1.lowRateInfo = hotel1LowRateInfo
        hotel1.isSponsoredListing = true
        hotel1.isMemberDeal = true
        val hotel1ExpectedString = ",;Hotel:1;;;;eVar39=2-MIP-AD-MOD|eVar30=1-2"

        val hotel2 = Hotel()
        hotel2.hotelId = "2"
        val hotel2ExpectedString = ""

        val hotel3 = Hotel()
        hotel3.hotelId = "3"
        val hotel3LowRateInfo = HotelRate()
        hotel3LowRateInfo.airAttached = true
        hotel3LowRateInfo.strikethroughPriceToShowUsers = 0.0.toFloat()
        hotel3LowRateInfo.priceToShowUsers = -1.5.toFloat()
        hotel3.lowRateInfo = hotel3LowRateInfo
        hotel3.isSponsoredListing = false
        hotel3.isMemberDeal = false
        val hotel3ExpectedString = ",;Hotel:3;;;;eVar39=4-MIP|eVar30=0"

        hotels.add(hotel0)
        hotels.add(hotel1)
        hotels.add(hotel2)
        hotels.add(hotel3)

        val productString = OmnitureTracking.getSearchResultsHotelProductStrings(hotels)
        val expectedString = hotel0ExpectedString + hotel1ExpectedString + hotel2ExpectedString + hotel3ExpectedString

        assertEquals(expectedString, productString)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDestSearchEventsSwp() {
        OmnitureTracking.trackHotelV2SearchBox(true);
        assertStateTracked(withEventsString("event118"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventHotelSoldOutIgnoreRoomSoldOut() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = false, isCurrentLocationSearch = false, isHotelSoldOut = true,
                isRoomSoldOut = true, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)

        assertStateTracked(withEventsString("event3,event14,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventHotelSoldOutIgnoreEtpEligible() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = true, isCurrentLocationSearch = false, isHotelSoldOut = true,
                isRoomSoldOut = false, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)

        assertStateTracked(withEventsString("event3,event14,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventEtpEligible() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = true, isCurrentLocationSearch = false, isHotelSoldOut = false,
                isRoomSoldOut = false, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)

        // events=event3,event5,event220,event221=0.00
        assertStateTracked(withEventsString("event3,event5,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventRoomSoldOut() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = false, isCurrentLocationSearch = false, isHotelSoldOut = false,
                isRoomSoldOut = true, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)
        assertStateTracked(withEventsString("event3,event18,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventEtpEligibleRoomSoldOut() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = true, isCurrentLocationSearch = false, isHotelSoldOut = false,
                isRoomSoldOut = true, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)
        assertStateTracked(withEventsString("event3,event5,event18,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventSwpEnabled() {
        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = false, isCurrentLocationSearch = false, isHotelSoldOut = false,
                isRoomSoldOut = false, pageLoadTimeData = pageLoadTimeData, swpEnabled = true)
        assertStateTracked(withEventsString("event3,event118,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInfoSiteTrackEventAirAttached() {
        hotelOffersResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.airAttached = true

        trackPageLoadHotelV2Infosite(hotelOffersResponse = hotelOffersResponse,
                isETPEligible = false, isCurrentLocationSearch = false, isHotelSoldOut = false,
                isRoomSoldOut = false, pageLoadTimeData = pageLoadTimeData, swpEnabled = false)
        assertStateTracked(withEventsString("event3,event57,event220,event221=0.00"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsDefault() {
        val trackingData = searchTrackingData()

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsSwpEnabled() {
        val trackingData = searchTrackingData()
        trackingData.swpEnabled = true

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51,event118"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsPinnedHotel() {
        val trackingData = searchTrackingData()
        trackingData.hasPinnedHotel = true
        trackingData.pinnedHotelSoldOut = false

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51,event282"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsPinnedHotelSoldOut() {
        val trackingData = searchTrackingData()
        trackingData.hasPinnedHotel = true
        trackingData.pinnedHotelSoldOut = true

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51,event283"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsHasSoldOutHotel() {
        val trackingData = searchTrackingData()
        trackingData.hasSoldOutHotel = true

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51,event14"), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsPageUsable() {
        val trackingData = searchTrackingData()
        val mockPerfData = Mockito.mock(AbstractSearchTrackingData.PerformanceData::class.java)
        whenever(mockPerfData.getPageLoadTime()).thenReturn("1.0")
        trackingData.performanceData = mockPerfData

        OmnitureTracking.trackHotelsV2Search(trackingData)
        assertStateTracked(withEventsString("event12,event51,event220,event221=1.0"), mockAnalyticsProvider)
    }

    private fun trackPageLoadHotelV2Infosite(hotelOffersResponse: HotelOffersResponse, isETPEligible: Boolean,
                                             isCurrentLocationSearch: Boolean, isHotelSoldOut: Boolean, isRoomSoldOut: Boolean,
                                             pageLoadTimeData: PageUsableData, swpEnabled: Boolean) {
        OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse,
                isETPEligible, isCurrentLocationSearch, isHotelSoldOut, isRoomSoldOut, pageLoadTimeData, swpEnabled)
    }

    private fun createHotelOffersResponse(): HotelOffersResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/offers/happypath.json")
        val hotelOffersResponse = resourceReader.constructUsingGson(HotelOffersResponse::class.java)
        hotelOffersResponse.checkInDate = "2000-01-01"
        hotelOffersResponse.checkOutDate = "2000-12-31"
        return hotelOffersResponse
    }

    private fun createPageUsableData(): PageUsableData {
        val pageLoadTimeData = PageUsableData()
        pageLoadTimeData.markPageLoadStarted(0)
        pageLoadTimeData.markAllViewsLoaded(1)
        return pageLoadTimeData
    }

    private fun searchTrackingData() : HotelSearchTrackingData {
        val trackingData = HotelSearchTrackingData()
        trackingData.checkInDate = LocalDate.now()
        trackingData.duration = 1
        return trackingData
    }
}
