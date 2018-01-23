package com.expedia.bookings.widget.packages

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.packages.BundleFlightViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageInboundFlightWidgetTest {
    var testOrigin: SuggestionV4 by Delegates.notNull()
    var testFlight: FlightLeg by Delegates.notNull()
    val testRegionName = "Chicago"
    val testAirportCode = "ORD"
    val testFlightText = "(ORD) Chicago"
    val testTravelerInfoText = "Jun 29 at 9:00 am, 1 Traveler"
    var testWidget: InboundFlightWidget by Delegates.notNull()
    var widgetVM: BundleFlightViewModel by Delegates.notNull()

    var expectedDisabledColor: Int by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        testOrigin = buildMockOriginSuggestion()
        testWidget = InboundFlightWidget(activity, null)
        widgetVM = BundleFlightViewModel(activity, LineOfBusiness.PACKAGES)
        testFlight = buildMockFlight()
        testWidget.viewModel = widgetVM
        testWidget.viewModel.flight.onNext(testFlight)
        expectedDisabledColor = ContextCompat.getColor(activity, R.color.package_bundle_icon_color)
        setUpParams()
    }

    @Test
    fun testEnable() {
        testWidget.enable()
        assertTrue(testWidget.rowContainer.isEnabled)
        assertTrue(testWidget.flightDetailsIcon.isEnabled)

        assertEquals(View.GONE, testWidget.flightDetailsIcon.visibility)
        val shadowDrawable = Shadows.shadowOf(testWidget.flightIcon.drawable)
        assertEquals(R.drawable.packages_flight2_icon, shadowDrawable.createdFromResId)

        val expectedFlightText = activity.getString(R.string.select_flight_to, StrUtils.formatCityName(testOrigin))
        assertEquals(expectedFlightText, testWidget.flightCardText.text)
        assertEquals(View.VISIBLE, testWidget.travelInfoText.visibility)
    }

    @Test
    fun testDisable() {
        testWidget.disable()
        assertFalse(testWidget.rowContainer.isEnabled)
        assertFalse(testWidget.flightDetailsIcon.isEnabled)

        assertEquals(expectedDisabledColor, testWidget.flightCardText.currentTextColor)
        assertEquals(expectedDisabledColor, testWidget.travelInfoText.currentTextColor)

        assertEquals(View.GONE, testWidget.forwardArrow.visibility)
    }

    @Test
    fun testHandleResultsLoaded() {
        testWidget.handleResultsLoaded()

        assertTrue(testWidget.rowContainer.isEnabled)
        assertTrue(testWidget.flightInfoContainer.isEnabled)

        assertEquals(View.GONE, testWidget.flightLoadingBar.visibility)

        val expectedFlightText = activity.getString(R.string.select_flight_to, StrUtils.formatCityName(testOrigin))
        assertEquals(expectedFlightText, testWidget.flightCardText.text)
        assertEquals(View.VISIBLE, testWidget.travelInfoText.visibility)
    }

    @Test
    fun testCancel() {
        testWidget.cancel()

        assertEquals(View.GONE, testWidget.flightLoadingBar.visibility)
        assertEquals(View.VISIBLE, testWidget.travelInfoText.visibility)
    }

    private fun setUpParams() {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(testOrigin)
                .destination(SuggestionV4())
                .build() as PackageSearchParams
        testWidget.viewModel.searchParams.onNext(packageParams)
        testWidget.viewModel.travelInfoTextObservable.onNext("")
    }

    private fun buildMockOriginSuggestion(): SuggestionV4 {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = testRegionName
        hierarchyInfo.airport = buildMockAirport()
        origin.hierarchyInfo = hierarchyInfo
        origin.regionNames = regionNames
        return origin
    }

    private fun buildMockAirport(): SuggestionV4.Airport {
        val airport = Mockito.mock(SuggestionV4.Airport::class.java)
        airport.airportCode = testAirportCode
        return airport
    }

    @Test
    fun testContentDescriptionExpanded() {
        widgetVM.flightTextObservable.onNext(testFlightText)
        widgetVM.travelInfoTextObservable.onNext(testTravelerInfoText)
        testWidget.flightDetailsContainer.visibility = View.VISIBLE

        val expandedState = "Button to collapse"
        val expectedText = Phrase.from(activity, R.string.select_flight_selected_cont_desc_TEMPLATE)
                .put("flight", testFlightText)
                .put("datetraveler", testTravelerInfoText)
                .put("expandstate", expandedState)
                .format().toString()

        assertEquals(expectedText, testWidget.selectedCardContentDescription())
    }

    @Test
    fun testContentDescriptionCollapsed() {
        widgetVM.flightTextObservable.onNext(testFlightText)
        widgetVM.travelInfoTextObservable.onNext(testTravelerInfoText)
        testWidget.flightDetailsContainer.visibility = View.GONE

        val collapsedState = "Button to expand"
        val expectedText = Phrase.from(activity, R.string.select_flight_selected_cont_desc_TEMPLATE)
                .put("flight", testFlightText)
                .put("datetraveler", testTravelerInfoText)
                .put("expandstate", collapsedState)
                .format().toString()

        assertEquals(expectedText, testWidget.selectedCardContentDescription())
    }

    @Test
    fun testFlightNotSelected() {
        widgetVM.flightTextObservable.onNext(testFlightText)
        widgetVM.travelInfoTextObservable.onNext(testTravelerInfoText)

        assertEquals(testFlightText, testWidget.flightCardText.text)
        assertEquals(testTravelerInfoText, testWidget.travelInfoText.text)
    }

    @Test
    fun testLoadingContentDescription() {
        testWidget.loadingStateObservable.onNext(true)

        val expectedText = Phrase.from(activity, R.string.select_flight_searching_cont_desc_TEMPLATE)
                .put("flight", testFlightText)
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(LocalDate.now().plusDays(2)))
                .put("travelers", "1 traveler")
                .format().toString()

        assertEquals(expectedText, testWidget.getRowInfoContainer().contentDescription)
    }

    @Test
    fun testLoadedContentDescription() {
        testWidget.loadingStateObservable.onNext(false)

        val expectedText = Phrase.from(activity, R.string.select_flight_cont_desc_TEMPLATE)
                .put("flight", testFlightText)
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(LocalDate.now().plusDays(2)))
                .put("travelers", "1 traveler")
                .format()
                .toString()

        assertEquals(expectedText, testWidget.getRowInfoContainer().contentDescription)
    }

    @Test
    fun testFlightExpandedWidgetContentDescription() {
        testWidget.expandFlightDetails()
        assertEquals(activity.getString(R.string.accessibility_cont_desc_role_button_collapse), testWidget.getFlightWidgetExpandedState())
    }

    @Test
    fun testFlightCollapsedWidgetContentDescription() {
        testWidget.collapseFlightDetails()
        assertEquals(activity.getString(R.string.accessibility_cont_desc_role_button_expand), testWidget.getFlightWidgetExpandedState())
    }

    @Test
    fun testBackPressExpanded() {
        testWidget.expandFlightDetails()
        testWidget.backButtonPressed()
        assertEquals(testWidget.flightDetailsContainer.visibility, Presenter.GONE)
    }

    @Test
    fun testBackPressCollapsed() {
        testWidget.collapseFlightDetails()
        testWidget.backButtonPressed()
        assertEquals(testWidget.flightDetailsContainer.visibility, Presenter.GONE)
    }

    fun buildMockFlight(): FlightLeg {
        val flight = Mockito.mock(FlightLeg::class.java)
        flight.destinationAirportCode = testAirportCode
        flight.destinationCity = testRegionName
        return flight
    }
}
