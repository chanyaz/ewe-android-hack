package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getCheckoutResponse
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightAggregatedResponse
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightCreateTripResponse
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightTripDetails
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AbacusTestUtils
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.io.BufferedReader
import java.io.FileReader
import org.mockito.Mockito.`when` as whenever

@RunWith(RobolectricRunner::class)
class OmnitureTrackingFlightTest {
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    private val context: Context by lazy {
        RuntimeEnvironment.application
    }

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        setupDb()
    }

    @Test
    fun testTrackFlightCheckoutInfoPageLoadEvents() {
        val mockResponse = getCreateTripMockResponse()

        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEventsString("event36,event71"), mockAnalyticsProvider,
                "FAILED: Expected event36 (checkout start), event71 (flight checkout start)")
    }

    @Test
    fun testTrackFlightCheckoutInfoPageLoadEventsWithInsurance() {
        val mockResponse = getCreateTripMockResponse(withInsuranceProduct = true)

        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEventsString("event36,event71,event122"), mockAnalyticsProvider,
                "FAILED: Expected event36 (checkout start), event71 (flight checkout start), and event 122 (insurance present)")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackUrgencyMessagingABTestOnCheckout() {
        val mockResponse = getCreateTripMockResponse()

        val abTest = ABTest(25037, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest)
        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(34 to "25037.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDontTrackUrgencyMessagingABTestOnCheckout() {
        val mockResponse = getCreateTripMockResponse()

        val abTest = ABTest(25037, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest, 0)
        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(34 to "25037.0.0")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackWebFlightConfirmationWithInsurance() {
        val (pageUsableData, flightTripDetails) = getFlightItinTripResponse()
        OmnitureTracking.trackWebFlightCheckoutConfirmation(flightTripDetails, pageUsableData)
        val expectedEvars = mapOf(18 to "App.Flight.Checkout.Confirmation",
                50 to "app.phone.android")
        val expectedProps = mapOf(2 to "Flight",
                3 to "SFO",
                4 to "DTW",
                8 to "5678|1234",
                71 to "5678",
                72 to "")
        val expectedProducts = ";Flight:SFO:Merchant;1;63.20;;eVar30=Merchant:FLT:SFO-DTW:20180524-nil,;Insurance:100001;1;19.00"
        val expectedEvents = "purchase,event220,event221=0.00"

        val appState = "App.Flight.Checkout.Confirmation"
        assertWebFlightConfirmationStateTracked(appState, expectedEvars, expectedProps, expectedProducts, expectedEvents)
    }

    @Test
    fun testTrackWebFlightConfirmationRoundTripDiffAirports() {
        val (pageUsableData, flightTripDetails) = getFlightItinTripResponse("../lib/mocked/templates/api/trips/itin_trip_flight_roundtrip_diff_airports.json")
        OmnitureTracking.trackWebFlightCheckoutConfirmation(flightTripDetails, pageUsableData)
        val expectedEvars = mapOf(18 to "App.Flight.Checkout.Confirmation",
                50 to "app.phone.android")
        val expectedProps = mapOf(2 to "Flight",
                3 to "SFO",
                4 to "DTW",
                8 to "5678|1234",
                71 to "5678",
                72 to "")
        val expectedProducts = ";Flight:SFO:Merchant;1;121.40;;eVar30=Merchant:FLT:SFO-DTW:20180524-nil"
        val expectedEvents = "purchase,event220,event221=0.00"

        val appState = "App.Flight.Checkout.Confirmation"
        assertWebFlightConfirmationStateTracked(appState, expectedEvars, expectedProps, expectedProducts, expectedEvents)
    }

    @Test
    fun testTrackWebFlightConfirmationUsingItinDetailRoundTrip() {
        val (pageUsableData, flightTripDetails) = getFlightItinTripResponse("../lib/mocked/templates/api/trips/flight_trip_details.json")
        OmnitureTracking.trackWebFlightCheckoutConfirmation(flightTripDetails, pageUsableData)

        val expectedEvars = mapOf(18 to "App.Flight.Checkout.Confirmation",
                50 to "app.phone.android")
        val expectedProps = mapOf(2 to "Flight",
                3 to "SFO",
                4 to "DTW",
                8 to "5678|1234",
                71 to "5678",
                72 to "")
        val expectedProducts = ";Flight:SFO:Merchant;1;60.20;;eVar30=Merchant:FLT:SFO-DTW:20180524-nil,;Insurance:null;"
        val expectedEvents = "purchase,event220,event221=0.00"

        val appState = "App.Flight.Checkout.Confirmation"
        assertWebFlightConfirmationStateTracked(appState, expectedEvars, expectedProps, expectedProducts, expectedEvents)
    }

    @Test
    fun testTrackWebFlightConfirmationTracksHiddenConfirmationXTestBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppConfirmationToolbarXHidden)

        val (pageUsableData, flightTripDetails) = getFlightItinTripResponse()
        OmnitureTracking.trackWebFlightCheckoutConfirmation(flightTripDetails, pageUsableData)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppConfirmationToolbarXHidden.key), mockAnalyticsProvider)
    }

    @Test
    fun testTrackWebFlightConfirmationTracksHiddenConfirmationXTestControl() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppConfirmationToolbarXHidden, AbacusVariant.CONTROL.value)

        val (pageUsableData, flightTripDetails) = getFlightItinTripResponse()
        OmnitureTracking.trackWebFlightCheckoutConfirmation(flightTripDetails, pageUsableData)

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppConfirmationToolbarXHidden.key), mockAnalyticsProvider)
    }

    private fun getFlightItinTripResponse(fileName: String = "../lib/mocked/templates/api/trips/flight_trip_with_insurance.json"): Pair<PageUsableData, FlightItinDetailsResponse> {
        val pageUsableData = PageUsableData()
        pageUsableData.markPageLoadStarted(10000)
        pageUsableData.markAllViewsLoaded(10000)

        val br = BufferedReader(FileReader(fileName))
        val flightTripDetails = Gson().fromJson(br, FlightItinDetailsResponse::class.java)
        return Pair(pageUsableData, flightTripDetails)
    }

    @Test
    fun testTrackBookingConfirmationDialog() {
        val pageUsableData = PageUsableData()
        pageUsableData.markPageLoadStarted(10000)
        pageUsableData.markAllViewsLoaded(10000)
        OmnitureTracking.trackFlightsBookingConfirmationDialog(pageUsableData)
        val expectedEvars = mapOf(18 to "App.Flight.Checkout.Confirmation.Slim",
                50 to "app.phone.android")

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNativeRateDetailsWebviewCheckoutTestBucketed() {
        val mockResponse = getCreateTripMockResponse()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(34 to "25620.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNativeRateDetailsWebviewCheckoutTestControl() {
        val mockResponse = getCreateTripMockResponse()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout, 0)
        OmnitureTracking.trackFlightCheckoutInfoPageLoad(mockResponse)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withProps(mapOf(34 to "25620.0.0")), mockAnalyticsProvider)
    }

    @Test
    fun testTrackNumberOfTicketsFromAggregatedResponse() {
        val pageUsableData = PageUsableData()
        pageUsableData.markPageLoadStarted(10000)
        pageUsableData.markAllViewsLoaded(10000)

        val checkoutResponse = getCheckoutResponseWithOnlyAggregatedResponseDetails(numberOfTickets = "20")

        Db.getTripBucket().flightV2.flightCheckoutResponse = checkoutResponse

        OmnitureTracking.trackFlightCheckoutConfirmationPageLoad(pageUsableData)

        val insuranceProductWithNumberOfTickets = "Insurance:typeId;20"
        OmnitureTestUtils.assertStateTracked("App.Flight.Checkout.Confirmation", OmnitureMatchers.withProductsString(insuranceProductWithNumberOfTickets, shouldExactlyMatch = false), mockAnalyticsProvider)
    }

    @Test
    fun testTrackUrgencyMessageDisplayedOnCheckout() {
        OmnitureTracking.trackUrgencyMessageDisplayed()
        val controlEvar = mapOf(28 to "App.CKO.Urgency.Shown")
        OmnitureTestUtils.assertLinkTracked("Universal Checkout", "App.CKO.Urgency.Shown", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(isRoundTrip = false, includeChild = false))
        val flightTripItem = TripBucketItemFlightV2(getFlightCreateTripResponse())
        Db.getTripBucket().add(flightTripItem)
    }

    private fun assertWebFlightConfirmationStateTracked(appState: String, expectedEvars: Map<Int, String>, expectedProps: Map<Int, String>, expectedProducts: String, expectedEvents: String) {
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withProps(expectedProps), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withProductsString(expectedProducts, shouldExactlyMatch = false), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withEventsString(expectedEvents), mockAnalyticsProvider)
    }

    private fun getCheckoutResponseWithOnlyAggregatedResponseDetails(numberOfTickets: String): FlightCheckoutResponse {
        val tripDetails = listOf<FlightTripDetails>(getFlightTripDetails(numberOfTickets = numberOfTickets))
        val aggregatedResponse = getFlightAggregatedResponse(listOfFlightDetails = tripDetails)
        val checkoutResponse = getCheckoutResponse(flightAggregatedResponse = aggregatedResponse, hasDetails = false)
        return checkoutResponse
    }

    private fun getCreateTripMockResponse(withInsuranceProduct: Boolean = false): FlightCreateTripResponse {
        val mockResponse = Mockito.mock(FlightCreateTripResponse::class.java)
        val mockOffer = Mockito.mock(FlightTripDetails.FlightOffer::class.java)
        mockOffer.availableInsuranceProducts = if (withInsuranceProduct) listOf(InsuranceProduct()) else emptyList()
        whenever(mockResponse.getOffer()).thenReturn(mockOffer)
        return mockResponse
    }
}
