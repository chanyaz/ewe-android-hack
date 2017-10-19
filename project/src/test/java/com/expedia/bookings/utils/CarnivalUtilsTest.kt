package com.expedia.bookings.utils

import com.carnival.sdk.AttributeMap
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.rail.responses.*
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarnivalUtilsTest : CarnivalUtils() {

    private lateinit var attributesToSend : AttributeMap
    private var eventNameToLog: String? = null
    private var userIdToLog: String? = null
    private var userEmailToLog: String? = null
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        SettingUtils.save(context, R.string.preference_new_carnival_notifications, true)
        initialize(context)
        attributesToSend = AttributeMap()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelSearch() {
        reset()

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Las Vegas, NV"
        val searchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 1, listOf(0), false, false, null, null)

        this.trackHotelSearch(searchParams)

        assertEquals(eventNameToLog, "search_hotel")
        assertEquals(attributesToSend.get("search_hotel_destination"),"Las Vegas, NV")
        assertEquals(attributesToSend.get("search_hotel_number_of_adults"), 1)
        assertEquals(attributesToSend.get("search_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("search_hotel_length_of_stay"), 3)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelInfoSite() {
        reset()

        var hotelOfferResponse = HotelOffersResponse()
        hotelOfferResponse.hotelName = "Twin Lotus Koh Lanta by Burasari"
        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Krabi, Thailand"
        var searchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, false, null, null)

        this.trackHotelInfoSite(hotelOfferResponse, searchParams)

        assertEquals(eventNameToLog, "product_view_hotel")
        assertEquals(attributesToSend.get("product_view_hotel_destination"), "Krabi, Thailand")
        assertEquals(attributesToSend.get("product_view_hotel_hotel_name"), "Twin Lotus Koh Lanta by Burasari")
        assertEquals(attributesToSend.get("product_view_hotel_number_of_adults"), 2)
        assertEquals(attributesToSend.get("product_view_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("product_view_hotel_length_of_stay"), 3)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSearch() {
        reset()

        this.trackFlightSearch("Orlando - MCO", 2, LocalDate.now())

        assertEquals(eventNameToLog, "search_flight")
        assertEquals(attributesToSend.get("search_flight_destination"), "Orlando - MCO")
        assertEquals(attributesToSend.get("search_flight_number_of_adults"), 2)
        assertEquals(attributesToSend.get("search_flight_departure_date"), LocalDate.now().toDate())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightCheckoutStart() {
        reset()

        val outboundLeg = FlightLeg()
        val outboundSegment = FlightLeg.FlightSegment()
        outboundSegment.durationHours = 2
        outboundSegment.durationMinutes = 30
        outboundSegment.layoverDurationHours = 0
        outboundSegment.layoverDurationMinutes = 0
        outboundSegment.airlineName = "Delta"
        outboundSegment.flightNumber = "103"
        val outboundSegment2 = FlightLeg.FlightSegment()
        outboundSegment2.durationHours = 1
        outboundSegment2.durationMinutes = 0
        outboundSegment2.layoverDurationHours = 0
        outboundSegment2.layoverDurationMinutes = 0
        outboundSegment2.airlineName = "Delta"
        outboundSegment2.flightNumber = "123"
        outboundLeg.segments = listOf(outboundSegment, outboundSegment2)

        val inboundLeg = FlightLeg()
        val inboundSegment = FlightLeg.FlightSegment()
        inboundSegment.durationHours = 1
        inboundSegment.durationMinutes = 15
        inboundSegment.layoverDurationHours = 0
        inboundSegment.layoverDurationMinutes = 45
        inboundSegment.airlineName = "United"
        inboundSegment.flightNumber = "212"
        inboundLeg.segments = listOf(inboundSegment)

        this.trackFlightCheckoutStart("Orlando - MCO", 2, LocalDate.now(), outboundLeg, inboundLeg, true)

        assertEquals(eventNameToLog, "checkout_start_flight")
        assertEquals(attributesToSend.get("checkout_start_flight_destination"), "Orlando - MCO")
        assertEquals(attributesToSend.get("checkout_start_flight_airline"), arrayListOf("Delta","United"))
        assertEquals(attributesToSend.get("checkout_start_flight_flight_number"), arrayListOf("123","212", "103"))
        assertEquals(attributesToSend.get("checkout_start_flight_number_of_adults"), 2)
        assertEquals(attributesToSend.get("checkout_start_flight_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("checkout_start_flight_length_of_flight"), "5:30")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackLxConfirmation() {
        reset()

        this.trackLxConfirmation("Disney World", "2017-10-18 08:00:00")

        assertEquals(eventNameToLog, "confirmation_lx")
        assertEquals(attributesToSend.get("confirmation_lx_activity_name"), "Disney World")
        assertEquals(attributesToSend.get("confirmation_lx_date_of_activity"), DateUtils.yyyyMMddHHmmssToLocalDate("2017-10-18 08:00:00").toDate())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackPackagesConfirmation() {
        reset()

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "New York"
        val packageParams = PackageSearchParams(SuggestionV4(),v4, LocalDate.now(),LocalDate.now().plusDays(3),1, listOf(),false)

        this.trackPackagesConfirmation(packageParams)

        assertEquals(eventNameToLog, "confirmation_pkg")
        assertEquals(attributesToSend.get("confirmation_pkg_destination"), "New York")
        assertEquals(attributesToSend.get("confirmation_pkg_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("confirmation_pkg_length_of_stay"), 3)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackRailConfirmation() {
        reset()

        val railResponse = RailCheckoutResponse()
        val railProduct = RailTripProduct()
        val railLeg = RailLegOption()
        railLeg.departureDateTime = RailDateTime()
        railLeg.departureDateTime.raw = "2016-12-10T08:30:00"
        railLeg.arrivalStation = RailStation("KSC", "King's Cross", "Station", "London")
        railProduct.legOptionList = listOf(railLeg)
        railResponse.railDomainProduct = RailDomainProduct()
        railResponse.railDomainProduct.railOffer = RailTripOffer()
        railResponse.railDomainProduct.railOffer.railProductList = mutableListOf(railProduct)

        this.trackRailConfirmation(railResponse)
        val formattedRailDate = railLeg.departureDateTime.toDateTime().toDate()

        assertEquals(eventNameToLog, "confirmation_rail")
        assertEquals(attributesToSend.get("confirmation_rail_destination"), "King's Cross, London")
        assertEquals(attributesToSend.get("confirmation_rail_departure_date"), formattedRailDate)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackAppLaunch() {
        reset()

        val traveler = Traveler()
        traveler.tuid = 12345
        traveler.email = "Tester@Test.com"
        val tripComponent = TripComponent(TripComponent.Type.HOTEL)
        val hotelTrip = Trip()
        hotelTrip.addTripComponent(tripComponent)
        val trips = arrayListOf(hotelTrip)

        this.trackLaunch(true, true, traveler, trips, com.expedia.bookings.data.LoyaltyMembershipTier.TOP, 100.1, 75.2)

        assertEquals(eventNameToLog, "app_open_launch_relaunch")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_location_enabled"), true)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_userid"), 12345)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_user_email"), "Tester@Test.com")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_sign-in"), true)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_booked_product"), arrayListOf("HOTEL"))
        assertEquals(attributesToSend.get("app_open_launch_relaunch_loyalty_tier"), "GOLD")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_last_location"), "100.1, 75.2")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_notification_type"), arrayListOf("MKTG", "SERV", "PROMO"))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSetUserInfo() {
        this.setUserInfo("123456", "Tester@Test.com")

        assertEquals(userIdToLog, "123456")
        assertEquals(userEmailToLog, "Tester@Test.com")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testClearUserInfo() {
        this.clearUserInfo()

        assertEquals(userIdToLog, null)
        assertEquals(userEmailToLog, null)
    }

    private fun reset() {
        eventNameToLog = ""
        attributesToSend.clear()
    }

    override fun setAttributes(attributes: AttributeMap, eventName: String) {
        //Don't actually send anything up to carnival
        eventNameToLog = eventName
        attributesToSend = attributes
    }

    override fun setUserInfo(userId: String?, userEmail: String?) {
        //Don't actually set these user values on carnival
        userIdToLog = userId
        userEmailToLog = userEmail
    }
}
