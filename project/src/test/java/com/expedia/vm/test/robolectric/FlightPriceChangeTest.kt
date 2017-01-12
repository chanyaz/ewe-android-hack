package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightPriceChangeTest {


    private var checkout: FlightCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_checkout_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        checkout = activity.findViewById(R.id.flight_checkout_presenter) as FlightCheckoutPresenter
    }

    @Test
    fun testCreateTripPriceChange() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        checkout.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getDummyFlightCreateTripPriceChangeResponse(9.0, 10.0))
        priceChangeSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValue(true)
    }

    @Test
    fun testCreateTripPriceAlert() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsCreateTripPriceChangeAlert)
        val priceChangeAlertPriceSubscriber = TestSubscriber<TripResponse>()
        val showPriceChangeAlertSubscriber = TestSubscriber<Boolean>()

        checkout.resetAndShowTotalPriceWidget()
        checkout.flightCreateTripViewModel.priceChangeAlertPriceObservable.subscribe(priceChangeAlertPriceSubscriber)
        checkout.flightCreateTripViewModel.showPriceChangeAlertObservable.subscribe(showPriceChangeAlertSubscriber)

        //Verify multiple createTripResponses just lead to one alert
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getDummyFlightCreateTripPriceChangeResponse(9.01, 10.01))
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getDummyFlightCreateTripPriceChangeResponse(9.01, 10.01))
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getDummyFlightCreateTripPriceChangeResponse(9.01, 10.01))

        priceChangeAlertPriceSubscriber.assertValueCount(3)
        showPriceChangeAlertSubscriber.assertValueCount(1)
        showPriceChangeAlertSubscriber.assertValue(true)

        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val okButton = alertDialog.findViewById(android.R.id.button1) as Button
        val errorMessage = alertDialog.findViewById(android.R.id.message) as android.widget.TextView
        assertEquals(true, alertDialog.isShowing)
        assert(errorMessage.text.contains("The price of your trip has changed from $10.01 to $9.01. Rates can change frequently. Book now to lock in this price."))
        assertEquals("OK", okButton.text )
    }

    @Test
    fun testCreateTripPriceChangeNotFired() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        checkout.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getDummyFlightCreateTripPriceChangeResponse(9.01, 10.0))
        priceChangeSubscriber.assertValueCount(0)
    }

    @Test
    fun testCheckoutPriceChange() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        checkout.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        checkout.flightCheckoutViewModel.checkoutPriceChangeObservable.onNext(getDummyFlightCheckoutResponse())
        priceChangeSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValue(true)
    }

    private fun getDummyFlightCheckoutResponse(): FlightCheckoutResponse? {
        val flightCheckoutResponse = FlightCheckoutResponse()
        val flightTripDetails = FlightTripDetails()
        val flightOffer = FlightTripDetails.FlightOffer()

        val money = Money(10, "USD")
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = money
        flightTripDetails.offer = flightOffer
        flightCheckoutResponse.details = flightTripDetails

        val oldFlightOffer = FlightTripDetails.FlightOffer()
        val oldMoney = Money(9, "USD")
        oldFlightOffer.totalPrice = oldMoney
        flightTripDetails.oldOffer = oldFlightOffer

        return flightCheckoutResponse
    }

    private fun getDummyFlightCreateTripPriceChangeResponse(newMoney: Double, oldMoney: Double): FlightCreateTripResponse? {
        val flightCreateTripResponse = FlightCreateTripResponse()
        val flightTripDetails = FlightTripDetails()
        val flightOffer = FlightTripDetails.FlightOffer()
        val newMoney = Money(BigDecimal(newMoney), "USD")
        val oldFlightOffer = FlightTripDetails.FlightOffer()
        val oldMoney = Money(BigDecimal(oldMoney), "USD")
        oldFlightOffer.totalPrice = oldMoney
        flightTripDetails.oldOffer = oldFlightOffer
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = newMoney
        flightTripDetails.offer = flightOffer
        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.newTrip = TripDetails("","","")
        flightCreateTripResponse.tealeafTransactionId = ""
        return flightCreateTripResponse
    }

}
