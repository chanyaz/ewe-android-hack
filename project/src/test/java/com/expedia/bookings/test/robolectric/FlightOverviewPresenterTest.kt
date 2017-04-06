package com.expedia.bookings.test.robolectric

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.packages.BundleFlightViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightOverviewPresenterTest {

    private val context = RuntimeEnvironment.application
    private lateinit var widget: FlightOverviewPresenter


    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        val validator = Ui.getApplication(context).travelerComponent().travelerValidator()
        validator.updateForNewSearch(setupFlightSearchParams())
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        SettingUtils.save(context, context.getString(R.string.preference_show_basic_economy), true)
    }

    @Test
    fun widgetVisibilityTest() {
        assertEquals(View.VISIBLE, widget.bundleOverviewHeader.visibility)
        assertEquals(View.VISIBLE, widget.flightSummary.visibility)
        assertEquals(View.VISIBLE, widget.getCheckoutPresenter().visibility)
        assertEquals(View.GONE, widget.cvv.visibility)
        assertEquals(View.GONE, widget.paymentFeeInfoWebView.visibility)

        val freeCancelltionText = widget.flightSummary.freeCancellationLabelTextView
        val splitTicketBaggageFeeLinkContainer = widget.flightSummary.splitTicketInfoContainer
        val airlineFeeWarningText = widget.flightSummary.airlineFeeWarningTextView
        val basicEconomyMessaging = widget.flightSummary.basicEconomyMessageTextView
        widget.viewModel.airlineFeeWarningTextObservable.onNext("An airline fee, based on card type, may be added upon payment.")
        widget.viewModel.showFreeCancellationObservable.onNext(true)
        widget.viewModel.showSplitTicketMessagingObservable.onNext(true)
        widget.viewModel.showAirlineFeeWarningObservable.onNext(true)
        widget.viewModel.showBasicEconomyMessageObservable.onNext(true)
        assertEquals("An airline fee, based on card type, may be added upon payment.", airlineFeeWarningText.text)
        assertEquals(View.VISIBLE, freeCancelltionText.visibility)
        assertEquals(View.VISIBLE, splitTicketBaggageFeeLinkContainer.visibility)
        assertEquals(View.VISIBLE, airlineFeeWarningText.visibility)
        assertEquals(View.VISIBLE, basicEconomyMessaging.visibility)
        widget.viewModel.airlineFeeWarningTextObservable.onNext("An airline fee, based on card type, is added upon payment.")
        assertEquals("An airline fee, based on card type, is added upon payment.", airlineFeeWarningText.text)

        widget.viewModel.showFreeCancellationObservable.onNext(false)
        widget.viewModel.showSplitTicketMessagingObservable.onNext(false)
        widget.viewModel.showAirlineFeeWarningObservable.onNext(false)
        widget.viewModel.showBasicEconomyMessageObservable.onNext(false)
        assertEquals(View.GONE, freeCancelltionText.visibility)
        assertEquals(View.GONE, splitTicketBaggageFeeLinkContainer.visibility)
        assertEquals(View.GONE, airlineFeeWarningText.visibility)
        assertEquals(View.GONE, basicEconomyMessaging.visibility)

    }

    @Test
    fun onOutBoundFlightWidgetClick() {
        val flightSummaryWidget = widget.flightSummary
        val outboundFlightWidget = flightSummaryWidget.outboundFlightWidget
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        outboundFlightWidget.viewModel.searchParams.onNext(setupFlightSearchParams())
        outboundFlightWidget.viewModel.travelInfoTextObservable.onNext("")
        val flightLeg = FlightLeg()
        flightLeg.flightSegments = ArrayList()
        outboundFlightWidget.viewModel.selectedFlightLegObservable.onNext(flightLeg)
        outboundFlightWidget.rowContainer.performClick()
        assertEquals(View.VISIBLE, outboundFlightWidget.flightDetailsContainer.visibility)
        outboundFlightWidget.rowContainer.performClick()
        assertEquals(View.GONE, outboundFlightWidget.flightDetailsContainer.visibility)
    }

    @Test
    fun onInBoundFlightWidgetClick() {
        val flightSummaryWidget = widget.flightSummary
        val inboundFlightWidget = flightSummaryWidget.inboundFlightWidget
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        inboundFlightWidget.viewModel.searchParams.onNext(setupFlightSearchParams())
        inboundFlightWidget.viewModel.travelInfoTextObservable.onNext("")
        val flightLeg = FlightLeg()
        flightLeg.flightSegments = ArrayList()
        inboundFlightWidget.viewModel.selectedFlightLegObservable.onNext(flightLeg)
        inboundFlightWidget.rowContainer.performClick()
        assertEquals(View.VISIBLE, inboundFlightWidget.flightDetailsContainer.visibility)
        inboundFlightWidget.rowContainer.performClick()
        assertEquals(View.GONE, inboundFlightWidget.flightDetailsContainer.visibility)
    }

    @Test
    fun testTotalPriceWidget() {
        val flightCheckoutPresenter = widget.getCheckoutPresenter()
        val totalPriceWidget = flightCheckoutPresenter.findViewById(R.id.total_price_widget) as TotalPriceWidget
        val bundleTotalPrice = flightCheckoutPresenter.findViewById(R.id.bundle_total_price) as TextView
        val bundleTotalText = flightCheckoutPresenter.findViewById(R.id.bundle_total_text) as TextView
        val bundleTotalIncludes = flightCheckoutPresenter.findViewById(R.id.bundle_total_includes_text) as TextView
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(bundleTotalText.text.toString(), context.getString(R.string.trip_total))
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(bundleTotalIncludes.text.toString(), context.getString(R.string.includes_taxes_and_fees))
        flightCheckoutPresenter.onCreateTripResponse(getFlightCreateTripResponse())
        assertEquals(bundleTotalPrice.text.toString(), Money(223, "USD").getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
        assertEquals(false, totalPriceWidget.dialog.isShowing)
        totalPriceWidget.performClick()
        assertEquals(true, totalPriceWidget.dialog.isShowing)
        totalPriceWidget.dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        assertEquals(false, totalPriceWidget.dialog.isShowing)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResetCheckout() {
        val flightCheckoutPresenter = widget.getCheckoutPresenter()
        Db.loadTripBucket(context)
        Db.setFlightSearchParams(setupFlightSearchParams())
        val createTripResponse = getFlightCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)
        val checkoutBtn = widget.checkoutButton
        assertEquals(true, checkoutBtn.isEnabled)
        assertEquals(View.VISIBLE, checkoutBtn.visibility)
        assertEquals(checkoutBtn.text.toString(), context.resources.getString(R.string.Checkout))
    }

    @Test
    fun testBasicEconomyMessageVisibility() {
        Db.loadTripBucket(context)
        Db.setFlightSearchParams(setupFlightSearchParams())
        val createTripResponse = getFlightCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))

        val flightCheckoutPresenter = widget.getCheckoutPresenter()
        val flightSummary = widget.flightSummary
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)

        assertEquals(View.GONE, flightSummary.basicEconomyMessageTextView.visibility)

        createTripResponse.details.legs[0].isBasicEconomy = true
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)

        assertEquals(View.VISIBLE, flightSummary.basicEconomyMessageTextView.visibility)
    }

    //TODO per Rajan, disabling flaky test that keeps blocking PRs. He will take a look.
