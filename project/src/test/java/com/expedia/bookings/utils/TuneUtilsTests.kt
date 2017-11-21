package com.expedia.bookings.utils

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.*
import com.expedia.bookings.data.user.User
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.tune.TuneEvent
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
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
        setupTuneProvider()

        assertTrue(provider.facebookReferralUrlString.isEmpty())

        TuneUtils.setFacebookReferralUrl(expectedUrlString)

        assertEquals(expectedUrlString, provider.facebookReferralUrlString)
    }

    @Test
    fun testTrackHotelSearchResults() {
        setupTuneProvider()

        val hotelSearchData = HotelSearchTrackingData()
        hotelSearchData.checkInDate = LocalDate.now()
        hotelSearchData.checkoutDate = LocalDate.now().plusDays(2)

        val hotel1 = generateHotelSearchObject("123", "Holiday Inn", "Orlando", 125.00f, 8.0)
        val hotel2 = generateHotelSearchObject("124", "Marriott", "Orlando", 145.00f, 7.0)
        val hotel3 = generateHotelSearchObject("125", "Motel 8", "Orlando", 76.00f, 5.0)
        hotelSearchData.hotels = listOf(hotel1, hotel2, hotel3)

        TuneUtils.trackHotelV2SearchResults(hotelSearchData)

        assertEquals("hotel_search_results", provider.trackedEvent?.eventName)
        assertEquals("hotel_search_results_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("hotel", provider.trackedEvent?.searchString)
        assertEquals(LocalDate.now().toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate.now().plusDays(2).toDate(), provider.trackedEvent?.date2)
        assertEquals("Orlando", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals("123,124,125", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals("123|Holiday Inn|\$|125.0|0.0|8.0:124|Marriott|\$|145.0|0.0|7.0:125|Motel 8|\$|76.0|0.0|5.0", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackHotelInfoSite() {
        setupTuneProvider()

        val hotelOffersResponse = HotelOffersResponse()
        hotelOffersResponse.checkInDate = "2025-11-23"
        hotelOffersResponse.checkOutDate = "2025-11-26"
        hotelOffersResponse.hotelCity = "Las Vegas"
        hotelOffersResponse.hotelName = "The Bellagio"
        hotelOffersResponse.hotelId = "12345"

        val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelRoomResponse.supplierType = "Hotel Supplier"
        hotelRoomResponse.rateInfo = generateHotelRateInfoObject(149f, 89f)
        hotelOffersResponse.hotelRoomResponse = listOf(hotelRoomResponse)

        TuneUtils.trackHotelV2InfoSite(hotelOffersResponse)

        assertEquals("hotel_infosite", provider.trackedEvent?.eventName)
        assertEquals("hotel_infosite_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(LocalDate(2025, 11, 23).toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate(2025, 11, 26).toDate(), provider.trackedEvent?.date2)
        assertEquals("12345", provider.trackedEvent?.contentId)
        assertEquals("The Bellagio", provider.trackedEvent?.contentType)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(89.00, provider.trackedEvent?.revenue)
        assertEquals("Las Vegas", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals("Hotel Supplier", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals(3, provider.trackedEvent?.eventItems?.first()?.quantity)
    }

    @Test
    fun testTrackHotelCheckoutStarted() {
        setupTuneProvider()

        val hotelProductResponse = HotelCreateTripResponse.HotelProductResponse()
        hotelProductResponse.hotelCity = "Seattle"
        hotelProductResponse.hotelName = "Space Needle Inn"
        hotelProductResponse.hotelId = "1029"
        hotelProductResponse.hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelProductResponse.hotelRoomResponse.roomTypeDescription = "King Suite"
        hotelProductResponse.checkInDate = "2025-11-23"
        hotelProductResponse.checkOutDate = "2025-11-26"

        val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelRoomResponse.rateInfo = generateHotelRateInfoObject(139f, 60f)
        hotelProductResponse.hotelRoomResponse = hotelRoomResponse

        TuneUtils.trackHotelV2CheckoutStarted(hotelProductResponse)

        assertEquals("hotel_rate_details", provider.trackedEvent?.eventName)
        assertEquals("hotel_rate_details_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(LocalDate(2025, 11, 23).toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate(2025, 11, 26).toDate(), provider.trackedEvent?.date2)
        assertEquals("1029", provider.trackedEvent?.contentId)
        assertEquals("Space Needle Inn", provider.trackedEvent?.contentType)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(139.00, provider.trackedEvent?.revenue)
        assertEquals("Seattle", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals(3, provider.trackedEvent?.quantity)
    }

    @Test
    fun testTrackHotelCheckoutConfirmation() {
        setupTuneProvider()

        val hotelCheckoutResponse = HotelCheckoutResponse()
        val productResponse = HotelCheckoutResponse.ProductResponse()
        productResponse.checkInDate = "2025-11-23"
        productResponse.checkOutDate = "2025-11-26"
        productResponse.hotelCity = "Phoenix"
        productResponse.localizedHotelName = "The Phoenician"
        productResponse.hotelId = "18762"

        val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelRoomResponse.rateInfo = generateHotelRateInfoObject(269.99f, 249.99f)
        productResponse.hotelRoomResponse = hotelRoomResponse

        val checkoutResponse = HotelCheckoutResponse.CheckoutResponse()
        checkoutResponse.productResponse = productResponse

        val bookingResponse = HotelCheckoutResponse.BookingResponse()
        bookingResponse.travelRecordLocator = "TRL"

        checkoutResponse.bookingResponse = bookingResponse
        hotelCheckoutResponse.checkoutResponse = checkoutResponse
        hotelCheckoutResponse.totalCharges = "269.99"
        hotelCheckoutResponse.currencyCode = "USD"

        TuneUtils.trackHotelV2Confirmation(hotelCheckoutResponse)

        assertEquals("hotel_confirmation", provider.trackedEvent?.eventName)
        assertEquals("hotel_confirmation_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(LocalDate(2025, 11, 23).toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate(2025, 11, 26).toDate(), provider.trackedEvent?.date2)
        assertEquals("18762", provider.trackedEvent?.contentId)
        assertEquals("The Phoenician", provider.trackedEvent?.contentType)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(269.99, provider.trackedEvent?.revenue)
        assertEquals("Phoenix", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals(3, provider.trackedEvent?.quantity)
    }

    @Test
    fun testTrackHotelSearchPackageResults() {
        setupTuneProvider()
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

        assertEquals("package_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_search_result_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("hotel", provider.trackedEvent?.searchString)
        assertEquals(LocalDate(2025, 11, 23).toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate(2025, 11, 26).toDate(), provider.trackedEvent?.date2)
        assertEquals("Denver", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals("123,124,125", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals("123|Holiday Inn|\$|125.0|0.0|8.0:124|Marriott|\$|145.0|0.0|7.0:125|Motel 8|\$|76.0|0.0|5.0", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackOutboundFlightPackageTracking() {
        setupTuneProvider()
        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("MCO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, LocalDate.now(), LocalDate.now().plusDays(2), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 320), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageOutBoundResults(packageSearchParams)

        assertEquals("package_outbound_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_outbound_search_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("MCO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(LocalDate.now().toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate.now().plusDays(2).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|320|RT|12345-54321:DL|\$|430|RT|12345-54321", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackInboundFlightPackageTracking() {
        setupTuneProvider()
        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("SFO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, LocalDate.now(), LocalDate.now().plusDays(4), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 829), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageInBoundResults(packageSearchParams)

        assertEquals("package_inbound_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_inbound_search_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("SFO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(LocalDate.now().toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate.now().plusDays(4).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|829|RT|12345-54321:DL|\$|430|RT|12345-54321", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackPackageConfirmation() {
        setupTuneProvider()

        val checkoutResponse = PackageCheckoutResponse()
        val packageSearchParams = PackageSearchParams(null, null, LocalDate.now(), LocalDate.now().plusDays(1),1, listOf(),false)

        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.hotel = HotelCreateTripResponse.HotelProductResponse()
        packageDetails.hotel.checkInDate = LocalDate.now().toString()
        packageDetails.hotel.checkOutDate = LocalDate.now().plusDays(1).toString()
        packageDetails.hotel.hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        packageDetails.hotel.hotelRoomResponse.rateInfo = HotelOffersResponse.RateInfo()
        packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo = HotelRate()
        packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo.averageRate = 89f
        packageDetails.hotel.hotelCity = "San Francisco"
        packageDetails.hotel.hotelId = "12345"
        packageDetails.hotel.hotelName = "Holiday Inn"
        packageDetails.pricing = PackageCreateTripResponse.Pricing()
        packageDetails.pricing.packageTotal = Money()
        packageDetails.pricing.packageTotal.amount = BigDecimal(750.99)
        packageDetails.pricing.packageTotal.currencyCode = "USD"

        val flightSegment = FlightLeg.FlightSegment()
        flightSegment.flightNumber = "Dl25"
        val flightSegments = listOf(flightSegment)
        val flightLeg = FlightLeg()
        flightLeg.flightSegments = flightSegments
        val flightLegList = listOf(flightLeg)

        packageSearchParams.flightLegList = flightLegList
        checkoutResponse.packageDetails = packageDetails

        TuneUtils.trackPackageConfirmation(checkoutResponse, packageSearchParams)

        assertEquals("package_confirmation", provider.trackedEvent?.eventName)
        assertEquals("package_confirmation_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(750.99, provider.trackedEvent?.revenue)
        assertEquals(89.00, provider.trackedEvent?.eventItems?.first()?.unitPrice)
        assertEquals("San Francisco", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals("Dl25", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals(LocalDate.now().toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate.now().plusDays(1).toDate(), provider.trackedEvent?.date2)
        assertEquals(1, provider.trackedEvent?.quantity)
        assertEquals("12345", provider.trackedEvent?.contentId)
        assertEquals("Holiday Inn", provider.trackedEvent?.contentType)
    }

    private fun setupTuneProvider(membershipTier: LoyaltyMembershipTier = LoyaltyMembershipTier.BASE, isLoggedIn: Boolean = false) {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(membershipTier), isLoggedIn)
        TuneUtils.init(provider)
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

    private fun generateHotelRateInfoObject(total: Float, average: Float) : HotelOffersResponse.RateInfo {
        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = HotelRate()
        rateInfo.chargeableRateInfo.total = total
        rateInfo.chargeableRateInfo.averageRate = average
        rateInfo.chargeableRateInfo.currencyCode = "USD"

        return rateInfo
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
