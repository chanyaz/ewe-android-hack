package com.expedia.vm.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.CheckoutToolbar
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightCheckoutToolbarTest {

    private var checkout: FlightCheckoutPresenter by Delegates.notNull()
    private var overview: FlightOverviewPresenter by Delegates.notNull()
    private var toolbar: CheckoutToolbar by Delegates.notNull()
    private val context = RuntimeEnvironment.application
    lateinit var travelerValidator: TravelerValidator
    lateinit var activity: Activity

    @Before fun before() {
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        travelerValidator = Ui.getApplication(RuntimeEnvironment.application).travelerComponent().travelerValidator()
        setupDb()
        travelerValidator.updateForNewSearch(Db.getFlightSearchParams())
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        overview = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        checkout = overview.getCheckoutPresenter()
        overview.getCheckoutPresenter().getPaymentWidgetViewModel().lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        toolbar = overview.bundleOverviewHeader.toolbar
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testToolbarMenuItemNotShownInCheckoutOverview() {
        assertFalse(toolbar.menuItem.isVisible)
        overview.showCheckout()
        assertFalse(toolbar.menuItem.isVisible)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testToolbarSaysDoneInPaymentDetailsWidget() {
        overview.showCheckout()
        checkout.paymentWidget.cardInfoContainer.performClick()
        checkout.paymentWidget.showPaymentForm(false)

        assertTrue(toolbar.menuItem.isVisible)
        assertEquals("Done", toolbar.menuItem.title)
    }

    @Test
    fun testToolbarReceivesOnDoneClicked() {
        overview.showCheckout()
        val testDoneClickedSubscriber = TestObserver<() -> Unit>()
        toolbar.viewModel.doneClickedMethod.subscribe(testDoneClickedSubscriber)
        checkout.paymentWidget.cardInfoContainer.performClick()
        checkout.paymentWidget.showPaymentForm(false)

        testDoneClickedSubscriber.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testToolbarMenuItemNotShownInPaymentOptionsWidget() {
        setupTempCardToShowPaymentOptions()

        overview.showCheckout()
        checkout.paymentWidget.cardInfoContainer.performClick()
        checkout.paymentWidget.showPaymentForm(false)

        assertFalse(toolbar.menuItem.isVisible)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testToolbarSaysDoneInTravelerEntryWidget() {
        setTravelersInDb(0)

        overview.showCheckout()
        checkout.openTravelerPresenter()

        assertTrue(toolbar.menuItem.isVisible)
        assertEquals("Done", toolbar.menuItem.title)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testToolbarMenuItemNotShownInTravelerPickerWidget() {
        setTravelersInDb(5)

        overview.showCheckout()
        checkout.openTravelerPresenter()

        assertFalse(toolbar.menuItem.isVisible)
    }

    @Test
    fun testBackButtonFocusedWhenShowingPaymentForm() {
        overview.showCheckout()
        checkout.paymentWidget.cardInfoContainer.performClick()
        checkout.paymentWidget.showPaymentForm(false)

        val backButton = toolbar.getChildAt(0)

        assertEquals("Back", backButton.contentDescription)
        assertTrue(backButton.isFocused)
    }

    @Test
    fun testBackButtonFocusedWhenShowingTravelerForm() {
        overview.showCheckout()
        Db.sharedInstance.travelers = listOf(Traveler())
        checkout.travelerSummaryCard.performClick()
        checkout.show(checkout.travelersPresenter)

        val backButton = toolbar.getChildAt(0)

        assertEquals("Back", backButton.contentDescription)
        assertTrue(backButton.isFocused)
    }

    @Test
    fun testBackButtonFocusedWhenShowingTravelerPicker() {
        Db.sharedInstance.travelers = listOf(Traveler(), Traveler())
        overview.showCheckout()
        checkout.travelerSummaryCard.performClick()

        val backButton = toolbar.getChildAt(0)

        assertEquals("Back", backButton.contentDescription)
        assertTrue(backButton.isFocused)
    }

    @Test
    fun testBackButtonFocusedWhenShowingPaymentOptions() {
        setupTempCardToShowPaymentOptions()
        overview.showCheckout()
        checkout.paymentWidget.cardInfoContainer.performClick()
        checkout.paymentWidget.showPaymentForm(false)

        val backButton = toolbar.getChildAt(0)

        assertEquals("Back", backButton.contentDescription)
        assertTrue(backButton.isFocused)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(setupFlightSearchParams())
        val flightTripItem = TripBucketItemFlightV2(getFlightCreateTripResponse())
        Db.getTripBucket().add(flightTripItem)
    }

    private fun setupFlightSearchParams(): FlightSearchParams {
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

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null, null)
    }

    private fun getFlightCreateTripResponse(): FlightCreateTripResponse {
        val flightCreateTripResponse = FlightCreateTripResponse()
        flightCreateTripResponse.tealeafTransactionId = "123456"
        val newTrip = TripDetails("1234", " ", " ")
        flightCreateTripResponse.newTrip = newTrip
        val pricePerPassengerList = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        val passengerInfo = FlightTripDetails().PricePerPassengerCategory()
        passengerInfo.passengerCategory = FlightTripDetails.PassengerCategory.ADULT
        passengerInfo.basePrice = Money(170, "USD")
        passengerInfo.totalPrice = Money(223, "USD")
        passengerInfo.taxesPrice = Money(53, "USD")
        pricePerPassengerList.add(passengerInfo)
        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.totalPrice = Money(223, "USD")
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.legs = ArrayList()
        val flightLeg = FlightLeg()
        flightLeg.segments = ArrayList()
        flightLeg.segments.add(FlightLeg.FlightSegment())
        flightTripDetails.legs.add(flightLeg)
        flightOffer.pricePerPassengerCategory = pricePerPassengerList
        flightTripDetails.offer = flightOffer
        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.totalPriceIncludingFees = Money(223, "USD")
        flightCreateTripResponse.selectedCardFees = Money(0, "USD")

        return flightCreateTripResponse
    }

    private fun setupTempCardToShowPaymentOptions() {
        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123", context)
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = LocalDate(2017, 1, 1)
        info.securityCode = "123"
        Db.sharedInstance.setTemporarilySavedCard(info)
    }

    private fun setTravelersInDb(numOfTravelers: Int) {
        val travelerList = ArrayList<Traveler>()
        for (i in 0..numOfTravelers) {
            val newTraveler = Traveler()
            travelerList.add(newTraveler)
        }
        Db.sharedInstance.setTravelers(travelerList)
    }
}