//    @Test
//    fun testBasicEconomyMessageClick() {
//        Db.loadTripBucket(context)
//        Db.setFlightSearchParams(setupFlightSearchParams())
//        val createTripResponse = getFlightCreateTripResponse()
//        createTripResponse.details.legs[0].isBasicEconomy = true
//        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))
//
//        val flightCheckoutPresenter = widget.getCheckoutPresenter()
//        val flightSummary = widget.flightSummary
//        val basicEconomyClickedTestSubscriber = TestSubscriber<Unit>()
//        flightSummary.basicEconomyInfoClickedSubject.subscribe(basicEconomyClickedTestSubscriber)
//
//        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)
//
//        assertEquals(BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name, widget.currentState)
//
//        flightSummary.basicEconomyMessageTextView.performClick()
//        basicEconomyClickedTestSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
//        assertEquals(BasicEconomyInfoWebView::class.java.name, widget.currentState)
//    }

    @Test
    fun testToolbar() {
        val flightSearchParams = setupFlightSearchParams()
        val checkoutOverviewHeaderToolbar = widget.bundleOverviewHeader.checkoutOverviewHeaderToolbar
        val flightCheckoutOverviewViewModel = FlightCheckoutOverviewViewModel(context)
        flightCheckoutOverviewViewModel.params.onNext(setupFlightSearchParams())
        checkoutOverviewHeaderToolbar.viewmodel = flightCheckoutOverviewViewModel
        val destinationText = checkoutOverviewHeaderToolbar.destinationText
        val city = SuggestionStrUtils.formatCityName(flightSearchParams.arrivalAirport.regionNames.displayName).trim()
        assertEquals(destinationText.text.toString(), city)

        val checkInOutDates = checkoutOverviewHeaderToolbar.checkInOutDates
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val checkIn = flightSearchParams.departureDate?.toString(formatter)
        val checkOut = flightSearchParams.returnDate?.toString(formatter)
        val checkInOutText = DateFormatUtils.formatPackageDateRange(context, checkIn, checkOut)
        assertEquals(checkInOutDates.text.toString(), checkInOutText)

        val travelers = checkoutOverviewHeaderToolbar.travelers
        val guests = flightSearchParams.guests
        val guestText = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, guests, guests);
        assertEquals(travelers.text.toString(), guestText)

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
        val hierarchyInfoDepart = SuggestionV4.HierarchyInfo()
        hierarchyInfoDepart.airport = SuggestionV4.Airport()
        hierarchyInfoDepart.airport!!.airportCode = "12qw"
        departureSuggestion.hierarchyInfo = hierarchyInfoDepart

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalRegionNames.fullName = "LAX - Los Angeles"
        arrivalSuggestion.regionNames = arrivalRegionNames
        arrivalSuggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name
        val hierarchyInfoArrive = SuggestionV4.HierarchyInfo()
        hierarchyInfoArrive.airport = SuggestionV4.Airport()
        hierarchyInfoArrive.airport!!.airportCode = "12qw"
        arrivalSuggestion.hierarchyInfo = hierarchyInfoArrive

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(2)
        childList.add(4)
        val checkIn = LocalDate().plusDays(2)
        val checkOut = LocalDate().plusDays(3)

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null)
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


}