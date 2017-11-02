package com.expedia.bookings.test

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.AdvanceSearchCheckableFilter
import com.expedia.bookings.widget.FlightAdvanceSearchWidget
import com.expedia.vm.flights.AdvanceSearchFilter
import com.expedia.vm.flights.FlightAdvanceSearchViewModel
import com.mobiata.android.util.SettingUtils
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightAdvanceSearchViewModelTest {

    private var widget: FlightAdvanceSearchWidget by Delegates.notNull()
    private var viewModel: FlightAdvanceSearchViewModel by Delegates.notNull()

    private var activity: Activity by Delegates.notNull()

    var server: MockWebServer = MockWebServer()
        @Rule get

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightAdvanceSearch)
        Ui.getApplication(activity).defaultFlightComponents()
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_advanced_search_widget,
                null) as FlightAdvanceSearchWidget
        viewModel = FlightAdvanceSearchViewModel()
        widget.viewModel = viewModel
    }

    @Test
    fun testAdvanceSearchWidgetCheckBox() {
        val nonStopCheckBoxObserver = (widget.advanceSearchFilterContainer.getChildAt(0) as AdvanceSearchCheckableFilter).checkObserver
        val refundableCheckBoxObserver = (widget.advanceSearchFilterContainer.getChildAt(1) as AdvanceSearchCheckableFilter).checkObserver
        val advanceSearchFilterTestSubscriber = TestObserver<AdvanceSearchFilter>()
        viewModel.selectAdvancedSearch.subscribe(advanceSearchFilterTestSubscriber)
        assertFalse(viewModel.isAdvanceSearchFilterSelected)

        //When only non stop filter is checked on
        nonStopCheckBoxObserver.onNext(Unit)
        assertEquals(true, advanceSearchFilterTestSubscriber.values().last().isChecked)
        assertTrue(viewModel.isAdvanceSearchFilterSelected)

        refundableCheckBoxObserver.onNext(Unit)
        assertEquals(true, advanceSearchFilterTestSubscriber.values().last().isChecked)
        nonStopCheckBoxObserver.onNext(Unit)
        assertEquals(false, advanceSearchFilterTestSubscriber.values().last().isChecked)
        assertTrue(viewModel.isAdvanceSearchFilterSelected)
    }
}