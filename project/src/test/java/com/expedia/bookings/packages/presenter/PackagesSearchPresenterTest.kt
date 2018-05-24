package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.vm.PackageSearchViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerPickerView
import com.expedia.util.PackageCalendarRules
import com.expedia.vm.FlightTravelerPickerViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.mockito.Mockito.spy

@RunWith(RobolectricRunner::class)
class PackagesSearchPresenterTest {
    private lateinit var widget: PackageSearchPresenter
    private lateinit var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    val context: Context = RuntimeEnvironment.application

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testNewTravelerPickerSelectionOperations() {
        inflate()
        val travelerCard = widget.travelerWidgetV2
        travelerCard.performClick()
        val view = travelerCard.travelerDialogView
        val travelerPicker = view.findViewById<FlightTravelerPickerView>(R.id.flight_traveler_view)

        travelerPicker.youthCountSelector.travelerPlus.performClick()
        travelerPicker.childCountSelector.travelerPlus.performClick()
        travelerPicker.infantCountSelector.travelerPlus.performClick()

        travelerPicker.getViewModel().showSeatingPreference = true
        travelerPicker.infantInLap.isChecked = true

        assertEquals("[16, 10, 1]", travelerPicker.viewmodel.travelerParamsObservable.value.childrenAges.toString())
        assertEquals("1", travelerPicker.viewmodel.travelerParamsObservable.value.numberOfAdults.toString())
        assertEquals("4", travelerPicker.viewmodel.travelerParamsObservable.value.getTravelerCount().toString())

        assertTrue(travelerPicker.viewmodel.isInfantInLapObservable.value)

        travelerPicker.infantCountSelector.travelerMinus.performClick()
        assertEquals("[16, 10]", travelerPicker.viewmodel.travelerParamsObservable.value.childrenAges.toString())
        assertEquals("3", travelerPicker.viewmodel.travelerParamsObservable.value.getTravelerCount().toString())

        travelerPicker.adultCountSelector.travelerPlus.performClick()
        assertEquals("2", travelerPicker.viewmodel.travelerParamsObservable.value.numberOfAdults.toString())
    }

    @Test
    fun testNewTravelerPickerWidgetItemsVisiblity() {
        inflate()
        val travelerCard = widget.travelerWidgetV2
        travelerCard.performClick()
        val view = travelerCard.travelerDialogView
        val travelerPicker = view.findViewById<FlightTravelerPickerView>(R.id.flight_traveler_view)

        assertEquals(View.VISIBLE, travelerPicker.visibility)
        assertEquals(View.VISIBLE, travelerPicker.adultCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.youthCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.childCountSelector.visibility)
        assertEquals(View.VISIBLE, travelerPicker.infantCountSelector.visibility)

        travelerPicker.infantCountSelector.travelerPlus.performClick()
        assertEquals(View.VISIBLE, travelerPicker.infantInLap.visibility)
    }

    @Test
    fun testTravelerFormSelectionsTracked() {
        inflate()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        val travelerCard = widget.travelerWidgetV2
        travelerCard.performClick()

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        val view = travelerCard.travelerDialogView
        val travelerPicker = view.findViewById<FlightTravelerPickerView>(R.id.flight_traveler_view)
        travelerPicker.viewmodel.lob = LineOfBusiness.PACKAGES

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        travelerPicker.adultCountSelector.travelerPlus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Add.Adult", mockAnalyticsProvider)

        travelerPicker.youthCountSelector.travelerPlus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Add.Youth", mockAnalyticsProvider)

        travelerPicker.childCountSelector.travelerPlus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Add.Child", mockAnalyticsProvider)

        travelerPicker.infantCountSelector.travelerPlus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Add.Infant", mockAnalyticsProvider)

        travelerPicker.adultCountSelector.travelerMinus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Remove.Adult", mockAnalyticsProvider)

        travelerPicker.youthCountSelector.travelerMinus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Remove.Youth", mockAnalyticsProvider)

        travelerPicker.childCountSelector.travelerMinus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Remove.Child", mockAnalyticsProvider)

        travelerPicker.infantCountSelector.travelerMinus.performClick()
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Remove.Infant", mockAnalyticsProvider)
    }

