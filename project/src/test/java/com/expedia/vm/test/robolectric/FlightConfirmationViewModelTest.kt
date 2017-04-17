package com.expedia.vm.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightConfirmationViewModel
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
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
        Ui.getApplication(activity).defaultHotelComponents()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        assertTrue(User.isLoggedIn(activity))
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
        vm.setRewardsPoints.onNext(userPoints)
        vm.confirmationObservable.onNext(Pair(response, customerEmail))

        destinationTestSubscriber.assertValue(destination)
        itinNumberTestSubscriber.assertValue("#${response.newTrip!!.itineraryNumber} sent to $customerEmail")
        expediaPointsSubscriber.assertValue("$userPoints Expedia+ Points")
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

    fun getCheckoutResponse(dateOfExpiration: String) : FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
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
        boolField.set(qualifierObject, true)
        timeRemainingField.set(qualifierObject, offerTimeField )
        field.set(response, qualifierObject)

        return response
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
        Assert.assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "0"

        vm = FlightConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun nullFlightLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        Assert.assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = null

        vm = FlightConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun noShowFlightLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        Assert.assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "100"
        vm = FlightConfirmationViewModel(activity)
        //adding test POS configuration without rewards enabled
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_show_rewards_false.json", false)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }


}