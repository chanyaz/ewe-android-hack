package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import org.mockito.Mockito.`when` as whenever

@RunWith(RobolectricRunner::class)
class FlightItinDetailsViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinDetailsViewModel

    val itinCardDataValidSubscriber = TestSubscriber<Unit>()
    val updateToolbarSubscriber = TestSubscriber<ItinToolbarViewModel.ToolbarParams>()
    val clearLegSummaryContainerSubscriber = TestSubscriber<Unit>()
    val createLegSummaryWidgetsSubscriber = TestSubscriber<FlightItinSegmentSummaryViewModel.SummaryWidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinDetailsViewModel(activity, "TEST_ITIN_ID")
    }

    @Test
    fun testUpdateItinCardDataFlightNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertValue(Unit)
    }

    @Test
    fun testUpdateItinCardDataFlightNotNull() {
        sut.itinCardDataNotValidSubject.subscribe(itinCardDataValidSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.updateItinCardDataFlight()
        itinCardDataValidSubscriber.assertNoValues()
        assertEquals(testItinCardData, sut.itinCardDataFlight)
    }

    @Test
    fun testOnResume() {
        sut.updateToolbarSubject.subscribe(updateToolbarSubscriber)

        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val now = DateTime.now()
        val startTime = now.plusDays(30)
        val startDate = LocaleBasedDateFormatUtils.dateTimeToMMMd(startTime).capitalize()
        whenever(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinCardData)
        sut.itineraryManager = mockItinManager
        sut.onResume()
        updateToolbarSubscriber.assertValue(ItinToolbarViewModel.ToolbarParams("Las Vegas", startDate, true))
    }

    @Test
    fun testUpdateLegSummaryWidget() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build()
        val departureTime = testItinCardData.flightLeg.segments[0].originWaypoint.bestSearchDateTime
        val arrivalTime = testItinCardData.flightLeg.segments[0].destinationWaypoint.bestSearchDateTime
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(1)
        createLegSummaryWidgetsSubscriber.assertValue(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 681",
                "COMPASS AIRLINES",
                departureTime,
                arrivalTime,
                "SFO",
                "San Francisco",
                "LAS",
                "Las Vegas"
        ))
    }

    @Test
    fun testUpdateLegSummaryWidgetMultiSegment() {
        sut.clearLegSummaryContainerSubject.subscribe(clearLegSummaryContainerSubscriber)
        sut.createSegmentSummaryWidgetsSubject.subscribe(createLegSummaryWidgetsSubscriber)

        val testItinCardData = ItinCardDataFlightBuilder().build(multiSegment = true)
        val departureTimeSegment1 = testItinCardData.flightLeg.segments[0].originWaypoint.bestSearchDateTime
        val arrivalTimeSegment1 = testItinCardData.flightLeg.segments[0].destinationWaypoint.bestSearchDateTime
        val departureTimeSegment2 = testItinCardData.flightLeg.segments[1].originWaypoint.bestSearchDateTime
        val arrivalTimeSegment2 = testItinCardData.flightLeg.segments[1].destinationWaypoint.bestSearchDateTime
        sut.itinCardDataFlight = testItinCardData
        sut.updateLegSummaryWidget()
        clearLegSummaryContainerSubscriber.assertValueCount(1)
        clearLegSummaryContainerSubscriber.assertValue(Unit)
        createLegSummaryWidgetsSubscriber.assertValueCount(2)
        createLegSummaryWidgetsSubscriber.assertValues(FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 1796",
                null,
                departureTimeSegment1,
                arrivalTimeSegment1,
                "SFO",
                "San Francisco",
                "EWR",
                "Newark"
        ), FlightItinSegmentSummaryViewModel.SummaryWidgetParams(
                "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smUA.gif",
                "United Airlines 1489",
                null,
                departureTimeSegment2,
                arrivalTimeSegment2,
                "EWR",
                "Newark",
                "PBI",
                "West Palm Beach"
        ))
    }
}