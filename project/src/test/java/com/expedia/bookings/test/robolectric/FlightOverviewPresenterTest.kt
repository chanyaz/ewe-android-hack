package com.expedia.bookings.test.robolectric

import android.app.AlertDialog
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.presenter.flight.FlightSummaryWidget
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.FlightCellWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.util.Optional
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.FlightOverviewSummaryViewModel
import com.expedia.vm.packages.PackageSearchType
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RxJavaTestImmediateSchedulerRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightOverviewPresenterTest {

    private val context = RuntimeEnvironment.application
    private lateinit var widget: FlightOverviewPresenter

    lateinit var flightLeg: FlightLeg
    lateinit var activity: FragmentActivity
    val flightServiceRule = ServicesRule(FlightServices::class.java)
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        val validator = Ui.getApplication(context).travelerComponent().travelerValidator()
        validator.updateForNewSearch(setupFlightSearchParams())
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        widget.viewModel.outboundSelectedAndTotalLegRank = Pair(0, 0)
        widget.viewModel
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFareFamilyWidgetVisibility() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        val testSubscriber = TestSubscriber.create<FlightCreateTripResponse>()
        val params = FlightCreateTripParams.Builder().productKey("happy_fare_family_round_trip").build()
        flightServiceRule.services!!.createTrip(params, testSubscriber)
        widget.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        assertEquals(View.VISIBLE, widget.fareFamilyCardView.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFareFamilyCreateTripFiring() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        val cardViewViewModel = widget.fareFamilyCardView.viewModel
        val detailsViewModel = widget.flightFareFamilyDetailsWidget.viewModel
        val testSubscriber = TestSubscriber.create<FlightCreateTripResponse>()
        val params = FlightCreateTripParams.Builder().productKey("happy_fare_family_round_trip").build()
        flightServiceRule.services!!.createTrip(params, testSubscriber)
        widget.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])

        val updateTripTestSubscriber = TestSubscriber.create<Pair<String, FlightTripResponse.FareFamilyDetails>>()
        cardViewViewModel.updateTripObserver.subscribe(updateTripTestSubscriber)

        //When done button is not pressed
        detailsViewModel.choosingFareFamilyObservable.onNext(getFareFamilyDetail("coach"))
        detailsViewModel.selectedFareFamilyObservable.onNext(getFareFamilyDetail("coach"))
        updateTripTestSubscriber.assertNoValues()

        //When done is pressed but user not changed the original selection
        detailsViewModel.doneButtonObservable.onNext(Unit)
        updateTripTestSubscriber.assertNoValues()

        //When done is pressed but user changed the selection too
        detailsViewModel.choosingFareFamilyObservable.onNext(getFareFamilyDetail("economy"))
        detailsViewModel.doneButtonObservable.onNext(Unit)
        updateTripTestSubscriber.assertValueCount(1)
        assertEquals("economy", updateTripTestSubscriber.onNextEvents[0].second.fareFamilyCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFareFamilyUnavailableError() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        val testSubscriber = TestSubscriber.create<FlightCreateTripResponse>()
        val params = FlightCreateTripParams.Builder().productKey("fare_family_unavailable_error").build()
        flightServiceRule.services!!.createTrip(params, testSubscriber)
        widget.flightFareFamilyDetailsWidget.viewModel.selectedFareFamilyObservable.onNext(testSubscriber.onNextEvents[0].fareFamilyList!!.fareFamilyDetails.first())
        widget.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val okButton = alertDialog.findViewById<View>(android.R.id.button1) as Button
        val errorMessage = alertDialog.findViewById<View>(android.R.id.message) as android.widget.TextView
        assertEquals(true, alertDialog.isShowing)
        assert(errorMessage.text.contains("Sorry, Economy Plus is now sold out. Please try again with a different fare option. Reverting your flights to your original fare selection."))
        assertEquals("OK", okButton.text)
    }

    @Test
    fun widgetVisibilityTest() {
        assertEquals(View.VISIBLE, widget.bundleOverviewHeader.visibility)
        assertEquals(View.VISIBLE, widget.flightSummary.visibility)
        assertEquals(View.VISIBLE, widget.getCheckoutPresenter().visibility)
        assertEquals(View.GONE, widget.cvv.visibility)
        assertEquals(View.GONE, widget.paymentFeeInfoWebView.visibility)

        val freeCancellationInfoContainer = widget.flightSummary.freeCancellationInfoContainer
        val splitTicketBaggageFeeLinkContainer = widget.flightSummary.splitTicketInfoContainer
        val airlineFeeWarningText = widget.flightSummary.airlineFeeWarningTextView
        val basicEconomyMessaging = widget.flightSummary.basicEconomyMessageTextView

        widget.viewModel.airlineFeeWarningTextObservable.onNext("There may be an additional fee based on your payment method.")
        widget.viewModel.showFreeCancellationObservable.onNext(true)
        widget.viewModel.showSplitTicketMessagingObservable.onNext(true)
        widget.viewModel.showAirlineFeeWarningObservable.onNext(true)
        widget.viewModel.showBasicEconomyMessageObservable.onNext(true)
        assertEquals("There may be an additional fee based on your payment method.", airlineFeeWarningText.text)
        assertEquals(View.VISIBLE, freeCancellationInfoContainer.visibility)
        assertEquals(View.VISIBLE, splitTicketBaggageFeeLinkContainer.visibility)
        assertEquals(View.VISIBLE, airlineFeeWarningText.visibility)
        assertEquals(View.VISIBLE, basicEconomyMessaging.visibility)

        widget.viewModel.airlineFeeWarningTextObservable.onNext("There may be an additional fee based on your payment method.")
        widget.viewModel.showFreeCancellationObservable.onNext(false)
        widget.viewModel.showSplitTicketMessagingObservable.onNext(false)
        widget.viewModel.showAirlineFeeWarningObservable.onNext(false)
        widget.viewModel.showBasicEconomyMessageObservable.onNext(false)
        assertEquals("There may be an additional fee based on your payment method.", airlineFeeWarningText.text)
        assertEquals(View.GONE, freeCancellationInfoContainer.visibility)
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
        assertEquals(View.VISIBLE, outboundFlightWidget.rowContainer.visibility)
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
        val totalPriceWidget = widget.bottomCheckoutContainer.totalPriceWidget
        val bundleTotalPrice = totalPriceWidget.bundleTotalPrice
        val bundleTotalText = totalPriceWidget.bundleTotalText
        val bundleTotalIncludes = totalPriceWidget.bundleTotalIncludes
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
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(Optional(createTripResponse))
        val checkoutBtn = widget.bottomCheckoutContainer.checkoutButton
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
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(Optional(createTripResponse))

        assertEquals(View.GONE, flightSummary.basicEconomyMessageTextView.visibility)

        createTripResponse.details.legs[0].isBasicEconomy = true
        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(Optional(createTripResponse))

        assertEquals(View.VISIBLE, flightSummary.basicEconomyMessageTextView.visibility)
    }