    @Test
    fun testSearchFormValidationWhenInvalidParams() {
        inflate()
        widget.searchViewModel = PackageSearchViewModel(activity)
        val errorNoDestinationTestSubscriber = TestObserver<Unit>()
        val errorNoOriginObservableTestSubscriber = TestObserver<Unit>()
        val errorNoDatesObservableTestSubscriber = TestObserver<Unit>()

        widget.searchViewModel.errorNoDestinationObservable.subscribe(errorNoDestinationTestSubscriber)
        widget.searchViewModel.errorNoOriginObservable.subscribe(errorNoOriginObservableTestSubscriber)
        widget.searchViewModel.errorNoDatesObservable.subscribe(errorNoDatesObservableTestSubscriber)

        widget.searchViewModel.formattedDestinationObservable.onNext("")
        widget.searchViewModel.searchObserver.onNext(Unit)

        errorNoOriginObservableTestSubscriber.assertValueCount(1)
        errorNoDestinationTestSubscriber.assertValueCount(1)
        errorNoDatesObservableTestSubscriber.assertValueCount(1)

        assertNotNull(widget.destinationCardView.compoundDrawablesRelative[2])
        assertNotNull(widget.originCardView.compoundDrawablesRelative[2])
        assertNotNull(widget.calendarWidgetV2.compoundDrawablesRelative[2])

        assertEquals(widget.getDestinationSearchBoxPlaceholderText(), widget.destinationCardView.text)
        assertEquals(widget.getDestinationSearchBoxPlaceholderText(), widget.destinationCardView.contentDescription)
    }

    @Test
    fun testSearchFormValidationWhenValidParams() {
        inflate()
        widget.searchViewModel = PackageSearchViewModel(activity)
        widget.searchViewModel.getParamsBuilder()
                .origin(SuggestionV4())
                .destination(SuggestionV4())
                .startDate(LocalDate.now())
                .adults(1)
                .children(listOf(1, 2, 3))
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams
        val errorNoDestinationTestSubscriber = TestObserver<Unit>()
        val errorNoOriginObservableTestSubscriber = TestObserver<Unit>()
        val errorNoDatesObservableTestSubscriber = TestObserver<Unit>()

        widget.searchViewModel.errorNoDestinationObservable.subscribe(errorNoDestinationTestSubscriber)
        widget.searchViewModel.errorNoOriginObservable.subscribe(errorNoOriginObservableTestSubscriber)
        widget.searchViewModel.errorNoDatesObservable.subscribe(errorNoDatesObservableTestSubscriber)

        widget.searchViewModel.searchObserver.onNext(Unit)
        widget.searchViewModel.formattedOriginObservable.onNext("test")
        widget.searchViewModel.formattedDestinationObservable.onNext("test")
        widget.searchViewModel.hasValidDatesObservable.onNext(true)

        errorNoOriginObservableTestSubscriber.assertValueCount(0)
        errorNoDestinationTestSubscriber.assertValueCount(0)
        errorNoDatesObservableTestSubscriber.assertValueCount(0)

        assertNull(widget.destinationCardView.compoundDrawablesRelative[2])
        assertNull(widget.originCardView.compoundDrawablesRelative[2])
        assertNull(widget.calendarWidgetV2.compoundDrawablesRelative[2])
    }

