package com.expedia.bookings.utils

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripInfo
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.lx.SearchType
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.user.User
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.tune.TuneEvent
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TuneUtilsTests {
    private lateinit var provider: TestTuneTrackingProviderImpl
    private lateinit var baseStartDate: LocalDate

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        baseStartDate = LocalDate.now()
    }

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
        hotelSearchData.checkInDate = baseStartDate
        hotelSearchData.checkoutDate = baseStartDate.plusDays(2)

        val hotel1 = generateHotelSearchObject("123", "Holiday Inn", "Orlando", 125.00f, 8.0)
        val hotel2 = generateHotelSearchObject("124", "Marriott", "Orlando", 145.00f, 7.0)
        val hotel3 = generateHotelSearchObject("125", "Motel 8", "Orlando", 76.00f, 5.0)
        hotelSearchData.hotels = listOf(hotel1, hotel2, hotel3)

        TuneUtils.trackHotelV2SearchResults(hotelSearchData)

        assertEquals("hotel_search_results", provider.trackedEvent?.eventName)
        assertEquals("hotel_search_results_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("hotel", provider.trackedEvent?.searchString)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(2).toDate(), provider.trackedEvent?.date2)
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
    fun testTrackFlightV2OutBoundResults() {
        setupTuneProvider()

        val flightSearchData = FlightSearchTrackingData()
        flightSearchData.departureAirport = generateFlightSuggestionV4("DTW", "10019")
        flightSearchData.arrivalAirport = generateFlightSuggestionV4("SFO", "90210")
        flightSearchData.flightLegList = listOf(generateFlightLeg("AA", 830), generateFlightLeg("DL", 420))
        flightSearchData.departureDate = baseStartDate
        flightSearchData.returnDate = baseStartDate.plusDays(5)

        TuneUtils.trackFlightV2OutBoundResults(flightSearchData)

        assertEquals("flight_outbound_result", provider.trackedEvent?.eventName)
        assertEquals("flight_outbound_result_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("SFO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(5).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|830|RT|10019-90210:DL|\$|420|RT|10019-90210", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackFlightV2InBoundResults() {
        setupTuneProvider()

        val flightSearchData = FlightSearchTrackingData()
        flightSearchData.departureAirport = generateFlightSuggestionV4("DTW", "12345")
        flightSearchData.arrivalAirport = generateFlightSuggestionV4("MCO", "48172")
        flightSearchData.flightLegList = listOf(generateFlightLeg("AA", 850), generateFlightLeg("DL", 450))
        flightSearchData.departureDate = baseStartDate
        flightSearchData.returnDate = baseStartDate.plusDays(5)

        TuneUtils.trackFlightV2InBoundResults(flightSearchData)

        assertEquals("flight_inbound_result", provider.trackedEvent?.eventName)
        assertEquals("flight_inbound_result_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("MCO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(5).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|850|RT|12345-48172:DL|\$|450|RT|12345-48172", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackFlightV2RateDetailOverview() {
        setupTuneProvider()

        val flightSearchParams = FlightSearchParams(generateFlightSuggestionV4("DTW", "12005"), generateFlightSuggestionV4("SFO", "00887"),
                baseStartDate, baseStartDate.plusDays(3), 1, listOf(), false, "first class", 1, "1350", false, true, null)

        val flightCreateTripResponse = FlightCreateTripResponse()
        flightCreateTripResponse.details = FlightTripDetails()
        flightCreateTripResponse.details.legs = listOf(generateFlightLeg("DL", 560))
        flightCreateTripResponse.details.offer = FlightTripDetails.FlightOffer()
        flightCreateTripResponse.details.offer.totalPrice = Money(560, "USD")

        val tripBucketFlight = TripBucketItemFlightV2(flightCreateTripResponse)
        Db.getTripBucket().add(tripBucketFlight)

        TuneUtils.trackFlightV2RateDetailOverview(flightSearchParams)

        assertEquals("flight_rate_details", provider.trackedEvent?.eventName)
        assertEquals("flight_rate_details_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(1, provider.trackedEvent?.eventItems?.first()?.quantity)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("SFO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals("DL", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(3).toDate(), provider.trackedEvent?.date2)
        assertEquals(560.00, provider.trackedEvent?.revenue)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
    }

    @Test
    fun testTrackFlightV2Booked() {
        setupTuneProvider()

        val flightCheckoutResponse = FlightCheckoutResponse()
        flightCheckoutResponse.totalChargesPrice = Money(826, "USD")
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.legs = listOf(generateFlightLeg("DL", 826), generateFlightLeg("AA", 560, true))
        flightCheckoutResponse.flightAggregatedResponse = FlightCheckoutResponse.FlightAggregatedResponse()
        flightCheckoutResponse.flightAggregatedResponse?.flightsDetailResponse = listOf(flightTripDetails)

        val flightSearchParams = FlightSearchParams(generateFlightSuggestionV4("DTW", "12345"), generateFlightSuggestionV4("MCO", "8675"),
                baseStartDate, baseStartDate.plusDays(5), 2, listOf(), false, "first class", 2, "123", false, true, null)

        TuneUtils.trackFlightV2Booked(flightCheckoutResponse, flightSearchParams)

        assertEquals("flight_confirmation", provider.trackedEvent?.eventName)
        assertEquals("flight_confirmation_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(826.00, provider.trackedEvent?.eventItems?.first()?.revenue)
        assertEquals(413.00, provider.trackedEvent?.eventItems?.first()?.unitPrice)
        assertEquals("12345", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("8675", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals("DL", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(1).toDate(), provider.trackedEvent?.date2)
        assertEquals(826.00, provider.trackedEvent?.revenue)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(2, provider.trackedEvent?.quantity)
    }

    @Test
    fun testTrackHotelSearchPackageResults() {
        setupTuneProvider()
        val searchResponse = mockPackageServiceRule.getMIDHotelResponse()
        TuneUtils.trackPackageHotelSearchResults(searchResponse)

        assertEquals("package_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_search_result_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("hotel", provider.trackedEvent?.searchString)
        assertEquals(LocalDate(2018, 5, 7).toDate(), provider.trackedEvent?.date1)
        assertEquals(LocalDate(2018, 5, 10).toDate(), provider.trackedEvent?.date2)
        assertEquals("Kathmandu", provider.trackedEvent?.eventItems?.first()?.attribute1)
        assertEquals("happy_room,5857498,531192,5421636,9787693", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals("happy_room|happy_room|USD|0.0|4.0|0:5857498|The Dwarika's Hotel|USD|0.0|5.0|0:531192|Hyatt Regency Kathmandu|USD|0.0|5.0|0:5421636|Hotel Yak & Yeti|USD|0.0|5.0|0:9787693|Dalai-La Boutique Hotel|USD|0.0|3.5|0", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackOutboundFlightPackageTracking() {
        setupTuneProvider()
        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("MCO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, baseStartDate, baseStartDate.plusDays(2), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 320), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageOutBoundResults(packageSearchParams)

        assertEquals("package_outbound_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_outbound_search_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("MCO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(2).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|320|RT|12345-54321:DL|\$|430|RT|12345-54321", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackInboundFlightPackageTracking() {
        setupTuneProvider()
        val origin = generateFlightSuggestionV4("DTW", "12345")
        val destination = generateFlightSuggestionV4("SFO", "54321")
        val packageSearchParams = PackageSearchParams(origin, destination, baseStartDate, baseStartDate.plusDays(4), 1, listOf(0), false)
        packageSearchParams.flightLegList = listOf(generateFlightLeg("AA", 829), generateFlightLeg("DL", 430))

        TuneUtils.trackPackageInBoundResults(packageSearchParams)

        assertEquals("package_inbound_search_results", provider.trackedEvent?.eventName)
        assertEquals("package_inbound_search_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("flight", provider.trackedEvent?.searchString)
        assertEquals("DTW", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("SFO", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(4).toDate(), provider.trackedEvent?.date2)
        assertEquals("AA|\$|829|RT|12345-54321:DL|\$|430|RT|12345-54321", provider.trackedEvent?.eventItems?.first()?.attribute5)
    }

    @Test
    fun testTrackPackageConfirmation() {
        setupTuneProvider()

        val checkoutResponse = PackageCheckoutResponse()
        val packageSearchParams = PackageSearchParams(null, null, baseStartDate, baseStartDate.plusDays(1), 1, listOf(), false)

        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.hotel = HotelCreateTripResponse.HotelProductResponse()
        packageDetails.hotel.checkInDate = baseStartDate.toString()
        packageDetails.hotel.checkOutDate = baseStartDate.plusDays(1).toString()
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
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals(baseStartDate.plusDays(1).toDate(), provider.trackedEvent?.date2)
        assertEquals(1, provider.trackedEvent?.quantity)
        assertEquals("12345", provider.trackedEvent?.contentId)
        assertEquals("Holiday Inn", provider.trackedEvent?.contentType)
    }

    @Test
    fun testTrackLXSearch() {
        setupTuneProvider()

        val searchParams = LxSearchParams("Las Vegas", baseStartDate, baseStartDate.plusDays(1), SearchType.DEFAULT_SEARCH, "park", null, null, false)
        val searchResponse = LXSearchResponse()
        searchResponse.activities = listOf(generateLXActivity("Tour", 80), generateLXActivity("Excursion", 125), generateLXActivity("Theme Park", 90))

        TuneUtils.trackLXSearch(searchParams, searchResponse)

        assertEquals("lx_search", provider.trackedEvent?.eventName)
        assertEquals("lx_search_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("Las Vegas", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("Tour,Excursion,Theme Park", provider.trackedEvent?.eventItems?.first()?.attribute4)
        assertEquals("Tour|USD|80.0:Excursion|USD|125.0:Theme Park|USD|90.0", provider.trackedEvent?.eventItems?.first()?.attribute5)
        assertEquals(baseStartDate.toDate(), provider.trackedEvent?.date1)
        assertEquals("lx", provider.trackedEvent?.searchString)
    }

    @Test
    fun testTrackLXDetails() {
        setupTuneProvider()

        val activityDate = "2025-12-10 12:30:00"
        val totalPrice = Money(149, "USD")

        TuneUtils.trackLXDetails("Orlando", totalPrice, activityDate, 1, "Tour")

        assertEquals("lx_details", provider.trackedEvent?.eventName)
        assertEquals("lx_details_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals("Orlando", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("Tour", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(1, provider.trackedEvent?.quantity)
        assertEquals(totalPrice.getAmount().toDouble(), provider.trackedEvent?.revenue)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(ApiDateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate(), provider.trackedEvent?.date1)
    }

    @Test
    fun testTrackLXConfirmation() {
        setupTuneProvider()

        val totalPrice = Money(150, "USD")
        val ticketPrice = Money(50, "USD")
        val activityDate = "2025-12-10 12:30:00"
        val checkoutResponse = LXCheckoutResponse()
        checkoutResponse.newTrip = TripInfo()
        checkoutResponse.newTrip.travelRecordLocator = "TRL"

        TuneUtils.trackLXConfirmation("San Francisco", totalPrice, ticketPrice, activityDate, checkoutResponse, "Tour", 2, 1)

        assertEquals("lx_confirmation", provider.trackedEvent?.eventName)
        assertEquals("lx_confirmation_item", provider.trackedEvent?.eventItems?.first()?.itemname)
        assertEquals(3, provider.trackedEvent?.eventItems?.first()?.quantity)
        assertEquals(totalPrice.amount.toDouble(), provider.trackedEvent?.eventItems?.first()?.revenue)
        assertEquals(ticketPrice.amount.toDouble(), provider.trackedEvent?.eventItems?.first()?.unitPrice)
        assertEquals("San Francisco", provider.trackedEvent?.eventItems?.first()?.attribute2)
        assertEquals("Tour", provider.trackedEvent?.eventItems?.first()?.attribute3)
        assertEquals(totalPrice.amount.toDouble(), provider.trackedEvent?.revenue)
        assertEquals(1, provider.trackedEvent?.quantity)
        assertEquals("USD", provider.trackedEvent?.currencyCode)
        assertEquals(ApiDateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate(), provider.trackedEvent?.date1)
    }

    private fun setupTuneProvider(membershipTier: LoyaltyMembershipTier = LoyaltyMembershipTier.BASE, isLoggedIn: Boolean = false) {
        provider = TestTuneTrackingProviderImpl(UserLoginTestUtil.mockUser(membershipTier), isLoggedIn)
        TuneUtils.init(provider)
    }

    private fun generateHotelSearchObject(hotelId: String, hotelName: String, hotelCity: String, hotelPrice: Float, proximity: Double): Hotel {
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

    private fun generateHotelRateInfoObject(total: Float, average: Float): HotelOffersResponse.RateInfo {
        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = HotelRate()
        rateInfo.chargeableRateInfo.total = total
        rateInfo.chargeableRateInfo.averageRate = average
        rateInfo.chargeableRateInfo.currencyCode = "USD"

        return rateInfo
    }

    private fun generateFlightSuggestionV4(airportCode: String, gaiaId: String): SuggestionV4 {
        val flightSuggestionV4 = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        hierarchyInfo.airport = SuggestionV4.Airport()
        hierarchyInfo.airport?.airportCode = airportCode
        flightSuggestionV4.hierarchyInfo = hierarchyInfo
        flightSuggestionV4.gaiaId = gaiaId

        return flightSuggestionV4
    }

    private fun generateFlightLeg(airlineCode: String, amount: Int, isReturn: Boolean = false): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.carrierCode = airlineCode
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money(amount, "$")

        val segment = FlightLeg.FlightSegment()
        segment.airlineCode = airlineCode
        if (isReturn) {
            segment.departureTimeRaw = baseStartDate.plusDays(1).toString()
        } else {
            segment.departureTimeRaw = baseStartDate.toString()
        }
        segment.arrivalTimeRaw = baseStartDate.plusDays(1).toString()
        flightLeg.segments = listOf(segment)

        return flightLeg
    }

    private fun generateLXActivity(title: String, amount: Int): LXActivity {
        val activity = LXActivity()
        activity.title = title
        activity.price = Money(amount, "USD")

        return activity
    }

    private class TestTuneTrackingProviderImpl(private val user: User? = UserLoginTestUtil.mockUser(),
                                               private val isLoggedIn: Boolean = false) : TuneTrackingProvider {
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
