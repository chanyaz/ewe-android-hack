package com.expedia.bookings.utils

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.data.user.User
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.tune.TuneEvent
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

@RunWith(RobolectricRunner::class)
class TuneUtilsTests {
    private lateinit var provider: TestTuneTrackingProviderImpl

    @After
    fun tearDown() {
        TuneUtils.init(null)
    }

    @Test
    fun testInitUpdatesPOSAndTracksLaunchEvent() {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.BASE))

        assertTrue(provider.posData.isEmpty())
        assertNull(provider.trackedEvent)

        TuneUtils.init(provider)

        assertFalse(provider.posData.isEmpty())
        assertNotNull(provider.trackedEvent)
        assertEquals("Custom_Open", provider.trackedEvent?.eventName)
        assertEquals("0", provider.trackedEvent?.attribute1)
        assertEquals("0", provider.trackedEvent?.attribute2)
        assertEquals(LoyaltyMembershipTier.BASE.toApiValue(), provider.trackedEvent?.attribute3)
        assertNull(provider.trackedEvent?.attribute4)
    }

    @Test
    fun testSetFacebookReferralUrlEquality() {
        val expectedUrlString = "http://expedia.com"

        provider = TestTuneTrackingProviderImpl()
        TuneUtils.init(provider)

        assertTrue(provider.facebookReferralUrlString.isEmpty())

        TuneUtils.setFacebookReferralUrl(expectedUrlString)

        assertEquals(expectedUrlString, provider.facebookReferralUrlString)
    }

    @Test
    fun testHotelSearchPackageResults() {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.BASE))
        TuneUtils.init(provider)

        val searchResponse = PackageSearchResponse()
        searchResponse.packageInfo = PackageSearchResponse.PackageInfo()
        searchResponse.packageInfo.hotelCheckinDate = PackageSearchResponse.HotelCheckinDate()
        searchResponse.packageInfo.hotelCheckinDate.isoDate = "2025-11-23"
        searchResponse.packageInfo.hotelCheckoutDate = PackageSearchResponse.HotelCheckoutDate()
        searchResponse.packageInfo.hotelCheckoutDate.isoDate = "2025-11-26"
        searchResponse.packageResult = PackageSearchResponse.PackageResult()
        searchResponse.packageResult.hotelsPackage = PackageSearchResponse.HotelPackage()

        val hotel1 = generateHotelSearchObject("123", "Holiday Inn", "Denver", 125.00f, 8.0)
        val hotel2 = generateHotelSearchObject("124", "Marriott", "Denver", 145.00f, 7.0)
        val hotel3 = generateHotelSearchObject("125", "Motel 8", "Denver", 76.00f, 5.0)

        searchResponse.packageResult.hotelsPackage.hotels = listOf(hotel1, hotel2, hotel3)

        TuneUtils.trackPackageHotelSearchResults(searchResponse)

        assertEquals(provider.trackedEvent?.eventName, "package_search_results")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.itemname, "package_search_result_item")
        assertEquals(provider.trackedEvent?.searchString, "hotel")
        assertEquals(provider.trackedEvent?.date1, LocalDate(2025, 11, 23).toDate())
        assertEquals(provider.trackedEvent?.date2, LocalDate(2025, 11, 26).toDate())
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute1, "Denver")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute4, "123,124,125")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute5, "123|Holiday Inn|\$|125.0|0.0|8.0:124|Marriott|\$|145.0|0.0|7.0:125|Motel 8|\$|76.0|0.0|5.0")
    }

    @Test
    fun testOutboundFlightPackageTracking() {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.BASE))
        TuneUtils.init(provider)

        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("MCO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, LocalDate.now(), LocalDate.now().plusDays(2), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 320), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageOutBoundResults(packageSearchParams)

        assertEquals(provider.trackedEvent?.eventName, "package_outbound_search_results")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.itemname, "package_outbound_search_item")
        assertEquals(provider.trackedEvent?.searchString, "flight")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute2, "DTW")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute3, "MCO")
        assertEquals(provider.trackedEvent?.date1, LocalDate.now().toDate())
        assertEquals(provider.trackedEvent?.date2, LocalDate.now().plusDays(2).toDate())
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute5, "AA|\$|320|RT|12345-54321:DL|\$|430|RT|12345-54321")
    }

    @Test
    fun testInboundFlightPackageTracking() {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.BASE))
        TuneUtils.init(provider)

        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("SFO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, LocalDate.now(), LocalDate.now().plusDays(4), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 829), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageInBoundResults(packageSearchParams)

        assertEquals(provider.trackedEvent?.eventName, "package_inbound_search_results")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.itemname, "package_inbound_search_item")
        assertEquals(provider.trackedEvent?.searchString, "flight")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute2, "DTW")
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute3, "SFO")
        assertEquals(provider.trackedEvent?.date1, LocalDate.now().toDate())
        assertEquals(provider.trackedEvent?.date2, LocalDate.now().plusDays(4).toDate())
        assertEquals(provider.trackedEvent?.eventItems?.first()?.attribute5, "AA|\$|829|RT|12345-54321:DL|\$|430|RT|12345-54321")
    }

    private fun generateHotelSearchObject(hotelId: String, hotelName: String, hotelCity: String, hotelPrice: Float, proximity: Double) : Hotel {
        val hotel = Hotel()
        hotel.hotelId = hotelId
        hotel.localizedName = hotelName
        hotel.lowRateInfo = HotelRate()
        hotel.lowRateInfo.total = hotelPrice
        hotel.lowRateInfo.currencyCode = "$"
        hotel.proximityDistanceInMiles = proximity
        hotel.city = hotelCity

        return hotel
    }

    private fun generateFlightSuggestionV4(airportCode: String, gaiaId: String) : SuggestionV4 {
        val flightSuggestionV4 = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        hierarchyInfo.airport = SuggestionV4.Airport()
        hierarchyInfo.airport?.airportCode = airportCode
        flightSuggestionV4.hierarchyInfo = hierarchyInfo
        flightSuggestionV4.gaiaId = gaiaId

        return flightSuggestionV4
    }

    private fun generateFlightLeg(airlineCode: String, amount: Int) : FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.carrierCode = airlineCode
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money(amount, "$")

        return flightLeg
    }

    private class TestTuneTrackingProviderImpl(private val user: User? = UserLoginTestUtil.mockUser(),
                                               private val isLoggedIn: Boolean = false): TuneTrackingProvider {
        var trackedEvent: TuneEvent? = null
            private set
        override val authenticatedUser: User?
            get() = user
        override val tuid: String
            get() = authenticatedUser?.tuidString ?: ""
        override val membershipTier: String?
            get() = authenticatedUser?.loyaltyMembershipInformation?.loyaltyMembershipTier?.toApiValue()
        override val isUserLoggedInValue: String
            get() = if (isLoggedIn) "1" else "0"
        override var posData: String = ""
        override var facebookReferralUrlString: String = ""

        override fun trackEvent(event: TuneEvent) {
            trackedEvent = event
        }

        override fun didFailDeeplink(error: String?) { TODO("not implemented") }
        override fun didReceiveDeeplink(deeplink: String?) { TODO("not implemented") }
    }
}