//    disabling flaky test until reliable solution found
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
//        val basicEconomyClickedTestObserver = TestObserver<Unit>()
//        flightSummary.basicEconomyInfoClickedSubject.subscribe(basicEconomyClickedTestObserver)
//
//        flightCheckoutPresenter.getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)
//
//        assertEquals(BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name, widget.currentState)
//
//        flightSummary.basicEconomyMessageTextView.performClick()
//        basicEconomyClickedTestObserver.awaitValueCount(1, 2, TimeUnit.SECONDS)
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
        assertEquals(checkInOutDates.text.toString(), getToolbarDateText(flightSearchParams))

        val travelers = checkoutOverviewHeaderToolbar.travelers
        val guests = flightSearchParams.guests
        val guestText = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, guests, guests)
        assertEquals(travelers.text.toString(), guestText)
    }

    @Test
    fun testFlightSummaryWidgets() {
        setShowMoreInfoTest()
        val flightSummaryWidget = widget.findViewById<View>(R.id.flight_summary) as FlightSummaryWidget
        flightSummaryWidget.viewmodel = FlightOverviewSummaryViewModel(context)
        flightSummaryWidget.viewmodel.params.onNext(setupFlightSearchParams(false))
        assertEquals(View.VISIBLE, flightSummaryWidget.outboundFlightTitle.visibility)
        assertEquals(View.GONE, flightSummaryWidget.inboundFlightTitle.visibility)

        flightSummaryWidget.viewmodel.params.onNext(setupFlightSearchParams(true))
        assertEquals(View.VISIBLE, flightSummaryWidget.outboundFlightTitle.visibility)
        assertEquals(View.VISIBLE, flightSummaryWidget.inboundFlightTitle.visibility)
    }

    @Test
    fun testFreeCancellationInfo() {
        var flightSummaryWidget = widget.findViewById<View>(R.id.flight_summary) as FlightSummaryWidget
        widget.viewModel.showFreeCancellationObservable.onNext(true)
        flightSummaryWidget.viewmodel = FlightOverviewSummaryViewModel(context)
        flightSummaryWidget.viewmodel.params.onNext(setupFlightSearchParams(false))
        assertEquals(View.VISIBLE, flightSummaryWidget.freeCancellationInfoContainer.visibility)
        assertEquals(View.GONE, flightSummaryWidget.freeCancellationInfoTextView.visibility)
        flightSummaryWidget.freeCancellationInfoContainer.performClick()
        assertEquals(View.VISIBLE, flightSummaryWidget.freeCancellationInfoTextView.visibility)
        assertEquals("After 24 hours, standard flight rules apply.", flightSummaryWidget.freeCancellationInfoTextView.text)
        flightSummaryWidget.freeCancellationInfoContainer.performClick()
        assertEquals(View.GONE, flightSummaryWidget.freeCancellationInfoTextView.visibility)
    }

    @Test
    fun testRowContainerWidgetsWhenBucketedForMoreInfoTest() {
        setShowMoreInfoTest()
        val flightSummaryWidget = widget.findViewById<View>(R.id.flight_summary) as FlightSummaryWidget
        val outboundFlightWidget = flightSummaryWidget.findViewById<View>(R.id.package_bundle_outbound_flight_widget) as OutboundFlightWidget
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        outboundFlightWidget.viewModel.searchParams.onNext(setupFlightSearchParams())
        outboundFlightWidget.viewModel.travelInfoTextObservable.onNext("")
        createExpectedFlightLeg()
        outboundFlightWidget.viewModel.selectedFlightLegObservable.onNext(flightLeg)

        assertEquals(outboundFlightWidget.rowContainer.getChildAt(0).javaClass.name, FlightCellWidget::class.java.name)

        outboundFlightWidget.rowContainer.performClick()
        assertEquals(View.VISIBLE, outboundFlightWidget.flightDetailsContainer.visibility)
        assertEquals(View.GONE, outboundFlightWidget.rowContainer.visibility)

        outboundFlightWidget.flightDetailsContainer.performClick()
        assertEquals(View.GONE, outboundFlightWidget.flightDetailsContainer.visibility)
        assertEquals(View.VISIBLE, outboundFlightWidget.rowContainer.visibility)
    }

    @Test
    fun testOutboundWidgetBaggageInfoPaymentInfoButtonVisibility() {
        createExpectedFlightLeg()
        val outboundFlightWidget = widget.flightSummary.outboundFlightWidget
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        prepareBundleWidgetViewModel(outboundFlightWidget.viewModel)
        val outboundFlightBaggagePackageDivider = outboundFlightWidget.baggagePaymentDivider
        assertEquals(View.VISIBLE, outboundFlightBaggagePackageDivider.visibility)
        val outboundFlightShowBaggageFeesInfo = outboundFlightWidget.baggageFeesButton as Button
        assertEquals(View.VISIBLE, outboundFlightShowBaggageFeesInfo.visibility)
        assertEquals(outboundFlightShowBaggageFeesInfo.text, context.getString(R.string.package_flight_overview_baggage_fees))
        val outboundFlightShowPaymentFeesInfo = outboundFlightWidget.paymentFeesButton as Button
        assertEquals(View.VISIBLE, outboundFlightShowPaymentFeesInfo.visibility)
        assertEquals(outboundFlightShowPaymentFeesInfo.text, context.getString(R.string.payment_and_baggage_fees_may_apply))
    }

    @Test
    fun testInboundWidgetBaggageInfoPaymentInfoButtonVisibility() {
        createExpectedFlightLeg()
        val inboundFlightWidget = widget.flightSummary.inboundFlightWidget
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        prepareBundleWidgetViewModel(inboundFlightWidget.viewModel)
        val inboundFlightBaggagePackageDivider = inboundFlightWidget.baggagePaymentDivider
        assertEquals(View.VISIBLE, inboundFlightBaggagePackageDivider.visibility)
        val inboundFlightShowBaggageFeesInfo = inboundFlightWidget.baggageFeesButton as Button
        assertEquals(View.VISIBLE, inboundFlightShowBaggageFeesInfo.visibility)
        assertEquals(inboundFlightShowBaggageFeesInfo.text, context.getString(R.string.package_flight_overview_baggage_fees))
        val inboundFlightShowPaymentFeesInfo = inboundFlightWidget.paymentFeesButton as Button
        assertEquals(View.VISIBLE, inboundFlightShowPaymentFeesInfo.visibility)
        assertEquals(inboundFlightShowPaymentFeesInfo.text, context.getString(R.string.payment_and_baggage_fees_may_apply))
    }

    @Test
    fun testOutboundWidgetBaggageInfoClick() {
        createExpectedFlightLeg()
        val outboundFlightWidget = widget.flightSummary.outboundFlightWidget
        val outboundFlightBaggageInfoTestSubscriber = TestSubscriber<String>()
        val outboundFlightBaggageFeesURL = "http://www.expedia.com/Flights-BagFees?originapt=SFO&destinationapt=SEA"
        flightLeg.baggageFeesUrl = outboundFlightBaggageFeesURL
        outboundFlightWidget.viewModel.baggageInfoUrlSubject.subscribe(outboundFlightBaggageInfoTestSubscriber)
        prepareBundleWidgetViewModel(outboundFlightWidget.viewModel)
        outboundFlightWidget.baggageFeesButton.performClick()
        outboundFlightBaggageInfoTestSubscriber.assertValue(outboundFlightBaggageFeesURL)
    }

    @Test
    fun testInboundWidgetBaggageInfoClick() {
        createExpectedFlightLeg()
        val inboundFlightWidget = widget.flightSummary.inboundFlightWidget
        val inboundFlightBaggageInfoTestSubscriber = TestSubscriber<String>()
        val inboundFlightBaggageFeesURL = "http://www.expedia.com/Flights-BagFees?originapt=SEA&destinationapt=SFO"
        flightLeg.baggageFeesUrl = inboundFlightBaggageFeesURL
        inboundFlightWidget.viewModel.baggageInfoUrlSubject.subscribe(inboundFlightBaggageInfoTestSubscriber)
        prepareBundleWidgetViewModel(inboundFlightWidget.viewModel)
        inboundFlightWidget.baggageFeesButton.performClick()
        inboundFlightBaggageInfoTestSubscriber.assertValue(inboundFlightBaggageFeesURL)
    }

    @Test
    fun testOutboundWidgetBaggageUrlUpdate() {
        createExpectedFlightLeg()
        val summaryWidgetViewModel = widget.flightSummary.viewmodel
        summaryWidgetViewModel.params.onNext(setupFlightSearchParams())
        val outboundFlightWidget = widget.flightSummary.outboundFlightWidget
        val outboundFlightBaggageInfoTestSubscriber = TestSubscriber<String>()
        flightLeg.baggageFeesUrl = "http://old baggage url"
        outboundFlightWidget.viewModel.baggageInfoUrlSubject.subscribe(outboundFlightBaggageInfoTestSubscriber)
        prepareBundleWidgetViewModel(outboundFlightWidget.viewModel)
        outboundFlightWidget.baggageFeesButton.performClick()
        assertEquals("http://old baggage url", outboundFlightBaggageInfoTestSubscriber.onNextEvents[0])

        //After new create trip
        summaryWidgetViewModel.tripResponse.onNext(getFlightCreateTripResponse())
        outboundFlightWidget.baggageFeesButton.performClick()
        assertEquals("http://new baggage url", outboundFlightBaggageInfoTestSubscriber.onNextEvents[1])
    }

    @Test
    fun testOutboundWidgetPaymentInfoClick() {
        createExpectedFlightLeg()
        val outboundFlightWidget = widget.flightSummary.outboundFlightWidget
        widget.getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.onNext("http://www.expedia.com/p/regulatory/obfees?langid=2057")
        outboundFlightWidget.paymentFeesButton.performClick()
        assertEquals(View.VISIBLE, widget.paymentFeeInfoWebView.visibility)
    }

