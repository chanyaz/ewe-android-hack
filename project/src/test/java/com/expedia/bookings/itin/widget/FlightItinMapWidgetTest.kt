package com.expedia.bookings.itin.widget

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.vm.FlightItinMapWidgetViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Waypoint
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class FlightItinMapWidgetTest {

    lateinit var activity: FragmentActivity
    lateinit var sut: FlightItinMapWidget
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    var leftButtonObservable = TestSubscriber<Boolean>()
    var rightButtonObservable = TestSubscriber<Boolean>()

    var leftButtonDrawableObservable = TestSubscriber<Int>()
    var rightButtonDrawableObservable = TestSubscriber<Int>()

    var leftButtonTextObservable = TestSubscriber<String>()
    var rightButtonTextObservable = TestSubscriber<String>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        sut = LayoutInflater.from(activity).inflate(R.layout.test_flight_itin_map_widget, null) as FlightItinMapWidget
        sut.viewModel = FlightItinMapWidgetViewModel()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testLeftButtonSetupOriginDestinationNull() {
        sut.itinActionsButtons.viewModel.leftButtonVisibilityObservable.subscribe(leftButtonObservable)
        sut.itinActionsButtons.viewModel.leftButtonDrawableObservable.subscribe(leftButtonDrawableObservable)
        sut.itinActionsButtons.viewModel.leftButtonTextObservable.subscribe(leftButtonTextObservable)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("")
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("")
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        leftButtonObservable.assertValueCount(0)
        leftButtonDrawableObservable.assertValueCount(0)
        leftButtonTextObservable.assertValueCount(0)
    }

    @Test
    fun testLeftButtonSetupOriginNull() {
        sut.itinActionsButtons.viewModel.leftButtonVisibilityObservable.subscribe(leftButtonObservable)
        sut.itinActionsButtons.viewModel.leftButtonDrawableObservable.subscribe(leftButtonDrawableObservable)
        sut.itinActionsButtons.viewModel.leftButtonTextObservable.subscribe(leftButtonTextObservable)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("")
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        leftButtonObservable.assertValueCount(1)
        leftButtonObservable.assertValue(true)
        leftButtonDrawableObservable.assertValueCount(1)
        leftButtonDrawableObservable.assertValue(R.drawable.itin_flight_terminal_map_icon)
        leftButtonTextObservable.assertValueCount(1)
        leftButtonTextObservable.assertValue("Terminal Maps")
    }

    @Test
    fun testLeftButtonSetup() {
        sut.itinActionsButtons.viewModel.leftButtonVisibilityObservable.subscribe(leftButtonObservable)
        sut.itinActionsButtons.viewModel.leftButtonDrawableObservable.subscribe(leftButtonDrawableObservable)
        sut.itinActionsButtons.viewModel.leftButtonTextObservable.subscribe(leftButtonTextObservable)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        leftButtonObservable.assertValueCount(1)
        leftButtonObservable.assertValue(true)
        leftButtonDrawableObservable.assertValueCount(1)
        leftButtonDrawableObservable.assertValue(R.drawable.itin_flight_terminal_map_icon)
        leftButtonTextObservable.assertValueCount(1)
        leftButtonTextObservable.assertValue("Terminal Maps")
    }

    @Test
    fun testRightButtonSetupOriginNull() {
        sut.itinActionsButtons.viewModel.rightButtonVisibilityObservable.subscribe(rightButtonObservable)
        sut.itinActionsButtons.viewModel.rightButtonDrawableObservable.subscribe(rightButtonDrawableObservable)
        sut.itinActionsButtons.viewModel.rightButtonTextObservable.subscribe(rightButtonTextObservable)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("")
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        rightButtonObservable.assertValueCount(0)
        rightButtonDrawableObservable.assertValueCount(0)
        rightButtonTextObservable.assertValueCount(0)
    }

    @Test
    fun testRightButtonSetup() {
        sut.itinActionsButtons.viewModel.rightButtonVisibilityObservable.subscribe(rightButtonObservable)
        sut.itinActionsButtons.viewModel.rightButtonDrawableObservable.subscribe(rightButtonDrawableObservable)
        sut.itinActionsButtons.viewModel.rightButtonTextObservable.subscribe(rightButtonTextObservable)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        rightButtonObservable.assertValueCount(1)
        rightButtonObservable.assertValue(true)
        rightButtonDrawableObservable.assertValueCount(1)
        rightButtonDrawableObservable.assertValue(R.drawable.ic_directions_icon_cta_button)
        rightButtonTextObservable.assertValueCount(1)
        rightButtonTextObservable.assertValue("Directions")
    }

    @Test
    fun testLeftButtonClick() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)
        sut.itinActionsButtons.viewModel.leftButtonClickedObservable.onNext(Unit)

        val bottomSheet = activity.supportFragmentManager.findFragmentByTag(sut.TERMINAL_MAP_BOTTOM_SHEET_TAG)
        assertNotNull(bottomSheet)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.TerminalMaps", mockAnalyticsProvider)
    }

    @Test
    fun testRightButtonClick() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)
        sut.itinActionsButtons.viewModel.rightButtonClickedObservable.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Itinerary Action", "App.Itinerary.Flight.Directions", mockAnalyticsProvider)
    }

    @Test
    fun testWidgetVisibile() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        assertEquals(View.VISIBLE, sut.cardView.visibility)
    }

    @Test
    fun testWidgetNotVisibile() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.flightLeg.segments[0].originWaypoint = TestWayPoint("")
        testItinCardData.flightLeg.segments[0].destinationWaypoint = TestWayPoint("")
        sut.viewModel.itinCardDataObservable.onNext(testItinCardData)

        assertEquals(View.GONE, sut.cardView.visibility)
    }

    class TestWayPoint(val code: String) : Waypoint(ACTION_UNKNOWN) {
        override fun getAirport(): Airport? {
            val airport = Airport()
            if (code.isEmpty()) {
                return null
            } else {
                airport.mAirportCode = code
                return airport
            }
        }
    }
}