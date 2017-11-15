package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinManageBookingViewModelTest {
    lateinit private var context: Context
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinManageBookingViewModel

    val itinCardDataValidSubscriber = TestSubscriber<Unit>()
    val itinCardDataSubscriber = TestSubscriber<ItinCardDataFlight>()
    val updateToolbarSubscriber = TestSubscriber<ItinToolbarViewModel.ToolbarParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinManageBookingViewModel(activity, "TEST_ITIN_ID")
        context = RuntimeEnvironment.application
    }

    @Test
    fun testItinCardDataFlightForNotNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        sut.itinCardDataFlightObservable.subscribe(itinCardDataSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        `when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertNoValues()
        itinCardDataSubscriber.assertValueCount(1)
        itinCardDataSubscriber.assertValue(testItinCardData)
        assertEquals(testItinCardData, sut.itinCardDataFlight)
    }

    @Test
    fun testItinCardDataFlightNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        `when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        sut.itineraryManager = mockItinManager

        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertValue(Unit)
        itinCardDataSubscriber.assertValueCount(0)
    }

    @Test
    fun testToolBar() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        `when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()
        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", "Las Vegas").format().toString()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

}