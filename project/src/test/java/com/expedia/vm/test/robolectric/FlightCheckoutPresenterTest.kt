package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.FlightTravelersViewModel
import org.joda.time.LocalDate
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
    private var overview: FlightOverviewPresenter by Delegates.notNull()

    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        overview = activity.findViewById(R.id.flight_overview_presenter) as FlightOverviewPresenter
        checkout = overview.getCheckoutPresenter()
        addFlightSearchParams()
    }

    @Test
    fun testPassportRequired() {
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        val passportIsRequiredTripResponse = getPassportRequiredCreateTripResponse(true)
        val flightTripItem = TripBucketItemFlightV2(passportIsRequiredTripResponse)
        Db.getTripBucket().add(flightTripItem)

        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(passportIsRequiredTripResponse)
        passportRequiredSubscriber.assertValues(false, true)
    }

    @Test
    fun testPassportNotRequired() {
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        val passportNotRequiredTripResponse = getPassportRequiredCreateTripResponse(false)
        val flightTripItem = TripBucketItemFlightV2(passportNotRequiredTripResponse)
        Db.getTripBucket().add(flightTripItem)

        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(passportNotRequiredTripResponse)
        passportRequiredSubscriber.assertValues(false, false)
    }

    private fun getPassportRequiredCreateTripResponse(passportRequired: Boolean): FlightCreateTripResponse? {
        val flightCreateTripResponse = FlightCreateTripResponse()
        val flightTripDetails = FlightTripDetails()
        val oldOffer =  FlightTripDetails.FlightOffer()
        oldOffer.totalPrice = Money(10, "USD")
        val flightOffer = FlightTripDetails.FlightOffer()

        flightOffer.isPassportNeeded = passportRequired
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = Money(9, "USD")

        flightTripDetails.oldOffer = oldOffer
        flightTripDetails.offer = flightOffer
        flightTripDetails.legs = setupFlightLeg()

        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.newTrip = TripDetails("", "", "")
        flightCreateTripResponse.tealeafTransactionId = ""

        return flightCreateTripResponse
    }

    private fun addFlightSearchParams() {
        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams
        Db.setFlightSearchParams(params)
    }

    private fun getFakeSuggestion(airportCode: String) : SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        val country = SuggestionV4.Country()
        country.name = ""
        hierarchyInfo.country = country
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA (SFO-San Francisco Intl.)"
        regionName.displayName = "San Francisco, CA (<B>SFO</B>-San Francisco Intl.)"
        regionName.fullName = "San Francisco, CA, United States (<B>SFO</B>-San Francisco Intl.)"
        suggestion.regionNames = regionName
        return suggestion
    }

    private fun setupFlightLeg() : ArrayList<FlightLeg> {
        val firstLeg = FlightLeg()
        val mockFlightSegment = FlightLeg.FlightSegment()
        firstLeg.segments = arrayListOf<FlightLeg.FlightSegment>()
        firstLeg.segments.add(0, mockFlightSegment)
        val legs = ArrayList<FlightLeg>()
        legs.add(firstLeg)
        return legs
    }
}
