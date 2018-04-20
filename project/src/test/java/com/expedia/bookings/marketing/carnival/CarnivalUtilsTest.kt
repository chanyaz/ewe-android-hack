package com.expedia.bookings.marketing.carnival

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Message
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailDateTime
import com.expedia.bookings.data.rail.responses.RailDomainProduct
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailStation
import com.expedia.bookings.data.rail.responses.RailTripOffer
import com.expedia.bookings.data.rail.responses.RailTripProduct
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationTypeConstants
import com.expedia.bookings.marketing.carnival.persistence.MockCarnivalPersistenceProvider
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ApiDateUtils
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowPendingIntent
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarnivalUtilsTest : CarnivalUtils() {

    private lateinit var attributesToSend: AttributeMap
    private var eventNameToLog: String? = null
    private var userIdToLog: String? = null
    private var userEmailToLog: String? = null
    private val context = RuntimeEnvironment.application
    private lateinit var persistenceProvider: MockCarnivalPersistenceProvider
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var fragmentManager: FragmentManager

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager
        persistenceProvider = MockCarnivalPersistenceProvider()
        initialize(context, persistenceProvider)
        attributesToSend = AttributeMap()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun messageListenerListens() {
        this.setupListener(fragmentManager)
        assert(supportFragmentManager != null)
    }

    @Test
    fun messageQueueWorks() {
        this.setupListener(fragmentManager)
        supportFragmentManager = null
        this.createInAppNotification(supportFragmentManager, CarnivalMessage())

        assert(messageQueue.any())
    }

    @Test
    fun messageQueueClears() {
        this.setupListener(fragmentManager)
        this.checkForStaleMessages()

        assert(!messageQueue.any())
    }

    @Test
    fun testTrackHotelSearch() {
        reset()

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Las Vegas, NV"
        val searchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 1, listOf(0), false, null, null)

        this.trackHotelSearch(searchParams)

        assertEquals(eventNameToLog, "search_hotel")
        assertEquals(attributesToSend.get("search_hotel_destination"), "Las Vegas, NV")
        assertEquals(attributesToSend.get("search_hotel_number_of_adults"), 1)
        assertEquals(attributesToSend.get("search_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("search_hotel_length_of_stay"), 3)
    }

    @Test
    fun testTrackHotelInfoSite() {
        reset()

        var hotelOfferResponse = HotelOffersResponse()
        hotelOfferResponse.hotelName = "Twin Lotus Koh Lanta by Burasari"
        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Krabi, Thailand"
        var searchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, null, null)

        this.trackHotelInfoSite(hotelOfferResponse, searchParams)

        assertEquals(eventNameToLog, "product_view_hotel")
        assertEquals(attributesToSend.get("product_view_hotel_destination"), "Krabi, Thailand")
        assertEquals(attributesToSend.get("product_view_hotel_hotel_name"), "Twin Lotus Koh Lanta by Burasari")
        assertEquals(attributesToSend.get("product_view_hotel_number_of_adults"), 2)
        assertEquals(attributesToSend.get("product_view_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("product_view_hotel_length_of_stay"), 3)
    }

    @Test
    fun testTrackFlightSearch() {
        reset()

        this.trackFlightSearch("Orlando - MCO", 2, LocalDate.now())

        assertEquals(eventNameToLog, "search_flight")
        assertEquals(attributesToSend.get("search_flight_destination"), "Orlando - MCO")
        assertEquals(attributesToSend.get("search_flight_number_of_adults"), 2)
        assertEquals(attributesToSend.get("search_flight_departure_date"), LocalDate.now().toDate())
    }

    @Test
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
        assertEquals(attributesToSend.get("checkout_start_flight_airline"), arrayListOf("Delta", "United"))
        assertEquals(attributesToSend.get("checkout_start_flight_flight_number"), arrayListOf("123", "212", "103"))
        assertEquals(attributesToSend.get("checkout_start_flight_number_of_adults"), 2)
        assertEquals(attributesToSend.get("checkout_start_flight_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("checkout_start_flight_length_of_flight"), "5:30")
    }

    @Test
    fun testTrackFlightConfirmation() {
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

        this.trackFlightCheckoutConfirmation("Orlando - MCO", 2, LocalDate.now(), outboundLeg, inboundLeg, true)

        assertEquals(eventNameToLog, "confirmation_flight")
        assertEquals(attributesToSend.get("confirmation_flight_destination"), "Orlando - MCO")
        assertEquals(attributesToSend.get("confirmation_flight_airline"), arrayListOf("Delta", "United"))
        assertEquals(attributesToSend.get("confirmation_flight_flight_number"), arrayListOf("123", "212", "103"))
        assertEquals(attributesToSend.get("confirmation_flight_number_of_adults"), 2)
        assertEquals(attributesToSend.get("confirmation_flight_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("confirmation_flight_length_of_flight"), "5:30")
    }

    @Test
    fun testTrackHotelCheckoutStart() {
        reset()

        val hotelCreateTripResponse = HotelCreateTripResponse()
        hotelCreateTripResponse.newHotelProductResponse = HotelCreateTripResponse.HotelProductResponse()
        hotelCreateTripResponse.newHotelProductResponse.localizedHotelName = "Hilton Garden Inn"

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Detroit, Michigan"
        val hotelSearchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, null, null)
        this.trackHotelCheckoutStart(hotelCreateTripResponse, hotelSearchParams)

        assertEquals(eventNameToLog, "checkout_start_hotel")
        assertEquals(attributesToSend.get("checkout_start_hotel_destination"), "Detroit, Michigan")
        assertEquals(attributesToSend.get("checkout_start_hotel_hotel_name"), "Hilton Garden Inn")
        assertEquals(attributesToSend.get("checkout_start_hotel_number_of_adults"), 2)
        assertEquals(attributesToSend.get("checkout_start_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("checkout_start_hotel_length_of_stay"), 3)
    }

    @Test
    fun testTrackHotelConfirmation() {
        reset()

        val hotelCheckoutResponse = HotelCheckoutResponse()
        val checkoutResponse = HotelCheckoutResponse.CheckoutResponse()
        val productResponse = HotelCheckoutResponse.ProductResponse()
        productResponse.hotelName = "Twin Lotus Koh Lanta by Burasari"
        hotelCheckoutResponse.checkoutResponse = checkoutResponse
        hotelCheckoutResponse.checkoutResponse.productResponse = productResponse
        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "Krabi, Thailand"
        var hotelSearchParams = HotelSearchParams(v4, LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, null, null)
        this.trackHotelConfirmation(hotelCheckoutResponse, hotelSearchParams)

        assertEquals(eventNameToLog, "confirmation_hotel")
        assertEquals(attributesToSend.get("confirmation_hotel_destination"), "Krabi, Thailand")
        assertEquals(attributesToSend.get("confirmation_hotel_hotel_name"), "Twin Lotus Koh Lanta by Burasari")
        assertEquals(attributesToSend.get("confirmation_hotel_number_of_adults"), 2)
        assertEquals(attributesToSend.get("confirmation_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("confirmation_hotel_length_of_stay"), 3)
    }

    @Test
    fun testTrackLxConfirmation() {
        reset()

        this.trackLxConfirmation("Disney World", "2017-10-18 08:00:00")

        assertEquals(eventNameToLog, "confirmation_lx")
        assertEquals(attributesToSend.get("confirmation_lx_activity_name"), "Disney World")
        assertEquals(attributesToSend.get("confirmation_lx_date_of_activity"), ApiDateUtils.yyyyMMddHHmmssToLocalDate("2017-10-18 08:00:00").toDate())
    }

    @Test
    fun testTrackPackagesConfirmation() {
        reset()

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "New York"
        val packageParams = PackageSearchParams(SuggestionV4(), v4, LocalDate.now(), LocalDate.now().plusDays(3), 1, listOf(), false)

        this.trackPackagesConfirmation(packageParams)

        assertEquals(eventNameToLog, "confirmation_pkg")
        assertEquals(attributesToSend.get("confirmation_pkg_destination"), "New York")
        assertEquals(attributesToSend.get("confirmation_pkg_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("confirmation_pkg_length_of_stay"), 3)
    }

    @Test
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
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testTrackAppLaunch() {
        reset()

        val traveler = Traveler()
        traveler.tuid = 12345
        traveler.email = "Tester@Test.com"
        val tripComponent = TripComponent(TripComponent.Type.HOTEL)
        val hotelTrip = Trip()
        hotelTrip.addTripComponent(tripComponent)
        val trips = arrayListOf(hotelTrip)
        val posUrl = "expedia.fr"

        this.trackLaunch(true, true, traveler, trips, com.expedia.bookings.data.LoyaltyMembershipTier.TOP, 100.1, 75.2, posUrl)

        assertEquals(eventNameToLog, "app_open_launch_relaunch")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_location_enabled"), true)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_userid"), 12345)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_user_email"), "Tester@Test.com")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_sign-in"), true)
        assertEquals(attributesToSend.get("app_open_launch_relaunch_booked_product"), arrayListOf("HOTEL"))
        assertEquals(attributesToSend.get("app_open_launch_relaunch_loyalty_tier"), "GOLD")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_last_location"), "100.1, 75.2")
        assertEquals(attributesToSend.get("app_open_launch_relaunch_notification_type"), arrayListOf("MKTG", "SERV", "PROMO"))
        assertEquals(attributesToSend.get("app_open_launch_relaunch_pos"), posUrl)
    }

    @Test
    fun testSetUserInfo() {
        this.setUserInfo("123456", "Tester@Test.com")

        assertEquals(userIdToLog, "123456")
        assertEquals(userEmailToLog, "Tester@Test.com")
    }

    @Test
    fun testClearUserInfo() {
        this.clearUserInfo()

        assertEquals(userIdToLog, null)
        assertEquals(userEmailToLog, null)
    }

    @Test
    fun customMessageListenerListens() {
        val listener = TestableCarnivalMessageListener()
        val bundle = Bundle()
        bundle.putString("title", "Custom Message Title")
        bundle.putString("alert", "Custom Body Message")
        bundle.putString("deeplink", "expda://flightSearch")

        listener.onMessageReceived(context, bundle, null)

        val intent = getIntent(listener.pendingIntent as PendingIntent)

        assertEquals("Custom Message Title", intent.extras.getString(CustomCarnivalListener.KEY_PAYLOAD_TITLE))
        assertEquals("expda://flightSearch", intent.extras.getString(CustomCarnivalListener.KEY_PAYLOAD_DEEPLINK))
        assertEquals("Custom Body Message", intent.extras.getString(CustomCarnivalListener.KEY_PAYLOAD_ALERT))
        assertEquals("expda://flightSearch", intent.data.toString())
    }

    @Test
    fun customMessageListenerListensWithNoDeeplinkProvided() {
        val listener = TestableCarnivalMessageListener()
        val bundle = Bundle()
        bundle.putString("title", "Custom Message Title")
        bundle.putString("alert", "Custom Body Message")

        listener.onMessageReceived(context, bundle, null)

        val intent = getIntent(listener.pendingIntent as PendingIntent)

        assertEquals("Custom Message Title", intent.extras.getString(CustomCarnivalListener.KEY_PAYLOAD_TITLE))
        assertEquals("Custom Body Message", intent.extras.getString(CustomCarnivalListener.KEY_PAYLOAD_ALERT))
        assertEquals("expda://home", intent.data.toString())
    }

    @Test
    fun customMessageListenerRequiresProviderKey() {
        val listener = TestableCarnivalMessageListener()
        val bundle = Bundle()
        bundle.putString("title", "Custom Message Title")
        bundle.putString("alert", "Custom Body Message")
        bundle.putString("provider", "carnival")

        assertEquals(listener.isNotificationFromCarnival(bundle), true)
    }

    @Test
    fun customAttributesWereSavedCorrectly() {
        val mockAttributes = getMockCarnivalAttributesAsJsonObject()
        persistenceProvider.put(mockAttributes)
        assertEquals(getMockCarnivalAttributesAsHashMap().toString(), persistenceProvider.getStoredAttributes().toString())
    }

    @Test
    fun intAttributesWereSavedCorrectly() {
        val sampleStringKey = "String_Key"
        val sampleInt = 86
        val mockAttributeMap = AttributeMap()

        mockAttributeMap.putInt(sampleStringKey, sampleInt)
        persistenceProvider.put(mockAttributeMap)
        val storedIntValue = persistenceProvider.get(sampleStringKey)

        assertEquals(sampleInt, storedIntValue.toString().toInt())
    }

    @Test
    fun booleanAttributesWereSavedCorrectly() {
        val sampleStringKey = "String_Key"
        val sampleBoolean = false
        val mockAttributeMap = AttributeMap()

        mockAttributeMap.putBoolean(sampleStringKey, sampleBoolean)
        persistenceProvider.put(mockAttributeMap)

        assertEquals(sampleBoolean, persistenceProvider.get(sampleStringKey).toString().toBoolean())
    }

    @Test
    fun stringAttributesWereSavedCorrectly() {
        val sampleStringKey = "String_Key"
        val sampleString = "This is a sample string"
        val mockAttributeMap = AttributeMap()

        mockAttributeMap.putString(sampleStringKey, sampleString)
        persistenceProvider.put(mockAttributeMap)

        assertEquals(sampleString, persistenceProvider.get(sampleStringKey))
    }

    @Test
    fun stringArrayListAttributesWereSavedCorrectly() {
        val sampleStringKey = "String_Key"
        val sampleArrayList = arrayListOf(
                CarnivalNotificationTypeConstants.MKTG,
                CarnivalNotificationTypeConstants.SERV,
                CarnivalNotificationTypeConstants.PROMO)
        val mockAttributeMap = AttributeMap()

        mockAttributeMap.putStringArray(sampleStringKey, sampleArrayList)
        persistenceProvider.put(mockAttributeMap)

        assertEquals(sampleArrayList.toString(), persistenceProvider.get(sampleStringKey))
    }

    @Test
    fun dateAttributesWereSavedCorrectly() {
        val sampleStringKey = "String_Key"

        val sampleDate = LocalDate.now().toDate()
        val mockAttributeMap = AttributeMap()

        mockAttributeMap.putDate(sampleStringKey, sampleDate)
        persistenceProvider.put(mockAttributeMap)
        assertEquals(sampleDate.toString(), persistenceProvider.get(sampleStringKey))
    }

    @Test
    fun pushNotificationMarketingCodeIsTrackedInOmniture() {
        val marketingCode = "OLA.EXPEDIA-US-MARKETING-CODE"
        val deeplink = Uri.parse("expda://hotelSearch?olacid=OLA.EXPEDIA-US-OLACID")
        val bundle = Bundle()
        bundle.putString(CustomCarnivalListener.KEY_PAYLOAD_MARKETING, marketingCode)

        this.trackCarnivalPush(context, deeplink, bundle)

        OmnitureTestUtils.assertStateTracked("App.Carnival.Push.Notification", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(10 to marketingCode)),
                OmnitureMatchers.withEvars(mapOf(11 to marketingCode))), mockAnalyticsProvider)
    }

    @Test
    fun pushNotificationOLAcidIsTrackedInOmniture() {
        val marketingCode = ""
        val deeplink = Uri.parse("expda://hotelSearch?olacid=OLA.EXPEDIA-US-OLACID")
        val bundle = Bundle()
        bundle.putString(CustomCarnivalListener.KEY_PAYLOAD_MARKETING, marketingCode)

        this.trackCarnivalPush(context, deeplink, bundle)

        OmnitureTestUtils.assertStateTracked("App.Carnival.Push.Notification", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(10 to "OLA.EXPEDIA-US-OLACID")),
                OmnitureMatchers.withEvars(mapOf(11 to "OLA.EXPEDIA-US-OLACID"))), mockAnalyticsProvider)
    }

    @Test
    fun parameterizedDeeplinksAreCorrectlyBuilt() {
        val mockAttributes = AttributeMap()
        val mockUri = Uri.parse("expda://hotelSearch?location={{search_hotel_destination}}&checkInDate={{search_hotel_check-in_date}}")

        mockAttributes.putString(CarnivalConstants.SEARCH_HOTEL_DESTINATION, "Disney World")
        mockAttributes.putDate(CarnivalConstants.SEARCH_HOTEL_CHECK_IN_DATE, LocalDate(2018, 1, 30).toDate())
        persistenceProvider.put(mockAttributes)

        val parameterizedUri = this.createParameterizedDeeplinkWithStoredValues(mockUri)
        val expectedUri = Uri.parse("expda://hotelSearch?location=Disney World&checkInDate=2018-01-30")

        assertEquals(expectedUri, parameterizedUri)
    }

    private fun getIntent(pendingIntent: PendingIntent): Intent {
        return (Shadow.extract(pendingIntent) as ShadowPendingIntent).savedIntent
    }

    private fun reset() {
        eventNameToLog = ""
        attributesToSend = AttributeMap()
    }

    override fun setAttributes(attributes: AttributeMap, eventName: String) {
        //Don't actually send anything up to carnival
        eventNameToLog = eventName
        attributesToSend = attributes
        persistenceProvider.put(attributes)
    }

    override fun setUserInfo(userId: String?, userEmail: String?) {
        //Don't actually set these user values on carnival
        userIdToLog = userId
        userEmailToLog = userEmail
    }

    override fun buildDialog(carnivalMessage: CarnivalMessage) {
        //don't actually build anything since the sdk message is not accessible.
    }

    private fun getMockCarnivalAttributesAsJsonObject(): AttributeMap {
        val mockAttributes = AttributeMap()

        mockAttributes.putBoolean(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED, true)
        mockAttributes.putString(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL, "Tester@Test.com")
        mockAttributes.putBoolean(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN, true)
        mockAttributes.putStringArray(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT, arrayListOf("HOTEL"))
        mockAttributes.putString(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER, "GOLD")
        mockAttributes.putString(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION, "100.1, 75.2")
        mockAttributes.putStringArray(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE, arrayListOf(
                CarnivalNotificationTypeConstants.MKTG,
                CarnivalNotificationTypeConstants.SERV,
                CarnivalNotificationTypeConstants.PROMO))
        mockAttributes.putString(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_POS, "expedia.fr")

        return mockAttributes
    }

    private fun getMockCarnivalAttributesAsHashMap(): HashMap<String, Any> {
        val mockAttributes = HashMap<String, Any>()

        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED, true)
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL, "Tester@Test.com")
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN, true)
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT, arrayListOf("HOTEL"))
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER, "GOLD")
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION, "100.1, 75.2")
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE, arrayListOf(
                CarnivalNotificationTypeConstants.MKTG,
                CarnivalNotificationTypeConstants.SERV,
                CarnivalNotificationTypeConstants.PROMO))
        mockAttributes.put(CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_POS, "expedia.fr")

        return mockAttributes
    }
}

class TestableCarnivalMessageListener : CarnivalUtils.CustomCarnivalListener() {
    var pendingIntent: PendingIntent? = null

    override fun onMessageReceived(context: Context, bundle: Bundle, message: Message?): Boolean {
        pendingIntent = this.createPendingIntent(context, bundle, bundle.getString(KEY_PAYLOAD_DEEPLINK))
        return true
    }
}
