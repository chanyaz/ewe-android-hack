package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.flightlib.data.Airport
import com.mobiata.flightlib.data.Waypoint
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever
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
    val customerSupportDetailSubscriber = TestSubscriber<ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams>()

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
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
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
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
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
        val segments = testItinCardData.flightLeg.segments
        testItinCardData.flightLeg.segments[segments.size - 1].destinationWaypoint = TestWayPoint("Las Vegas")
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", "Las Vegas").format().toString()

        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    @Test
    fun testCustomerSupportDetails() {
        sut.customerSupportDetailsSubject.subscribe(customerSupportDetailSubscriber)
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        testItinCardData.tripComponent.parentTrip.tripNumber = "123456789"
        testItinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic = "+1-866-230-3837"
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.setUp()

        val header = Phrase.from(context, R.string.itin_flight_customer_support_header_text_TEMPLATE).put("brand", "Expedia").format().toString()
        val itineraryNumb = Phrase.from(context, R.string.itin_flight_itinerary_number_TEMPLATE).put("itin_number", "123456789").format().toString()
        val customerSupportNumber = "+1-866-230-3837"
        val customerSupportButton = Phrase.from(context, R.string.itin_flight_customer_support_site_header_TEMPLATE).put("brand", "Expedia").format().toString()
        val customerSupportURL = "http://www.expedia.com/service/"
        customerSupportDetailSubscriber.assertValue(ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams(header, itineraryNumb, customerSupportNumber, customerSupportButton, customerSupportURL))
    }

    class TestWayPoint(val city: String) : Waypoint(ACTION_UNKNOWN) {
        override fun getAirport(): Airport? {
            val airport = Airport()
            if (city.isEmpty()) {
                return null
            } else {
                airport.mCity = city
                return airport
            }
        }
    }

}
