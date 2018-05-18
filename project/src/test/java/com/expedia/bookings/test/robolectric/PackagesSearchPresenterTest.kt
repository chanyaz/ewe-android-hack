package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.packages.presenter.PackageSearchPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerPickerView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        widget = LayoutInflater.from(activity).inflate(R.layout.test_packages_search_presenter,
                null) as PackageSearchPresenter
    }

    @Test
    fun testNewTravelerPickerSelectionOperations() {
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
}
