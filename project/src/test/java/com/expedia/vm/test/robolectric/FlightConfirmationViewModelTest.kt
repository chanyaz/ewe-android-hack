package com.expedia.vm.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.flights.KrazyglueSearchParams
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.server.DateTimeParser
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.FlightPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.flights.FlightConfirmationViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightConfirmationViewModelTest {
    val customerEmail = "fakeEmail@mobiata.com"
    private var vm: FlightConfirmationViewModel by Delegates.notNull()
    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()
    private val testArrivalDateTimeTomorrow = DateTime.now().plusDays(1).toString()
    private val testReturnDateFiveDaysAhead = DateTime.now().plusDays(5).toString()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultFlightComponents()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        shadowApplication = ShadowApplication.getInstance()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun flightConfirmationViewModelTest() {
        val expiresInFuture = DateTime.now().plusDays(50).toString()
        val response = getCheckoutResponse(expiresInFuture)
        val destination = "Detroit"
        val userPoints = "100"

        val destinationTestSubscriber= TestSubscriber<String>()
        val itinNumberTestSubscriber = TestSubscriber<String>()
        val expediaPointsSubscriber = TestSubscriber<String>()
        val crossSellWidgetView = TestSubscriber<Boolean>()

        vm = FlightConfirmationViewModel(activity)
        vm.destinationObservable.subscribe(destinationTestSubscriber)
        vm.itinNumberMessageObservable.subscribe(itinNumberTestSubscriber)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.crossSellWidgetVisibility.subscribe(crossSellWidgetView)
        vm.destinationObservable.onNext(destination)
        vm.setRewardsPoints.onNext(Optional(userPoints))
        vm.confirmationObservable.onNext(Pair(response, customerEmail))

        destinationTestSubscriber.assertValue(destination)
        itinNumberTestSubscriber.assertValue("#${response.newTrip!!.itineraryNumber} sent to $customerEmail")
        expediaPointsSubscriber.assertValue("$userPoints points earned")
        crossSellWidgetView.assertValue(true)
    }

    @Test
    fun crossSellNotOfferedTest() {
        val pastExpiration = DateTime.now().minusDays(50).toString()
        val response = getCheckoutResponseWithoutAirAttachOffer(pastExpiration)
        val crossSellWidgetView = TestSubscriber<Boolean>()

        vm = FlightConfirmationViewModel(activity)
        vm.crossSellWidgetVisibility.subscribe(crossSellWidgetView)
        vm.confirmationObservable.onNext(Pair(response, customerEmail))

        crossSellWidgetView.assertValue(false)
    }

    fun getCheckoutResponse(dateOfExpiration: String, totalPrice: Money? = Money("100", "USD"), hasAirAttach: Boolean = true, isRoundTrip: Boolean = false): FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        setFlightLeg(response, isRoundTrip)
        response.passengerDetails = listOf(Traveler("test", "traveler", "1", "9999999", "test@email.com"))
        val qualifierObject = FlightCheckoutResponse.AirAttachInfo()
        val offerTimeField = FlightCheckoutResponse.AirAttachInfo.AirAttachExpirationInfo()

        val field = response.javaClass.getDeclaredField("airAttachInfo")
        field.isAccessible = true

        val boolField = qualifierObject.javaClass.getDeclaredField("hasAirAttach")
        boolField.isAccessible = true

        val timeRemainingField = qualifierObject.javaClass.getDeclaredField("offerExpirationTimes")
        timeRemainingField.isAccessible = true

        val timeField = offerTimeField.javaClass.getDeclaredField("fullExpirationDate")
        timeField.isAccessible = true

        timeField.set(offerTimeField , dateOfExpiration)
        boolField.set(qualifierObject, hasAirAttach)
        timeRemainingField.set(qualifierObject, offerTimeField )

        val priceField = response.javaClass.getDeclaredField("totalChargesPrice")
        priceField.isAccessible = true
        priceField.set(response, totalPrice)
        field.set(response, qualifierObject)

        return response
    }

    private fun setFlightLeg(response: FlightCheckoutResponse, isRoundTrip: Boolean) {
        response.details = FlightTripDetails()
        val arrivalSegment = FlightLeg.FlightSegment()
        arrivalSegment.arrivalAirportCode = "LAX"
        arrivalSegment.arrivalTimeRaw = testArrivalDateTimeTomorrow
        arrivalSegment.departureTimeRaw = testArrivalDateTimeTomorrow
        val departureLeg = FlightLeg()
        departureLeg.segments = listOf(arrivalSegment)
        if (isRoundTrip) {
            val departureSegment = FlightLeg.FlightSegment()
            departureSegment.departureAirportCode = "SFO"
            departureSegment.departureTimeRaw = testReturnDateFiveDaysAhead
            val returnLeg = FlightLeg()
            returnLeg.segments = listOf(departureSegment)
            response.details.legs = listOf(departureLeg, returnLeg)
        } else {
            response.details.legs = listOf(departureLeg)
        }
    }

    fun getCheckoutResponseWithoutAirAttachOffer(dateOfExpiration: String) : FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        val qualifierObject = FlightCheckoutResponse.AirAttachInfo()

        val field = response.javaClass.getDeclaredField("airAttachInfo")
        field.isAccessible = true

        val boolField = qualifierObject.javaClass.getDeclaredField("hasAirAttach")
        boolField.isAccessible = true
        boolField.set(qualifierObject, false)
        field.set(response, qualifierObject)

        return response
    }

    @Test
    fun zeroFlightLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "0"

        vm = FlightConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(Optional(userPoints))

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun nullFlightLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = null

        vm = FlightConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(Optional(userPoints))

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun noShowFlightLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "100"
        vm = FlightConfirmationViewModel(activity)
        //adding test POS configuration without rewards enabled
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_show_rewards_false.json", false)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(Optional(userPoints))

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun testRewardsPointsStringFlights() {
        val rewardsString = TestSubscriber<String>()
        vm = FlightConfirmationViewModel(activity)
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        vm.rewardPointsObservable.subscribe(rewardsString)

        vm.setRewardsPoints.onNext(Optional("100"))
        rewardsString.assertValue("100 points earned")
    }

    @Test
    fun testNumberOfTravelersString() {
        val travelersString = TestSubscriber<String>()
        val numberOfTravelers = 5
        vm = FlightConfirmationViewModel(activity)
        vm.formattedTravelersStringSubject.subscribe(travelersString)

        vm.numberOfTravelersSubject.onNext(numberOfTravelers)
        travelersString.assertValue("5 travelers")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTripTotalPriceStringWhenToggled() {
        val priceString = TestSubscriber<String>()
        vm = FlightConfirmationViewModel(activity)
        vm.tripTotalPriceSubject.subscribe(priceString)
        vm.confirmationObservable.onNext(Pair(getCheckoutResponse(DateTime.now().toString(), null), customerEmail))

        vm.confirmationObservable.onNext(Pair(getCheckoutResponse(DateTime.now().toString()), customerEmail))
        priceString.assertValues("", "$100")
    }

    @Test
    fun testTripProtectionStringNotVisible() {
        val showTripProtection = TestSubscriber<Boolean>()
        val checkoutResponse = getCheckoutResponse(DateTime.now().toString())
        vm = FlightConfirmationViewModel(activity)

        vm.showTripProtectionMessage.subscribe(showTripProtection)
        vm.confirmationObservable.onNext(Pair(checkoutResponse, customerEmail))

        showTripProtection.assertValues(false, false)
    }

    @Test
    fun testTripProtectionStringVisible() {
        val showTripProtection = TestSubscriber<Boolean>()
        val checkoutResponse = getCheckoutResponse(DateTime.now().toString())
        setUpInsuranceProductInResponse(checkoutResponse)

        vm = FlightConfirmationViewModel(activity)

        vm.showTripProtectionMessage.subscribe(showTripProtection)
        vm.confirmationObservable.onNext(Pair(checkoutResponse, customerEmail))

        showTripProtection.assertValues(false, true)
    }

    @Test
    fun testKrazyglueParamsReturnDateForRoundTrip() {
        vm = FlightConfirmationViewModel(activity)
        val checkoutResponse = getCheckoutResponse(DateTime.now().toString(), totalPrice = Money(100, "$"), hasAirAttach = false, isRoundTrip = true)
        val testKrazyglueParams = vm.getKrazyglueSearchParams(checkoutResponse, FlightPresenterTestUtil.getFlightSearchParams(isRoundTrip = true))

        assertKrazyglueParams(expectedReturnDateTime = testReturnDateFiveDaysAhead, testKrazyglueSearchParams = testKrazyglueParams)
    }

    @Test
    fun testKrazyglueParamsReturnDateOneWay() {
        vm = FlightConfirmationViewModel(activity)
        val testKrazyglueParams = vm.getKrazyglueSearchParams(getCheckoutResponse(DateTime.now().toString()), FlightPresenterTestUtil.getFlightSearchParams(isRoundTrip = false))
        val testReturnDateOneDayAfterArrival = DateTimeParser.parseISO8601DateTimeString(testArrivalDateTimeTomorrow).plusDays(1).toString()

        assertKrazyglueParams(expectedReturnDateTime = testReturnDateOneDayAfterArrival, testKrazyglueSearchParams = testKrazyglueParams)
    }

    @Test
    fun testKrazyglueParamsWithNoChildTraveler() {
        vm = FlightConfirmationViewModel(activity)
        val testKrazyglueParams = vm.getKrazyglueSearchParams(getCheckoutResponse(DateTime.now().toString()), FlightPresenterTestUtil.getFlightSearchParams(isRoundTrip = false, includeChild = false))
        val testReturnDateOneDayAfterArrival = DateTimeParser.parseISO8601DateTimeString(testArrivalDateTimeTomorrow).plusDays(1).toString()

        assertKrazyglueParams(expectedReturnDateTime = testReturnDateOneDayAfterArrival, testKrazyglueSearchParams = testKrazyglueParams, withChild = false)
    }

    @Test
    fun testFlightSearchParamsBecomeHotelSearchParamsForKrazyglue() {
        bucketViewmodelIntoKrazyglue()
        val hotelSearchParamsTestSubscriber = TestSubscriber<HotelSearchParams>()
        vm.krazyGlueHotelSearchParamsObservable.subscribe(hotelSearchParamsTestSubscriber)
        vm.flightSearchParamsObservable.onNext(FlightPresenterTestUtil.getFlightSearchParams(isRoundTrip = true))

        hotelSearchParamsTestSubscriber.assertNoValues()
        vm.flightCheckoutResponseObservable.onNext(getCheckoutResponse(DateTime.now().toString()))

        hotelSearchParamsTestSubscriber.assertValueCount(1)
        assertTrue(hotelSearchParamsTestSubscriber.onNextEvents[0] is HotelSearchParams)
    }

    @Test
    fun testRegionIdParsedFromDeeplinkUrl() {
        bucketViewmodelIntoKrazyglue()
        val regionIdTestSubscriber = TestSubscriber<String>()
        vm.krazyGlueRegionIdObservable.subscribe(regionIdTestSubscriber)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = true))

        regionIdTestSubscriber.assertValue("178276")
    }

    @Test
    fun testAirAttachVisibilityWithKrazyglueTurnedOn() {
        bucketViewmodelIntoKrazyglue()
        val checkoutResponse = getCheckoutResponse(DateTime.now().toString())
        vm = FlightConfirmationViewModel(activity)
        vm.confirmationObservable.onNext(Pair(checkoutResponse, customerEmail))

        assertFalse(vm.crossSellWidgetVisibility.value)
    }

    @Test
    fun testAirAttachVisibilityWithKrazyglueTurnedOff() {
        val checkoutResponse = getCheckoutResponse(DateTime.now().toString(), hasAirAttach = true)

        vm = FlightConfirmationViewModel(activity)
        vm.confirmationObservable.onNext(Pair(checkoutResponse, customerEmail))

        assertTrue(vm.crossSellWidgetVisibility.value)
    }

    @Test
    fun testSignedKrazyglueUrl() {
        vm = FlightConfirmationViewModel(activity)
        var successfulUrl = "/xsell-api/1.0/offers?partnerId=expedia-hot-mobile-conf&outboundEndDateTime=2020-10-10T00:02:06.401Z&returnStartDateTime=2020-10-11T00:02:06.401Z&destinationTla=LAS&numOfAdults=1&numOfChildren=2&childAges=1,2&fencedResponse=true&signature=o998P5_hq9fHqVepvS4bTDkqxlI"
        var krazyglueParams = KrazyglueSearchParams("LAS", "2020-10-10T00:02:06.401Z", "2020-10-11T00:02:06.401Z", 1, 2, listOf(1, 2))

        assertEquals(successfulUrl, vm.getSignedKrazyglueUrl(krazyglueParams))

        successfulUrl = "/xsell-api/1.0/offers?partnerId=expedia-hot-mobile-conf&outboundEndDateTime=2020-10-10T00:02:06.401Z&returnStartDateTime=2020-10-11T00:02:06.401Z&destinationTla=LAS&numOfAdults=1&numOfChildren=1&childAges=1&fencedResponse=true&signature=xjRBmbyalbKYHilFBpMxMAJYIvI"
        krazyglueParams = KrazyglueSearchParams("LAS", "2020-10-10T00:02:06.401Z", "2020-10-11T00:02:06.401Z", 1, 1, listOf(1))

        assertEquals(successfulUrl, vm.getSignedKrazyglueUrl(krazyglueParams))
    }

    @Test
    fun testSuccessfulKrazyglueResponseMaintainsHiddenCrossSell() {
        bucketViewmodelIntoKrazyglue()
        val crossSellVisibilityTestSubscriber = TestSubscriber<Boolean>()
        vm.crossSellWidgetVisibility.subscribe(crossSellVisibilityTestSubscriber)
        val crossSellEligibleCheckoutResponse = getCheckoutResponse(DateTime.now().toString(), totalPrice = Money(100, "$"), hasAirAttach = false, isRoundTrip = true)
        vm.flightCheckoutResponseObservable.onNext(crossSellEligibleCheckoutResponse)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = true))

        crossSellVisibilityTestSubscriber.assertValue(false)
    }

    @Test
    fun testFailedKrazyGlueResponseShowsCrossSellWhenEligible() {
        bucketViewmodelIntoKrazyglue()
        val crossSellVisibilityTestSubscriber = TestSubscriber<Boolean>()
        vm.crossSellWidgetVisibility.subscribe(crossSellVisibilityTestSubscriber)
        val crossSellEligibleCheckoutResponse = getCheckoutResponse(DateTime.now().toString(), totalPrice = Money(100, "$"), hasAirAttach = true, isRoundTrip = true)
        vm.flightCheckoutResponseObservable.onNext(crossSellEligibleCheckoutResponse)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = false))

        crossSellVisibilityTestSubscriber.assertValue(true)
    }

    @Test
    fun testFailedKrazyGlueResponseHidesCrossSellAndKrazyglueWhenNotEligible() {
        bucketViewmodelIntoKrazyglue()
        val crossSellVisibilityTestSubscriber = TestSubscriber<Boolean>()
        vm.crossSellWidgetVisibility.subscribe(crossSellVisibilityTestSubscriber)
        val crossSellNotEligibleCheckoutResponse = getCheckoutResponse(DateTime.now().toString(), totalPrice = Money(100, "$"), hasAirAttach = false, isRoundTrip = true)
        vm.flightCheckoutResponseObservable.onNext(crossSellNotEligibleCheckoutResponse)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = false))

        crossSellVisibilityTestSubscriber.assertValue(false)
    }

    @Test
    fun testSuccessfulKrazyglueResponse() {
        bucketViewmodelIntoKrazyglue()
        val regionIdTestSubscriber = TestSubscriber<String>()
        val krazyglueHotelsTestSubscriber = TestSubscriber<List<KrazyglueResponse.KrazyglueHotel>>()
        vm.krazyGlueRegionIdObservable.subscribe(regionIdTestSubscriber)
        vm.krazyglueHotelsObservable.subscribe(krazyglueHotelsTestSubscriber)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = true))

        regionIdTestSubscriber.assertValue("178276")
        assertEquals(3, krazyglueHotelsTestSubscriber.onNextEvents[0].size)
    }

    @Test
    fun testFailedKrazyglueResponse() {
        bucketViewmodelIntoKrazyglue()
        val regionIdTestSubscriber = TestSubscriber<String>()
        val krazyglueHotelsTestSubscriber = TestSubscriber<List<KrazyglueResponse.KrazyglueHotel>>()
        vm.krazyGlueRegionIdObservable.subscribe(regionIdTestSubscriber)
        vm.krazyglueHotelsObservable.subscribe(krazyglueHotelsTestSubscriber)
        vm.getKrazyglueResponseObserver().onNext(FlightPresenterTestUtil.getKrazyglueResponse(isSuccessful = false))

        regionIdTestSubscriber.assertNoValues()
        krazyglueHotelsTestSubscriber.assertValue(emptyList())
    }

    private fun setUpInsuranceProductInResponse(checkoutResponse: FlightCheckoutResponse) {
        val flightAggregatedResponse = FlightCheckoutResponse.FlightAggregatedResponse()
        val list = ArrayList<FlightTripDetails>()
        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.selectedInsuranceProduct = InsuranceProduct()
        val tripDetail = FlightTripDetails()
        tripDetail.offer = flightOffer
        list.add(tripDetail)
        flightAggregatedResponse.flightsDetailResponse = list
        checkoutResponse.flightAggregatedResponse = flightAggregatedResponse
    }

    private fun bucketViewmodelIntoKrazyglue() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsKrazyglue)
        vm = FlightConfirmationViewModel(activity)
    }

    fun assertKrazyglueParams(expectedReturnDateTime: String, testKrazyglueSearchParams: KrazyglueSearchParams, withChild: Boolean = true) {
        assertEquals(expectedReturnDateTime, testKrazyglueSearchParams.returnDateTime)
        assertEquals(testArrivalDateTimeTomorrow, testKrazyglueSearchParams.arrivalDateTime)
        assertEquals("LAX", testKrazyglueSearchParams.destinationCode)
        assertEquals("99e4957f-c45f-4f90-993f-329b32e53ca1", testKrazyglueSearchParams.apiKey)
        assertEquals("/xsell-api/1.0/offers", testKrazyglueSearchParams.baseUrl)
        assertEquals(2, testKrazyglueSearchParams.numOfAdults)
        if (withChild) {
            assertEquals(1, testKrazyglueSearchParams.numOfChildren)
            assertEquals(listOf(4), testKrazyglueSearchParams.childAges)
        } else {
            assertEquals(0, testKrazyglueSearchParams.numOfChildren)
            assertEquals(emptyList(), testKrazyglueSearchParams.childAges)
        }
    }
}
