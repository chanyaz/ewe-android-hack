package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.FlightTravelersViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightCheckoutPresenterTest {


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
    fun testPassportRequired() {
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getPassportRequiredCreateTripResponse(true))
        passportRequiredSubscriber.assertValues(false, true)
    }

    @Test
    fun testPassportNotRequired() {
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getPassportRequiredCreateTripResponse(false))
        passportRequiredSubscriber.assertValues(false, false)
    }

    private fun getPassportRequiredCreateTripResponse(passportRequired: Boolean): FlightCreateTripResponse? {
        val flightCreateTripResponse = FlightCreateTripResponse()
        val flightTripDetails = FlightTripDetails()

        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.isPassportNeeded = passportRequired
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = Money(9, "USD")

        flightTripDetails.offer = flightOffer

        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.newTrip = TripDetails("", "", "")
        flightCreateTripResponse.tealeafTransactionId = ""

        return flightCreateTripResponse
    }
}
