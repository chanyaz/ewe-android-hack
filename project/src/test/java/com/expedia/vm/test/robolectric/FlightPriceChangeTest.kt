package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.traveler.FlightTravelersViewModel
import org.joda.time.LocalDate
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
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class FlightPriceChangeTest {


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
    fun testCreateTripPriceChange() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        overview.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        val dummyFlightTripResposnse = getDummyFlightCreateTripPriceChangeResponse(9.0, 10.0)
        val flightTripItem = TripBucketItemFlightV2(dummyFlightTripResposnse )
        Db.getTripBucket().add(flightTripItem)
        
        checkout.getCreateTripViewModel().createTripResponseObservable.onNext(dummyFlightTripResposnse )
        priceChangeSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValue(true)
    }

    @Test
    fun testCreateTripPriceAlert() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsCreateTripPriceChangeAlert)
        val priceChangeAlertPriceSubscriber = TestSubscriber<TripResponse>()
        val showPriceChangeAlertSubscriber = TestSubscriber<Boolean>()
        val dummyFlightTripResponse = getDummyFlightCreateTripPriceChangeResponse(9.01, 10.01)
        val flightTripItem = TripBucketItemFlightV2(dummyFlightTripResponse)
        Db.getTripBucket().add(flightTripItem)

        overview.resetAndShowTotalPriceWidget()
        checkout.flightCreateTripViewModel.priceChangeAlertPriceObservable.subscribe(priceChangeAlertPriceSubscriber)
        checkout.flightCreateTripViewModel.showPriceChangeAlertObservable.subscribe(showPriceChangeAlertSubscriber)

        //Verify multiple createTripResponses just lead to one alert
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(dummyFlightTripResponse)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(dummyFlightTripResponse)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(dummyFlightTripResponse)

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
        val dummyFlightCreateTripResponse = getDummyFlightCreateTripPriceChangeResponse(9.01, 10.0)
        val flightTripItem = TripBucketItemFlightV2(dummyFlightCreateTripResponse)
        Db.getTripBucket().add(flightTripItem)

        overview.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(dummyFlightCreateTripResponse)
        priceChangeSubscriber.assertValueCount(0)
    }

    @Test
    fun testCheckoutPriceChange() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        overview.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        checkout.flightCheckoutViewModel.checkoutPriceChangeObservable.onNext(getDummyFlightCheckoutResponse())
        priceChangeSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValue(true)
    }

    @Test
    fun testCheckoutPriceChangeDoesNotCrashWithoutOldOffer() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        val oldMoneySubscriber = TestSubscriber<Money>()
        val priceChangeTextSubscriber = TestSubscriber<String>()
        overview.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        overview.priceChangeWidget.viewmodel.originalPrice.subscribe(oldMoneySubscriber)
        overview.priceChangeWidget.viewmodel.priceChangeText.subscribe(priceChangeTextSubscriber)

        val responseWithoutOldOffer = getDummyFlightCheckoutResponse()
        responseWithoutOldOffer?.details?.oldOffer = null
        checkout.flightCheckoutViewModel.checkoutPriceChangeObservable.onNext(responseWithoutOldOffer)

        priceChangeSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValue(true)
        assertNull(oldMoneySubscriber.onNextEvents[0])
        priceChangeTextSubscriber.assertNoValues()
    }

    @Test
    fun testCreateTripPriceChangeDoesNotCrashWithoutOldOffer() {
        val priceChangeSubscriber = TestSubscriber<Boolean>()
        val oldMoneySubscriber = TestSubscriber<Money>()
        val priceChangeTextSubscriber = TestSubscriber<String>()
        overview.priceChangeWidget.viewmodel.priceChangeVisibility.subscribe(priceChangeSubscriber)
        overview.priceChangeWidget.viewmodel.originalPrice.subscribe(oldMoneySubscriber)
        overview.priceChangeWidget.viewmodel.priceChangeText.subscribe(priceChangeTextSubscriber)

        val responseWithoutOldOffer = getDummyFlightCreateTripPriceChangeResponse(9.0, 10.0)
        val flightTripItem = TripBucketItemFlightV2(responseWithoutOldOffer)
        Db.getTripBucket().add(flightTripItem)
        responseWithoutOldOffer?.details?.oldOffer = null
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(responseWithoutOldOffer)

        priceChangeSubscriber.assertNoValues()
        assertNull(oldMoneySubscriber.onNextEvents[0])
        priceChangeTextSubscriber.assertNoValues()
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
        flightTripDetails.legs = setupFlightLeg()
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = newMoney
        flightTripDetails.offer = flightOffer

        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.newTrip = TripDetails("","","")
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
