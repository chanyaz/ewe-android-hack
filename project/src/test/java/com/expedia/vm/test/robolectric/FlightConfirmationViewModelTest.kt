package com.expedia.vm.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.flights.FlightConfirmationViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightConfirmationViewModelTest {
    val customerEmail = "fakeEmail@mobiata.com"
    private var vm: FlightConfirmationViewModel by Delegates.notNull()
    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()

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

    fun getCheckoutResponse(dateOfExpiration: String, totalPrice: Money? = Money("100", "USD"), hasAirAttach: Boolean = true): FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        setFlightLeg(response)
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

    private fun setFlightLeg(response: FlightCheckoutResponse) {
        response.details = FlightTripDetails()
        val segment = FlightLeg.FlightSegment()
        segment.arrivalAirportCode = "LAX"
        segment.arrivalTimeRaw = DateTime.now().plusDays(1).toString()
        val flightLeg = FlightLeg()
        flightLeg.segments = listOf(segment)
        response.details.legs = listOf(flightLeg)
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
    fun testFlightSearchParamsBecomeHotelSearchParamsForKrazyglue() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)
        SettingUtils.save(activity.applicationContext, R.string.preference_enable_krazy_glue_on_flights_confirmation, true)
        vm = FlightConfirmationViewModel(activity)
        val hotelSearchParamsTestSubscriber = TestSubscriber<HotelSearchParams>()
        vm.krazyGlueHotelSearchParamsObservable.subscribe(hotelSearchParamsTestSubscriber)
        vm.flightSearchParamsObservable.onNext(getFlightSearchParams())

        hotelSearchParamsTestSubscriber.assertNoValues()
        vm.flightCheckoutResponseObservable.onNext(getCheckoutResponse(DateTime.now().toString()))

        hotelSearchParamsTestSubscriber.assertValueCount(1)
        assertTrue(hotelSearchParamsTestSubscriber.onNextEvents[0] is HotelSearchParams)
    }

    @Test
    fun testAirAttachVisibilityWithKrazyglueTurnedOn() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsKrazyglue, 1)
        SettingUtils.save(activity, activity.getString(R.string.preference_enable_krazy_glue_on_flights_confirmation), true)
        vm = FlightConfirmationViewModel(activity)

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

    private fun getFlightSearchParams(): FlightSearchParams {
        val departureSuggestion = SuggestionV4()
        departureSuggestion.gaiaId = "1234"
        val departureRegionNames = SuggestionV4.RegionNames()
        departureRegionNames.displayName = "San Francisco"
        departureRegionNames.shortName = "SFO"
        departureRegionNames.fullName = "SFO - San Francisco"
        departureSuggestion.regionNames = departureRegionNames

        val testDepartureCoordinates = SuggestionV4.LatLng()
        testDepartureCoordinates.lat = 600.5
        testDepartureCoordinates.lng = 300.3
        departureSuggestion.coordinates = testDepartureCoordinates

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalRegionNames.fullName = "LAX - Los Angeles"
        arrivalSuggestion.regionNames = arrivalRegionNames

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(4)
        val checkIn = LocalDate().plusDays(2)
        val checkOut = LocalDate().plusDays(3)

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null)
    }

}