    @Test
    fun testCalendarWidgetIsNotDisplayedAutomaticallyForExpiredSavedParams() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)

        widget.searchViewModel = packageSearchViewModel
        widget.searchViewModel.performSearchObserver.onNext(getDummyPackageSearchParams(-2, -1))
        widget.searchViewModel.previousSearchParamsObservable.onNext(getDummyPackageSearchParams(-2, -1))

        val dialog = (activity as FragmentActivity).supportFragmentManager.findFragmentByTag(Constants.TAG_CALENDAR_DIALOG)
        assertNull(dialog)
    }

    @Test
    fun testCalendarWidgetIsNotDisplayedWithTalkback() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val mockedModel = spy(packageSearchViewModel)
        `when`(mockedModel.isTalkbackActive()).thenReturn(true)

        widget.searchViewModel = mockedModel

        widget.searchViewModel.originLocationObserver.onNext(getDummySuggestion())
        widget.searchViewModel.destinationLocationObserver.onNext(getDummySuggestion())

        val dialog = (activity as FragmentActivity).supportFragmentManager.findFragmentByTag(Constants.TAG_CALENDAR_DIALOG)
        assertNull(dialog)
        assertFalse(widget.searchButton.isEnabled)
    }

    @Test
    fun testSearchButtonIsEnabledWithValidParamsWhenTalkbackIsEnabled() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val mockedModel = spy(packageSearchViewModel)
        `when`(mockedModel.isTalkbackActive()).thenReturn(true)

        widget.searchViewModel = mockedModel

        widget.searchViewModel.performSearchObserver.onNext(getDummyPackageSearchParams(2, 1))
        widget.searchViewModel.previousSearchParamsObservable.onNext(getDummyPackageSearchParams(2, 1))

        assertTrue(widget.searchButton.isEnabled)
    }

    @Test
    fun verifyPreviousSearchParamsAreRetained() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val calendarWidget = widget.calendarWidgetV2

        widget.searchViewModel = packageSearchViewModel
        widget.searchViewModel.performSearchObserver.onNext(getDummyPackageSearchParams(1, 2))
        widget.searchViewModel.previousSearchParamsObservable.onNext(getDummyPackageSearchParams(1, 2))

        assertFalse(calendarWidget.calendarDialog?.isVisible ?: false)
        val dialog = (activity as FragmentActivity).supportFragmentManager.findFragmentByTag(Constants.TAG_CALENDAR_DIALOG)
        assertNull(dialog)
    }

    @Test
    fun verifyInfantInSeatParamWhenPreviousSearchParamsAreRetained() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val infantInSeatObservable = TestObserver.create<Boolean>()

        widget.searchViewModel = packageSearchViewModel
        val travelerPicker = widget.travelerWidgetV2.travelerDialogView.findViewById<FlightTravelerPickerView>(R.id.flight_traveler_view)
        travelerPicker.viewmodel = FlightTravelerPickerViewModel(activity)

        travelerPicker.getViewModel().infantInSeatObservable.subscribe(infantInSeatObservable)

        var prevSearchParams = PackageTestUtil.getPackageSearchParams(childCount = emptyList(), infantInLap = false)
        widget.searchViewModel.performSearchObserver.onNext(prevSearchParams)
        widget.searchViewModel.previousSearchParamsObservable.onNext(prevSearchParams)

        assertEquals(0, infantInSeatObservable.valueCount())

        prevSearchParams = getDummyPackageSearchParams(1, 2)
        widget.searchViewModel.performSearchObserver.onNext(prevSearchParams)
        widget.searchViewModel.previousSearchParamsObservable.onNext(prevSearchParams)

        infantInSeatObservable.assertValue(true)
        assertFalse(travelerPicker.viewmodel.isInfantInLapObservable.value)

        prevSearchParams = PackageTestUtil.getPackageSearchParams(childCount = listOf(1, 2), infantInLap = true)
        widget.searchViewModel.performSearchObserver.onNext(prevSearchParams)
        widget.searchViewModel.previousSearchParamsObservable.onNext(prevSearchParams)

        assertFalse(infantInSeatObservable.values()[1])
        assertTrue(travelerPicker.viewmodel.isInfantInLapObservable.value)
    }

    @Test
    fun testMaxDurationErrorIsDisplayed() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val errorObserver = TestObserver.create<String>()
        widget.searchViewModel = PackageSearchViewModel(activity)
        widget.searchViewModel = packageSearchViewModel

        widget.searchViewModel.errorMaxDurationObservable.subscribe(errorObserver)

        val params = getDummyPackageSearchParams(1, 30, getDummySuggestion(), getDummySuggestion("Delhi"))
        widget.searchViewModel.performSearchObserver.onNext(params)
        widget.searchViewModel.previousSearchParamsObservable.onNext(params)
        widget.searchViewModel.searchObserver.onNext(Unit)

        errorObserver.assertValue(RuntimeEnvironment.application.getString(R.string.hotel_search_range_error_TEMPLATE, PackageCalendarRules(activity).getMaxSearchDurationDays()))
    }

    @Test
    fun testMaxRangeErrorIsDisplayed() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val errorObserver = TestObserver.create<String>()
        widget.searchViewModel = PackageSearchViewModel(activity)
        widget.searchViewModel = packageSearchViewModel

        widget.searchViewModel.errorMaxRangeObservable.subscribe(errorObserver)

        val params = getDummyPackageSearchParams(320, 12, getDummySuggestion(), getDummySuggestion("Delhi"))
        widget.searchViewModel.performSearchObserver.onNext(params)
        widget.searchViewModel.previousSearchParamsObservable.onNext(params)
        widget.searchViewModel.searchObserver.onNext(Unit)

        errorObserver.assertValue(RuntimeEnvironment.application.getString(R.string.error_date_too_far, PackageCalendarRules(activity).getMaxDateRange()))
    }

    @Test
    fun verifyCalendarWidgetIsShownAfterSelectingOriginDestination() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)
        val calendarWidget = widget.calendarWidgetV2
        widget.searchViewModel = packageSearchViewModel

        widget.searchViewModel.setOriginText(getDummySuggestion())
        widget.searchViewModel.setDestinationText(getDummySuggestion())

        assertTrue(calendarWidget.calendarDialog?.isShowInitiated ?: false)
        val dialog = (activity as FragmentActivity).supportFragmentManager.findFragmentByTag(Constants.TAG_CALENDAR_DIALOG)
        assertNotNull(dialog)
    }

    @Test
    fun verifySuggestionHistoryFileName() {
        inflate()
        val packageSearchViewModel = PackageSearchViewModel(activity)

        widget.searchViewModel = packageSearchViewModel
        widget.showSuggestionState(true)
        widget.searchViewModel.originLocationObserver.onNext(getDummySuggestion())
        assertEquals(SuggestionV4Utils.RECENT_PACKAGE_DEPARTURE_SUGGESTIONS_FILE, widget.getSuggestionHistoryFileName())

        widget.showSuggestionState(false)
        widget.searchViewModel.destinationLocationObserver.onNext(getDummySuggestion())
        assertEquals(SuggestionV4Utils.RECENT_PACKAGE_ARRIVAL_SUGGESTIONS_FILE, widget.getSuggestionHistoryFileName())
    }

    @Test
    fun verifyTabsVisibilityWhenFHCIsInControl() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesWebviewFHC)
        inflate()
        assertEquals(View.GONE, widget.tabs.visibility)
    }

    @Test
    fun verifyTabsVisibilityWhenFHCIsBucketed() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesWebviewFHC)
        inflate()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.searchViewModel = PackageSearchViewModel(activity)

        assertEquals(View.VISIBLE, widget.tabs.visibility)
        assertEquals(2, widget.tabs.tabCount)
        assertEquals(RuntimeEnvironment.application.getString(R.string.nav_hotel_plus_flight), widget.tabs.getTabAt(0)?.text)
        assertEquals(RuntimeEnvironment.application.getString(R.string.nav_hotel_plus_flight_plus_car), widget.tabs.getTabAt(1)?.text)

        widget.tabs.getTabAt(1)?.select()

        assertFalse(widget.searchViewModel.isFHPackageSearch)
        OmnitureTestUtils.assertLinkTracked("FHC tab", "App.Package.DS.FHC.TabClicked", mockAnalyticsProvider)

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.tabs.getTabAt(0)?.select()
        widget.tabs.getTabAt(1)?.select()

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testCabinClassView() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppPackagesFFPremiumClass)
        inflate()

        val cabinClassObserver = TestObserver.create<FlightServiceClassType.CabinCode>()

        widget.searchViewModel = PackageSearchViewModel(activity)

        widget.flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.subscribe(cabinClassObserver)

        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .flightCabinClass("business")
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        widget.searchViewModel.previousSearchParamsObservable.onNext(params)

        cabinClassObserver.assertValues(FlightServiceClassType.CabinCode.COACH, FlightServiceClassType.CabinCode.BUSINESS)
        assertEquals(LineOfBusiness.PACKAGES, widget.flightCabinClassWidget.lob)
    }

    private fun getDummyPackageSearchParams(startDateOffset: Int, endDateOffset: Int, origin: SuggestionV4 = getDummySuggestion(),
                                            destination: SuggestionV4 = getDummySuggestion()): PackageSearchParams {
        val startDate = LocalDate.now().plusDays(startDateOffset)
        val endDate = startDate.plusDays(endDateOffset)

        val paramsBuilder = PackageSearchParams.Builder(26, 369)
                .infantSeatingInLap(false)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1)
                .children(listOf(1, 2, 3))
                .endDate(endDate) as PackageSearchParams.Builder

        return paramsBuilder.build()
    }

    private fun getDummySuggestion(name: String = "London"): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = name
        suggestion.regionNames.fullName = name
        suggestion.regionNames.shortName = name
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = name
        suggestion.hierarchyInfo!!.airport!!.multicity = name
        return suggestion
    }

    private fun inflate() {
        widget = LayoutInflater.from(activity).inflate(R.layout.test_packages_search_presenter,
                null) as PackageSearchPresenter
    }
}