//   TODO https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/6024
//    @Test
//    fun testInboundWidgetPaymentInfoClick() {
//        createExpectedFlightLeg()
//        val inboundFlightWidget = widget.flightSummary.inboundFlightWidget
//        widget.getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.onNext("http://www.expedia.com/p/regulatory/obfees?langid=2057")
//        inboundFlightWidget.paymentFeesButton.performClick()
//        assertEquals(View.VISIBLE, widget.paymentFeeInfoWebView.visibility)
//    }

    @Test
    fun testOutboundWidgetPaymentInfoClickWithNoURL() {
        createExpectedFlightLeg()
        val outboundFlightWidget = widget.flightSummary.outboundFlightWidget
        widget.getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.onNext("")
        outboundFlightWidget.paymentFeesButton.performClick()
        assertEquals(View.GONE, widget.paymentFeeInfoWebView.visibility)
    }

    @Test
    fun testInboundWidgetPaymentInfoClickWithNoURL() {
        createExpectedFlightLeg()
        val inboundFlightWidget = widget.flightSummary.inboundFlightWidget
        widget.getCheckoutPresenter().getCheckoutViewModel().obFeeDetailsUrlSubject.onNext("")
        inboundFlightWidget.paymentFeesButton.performClick()
        assertEquals(View.GONE, widget.paymentFeeInfoWebView.visibility)
    }

    private fun setupFlightSearchParams(isRoundTrip: Boolean = true): FlightSearchParams {
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
        val checkOut = if (isRoundTrip) LocalDate().plusDays(3) else null

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null,null)
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
        val seatClassAndBookingCode = FlightTripDetails().SeatClassAndBookingCode()
        seatClassAndBookingCode.seatClass = "coach"
        flightOffer.offersSeatClassAndBookingCode = listOf(listOf(seatClassAndBookingCode))
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.legs = ArrayList()
        val flightLeg = FlightLeg()
        flightLeg.segments = ArrayList()
        flightLeg.segments.add(FlightLeg.FlightSegment())
        flightLeg.baggageFeesUrl = "http://new baggage url"
        flightTripDetails.legs.add(flightLeg)
        flightOffer.pricePerPassengerCategory = pricePerPassengerList
        flightTripDetails.offer = flightOffer
        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.totalPriceIncludingFees = Money(223, "USD")
        flightCreateTripResponse.selectedCardFees = Money(0, "USD")

        return flightCreateTripResponse
    }

    private fun getToolbarDateText(params: FlightSearchParams): String {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val checkIn = params.departureDate?.toString(formatter)
        val checkOut = params.returnDate?.toString(formatter)
        return DateFormatUtils.formatPackageDateRange(context, checkIn, checkOut)
    }

    private fun createExpectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.mayChargeObFees = true
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.departureTimeShort = "1:10AM"
        flightLeg.arrivalTimeShort = "12:20PM"
        flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flightLeg.stopCount = 1
        flightLeg.baggageFeesUrl = "http://www.expedia.com/Flights-BagFees?originapt=SFO&destinationapt=SEA"
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(201)
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
        val list: ArrayList<FlightLeg.FlightSegment> = ArrayList()
        list.add(createFlightSegment())
        flightLeg.flightSegments = list
    }

    private fun createFlightSegment(): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "Virgin America"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = "San Francisco"
        airlineSegment.arrivalCity = "Honolulu"
        airlineSegment.departureAirportCode = "SFO"
        airlineSegment.arrivalAirportCode = "SEA"
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        airlineSegment.elapsedDays = 0
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }

    private fun getDummySuggestion(airportCode: String) : SuggestionV4 {
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

    private fun prepareBundleWidgetViewModel(bundleFlightViewModel: BundleFlightViewModel) {
        bundleFlightViewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
        bundleFlightViewModel.suggestion.onNext(getDummySuggestion("SFO"))
        bundleFlightViewModel.flight.onNext(flightLeg)
        bundleFlightViewModel.date.onNext(LocalDate())
        bundleFlightViewModel.guests.onNext(1)
    }

    private fun getFareFamilyDetail(className: String): FlightTripResponse.FareFamilyDetails {
        return FlightTripResponse.FareFamilyDetails(className, className, className,
                Money("210.00", "USD"), Money(1, "USD"), true, HashMap())
    }

    private fun setShowMoreInfoTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview)
    }
}