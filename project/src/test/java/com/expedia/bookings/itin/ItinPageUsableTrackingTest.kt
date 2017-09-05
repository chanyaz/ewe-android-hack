package com.expedia.bookings.itin

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class ItinPageUsableTrackingTest {
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var sut: ItinPageUsableTracking

    @Before
    fun before() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut = ItinPageUsableTracking()
    }

    @Test
    fun whenAllDataIsPresentThenTrack() {
        sut.markSuccessfulStartTime(0)
        sut.markTripResultsUsable(100)
        sut.trackIfReady(listOfCardData())

        OmnitureTestUtils.assertStateTrackedWithEventsAndEvars(mockAnalyticsProvider, "App.Itinerary", "event63,event220,event221=0.10")
    }

    @Test
    fun whenStartTimeIsMissingDoNotTrack() {
        sut.markTripResultsUsable(100)
        sut.trackIfReady(listOfCardData())

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun whenEndTimeIsMissingDoNotTrack() {
        sut.markSuccessfulStartTime(0)
        sut.trackIfReady(listOfCardData())

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun whenTripsListIsEmptyDoNotTrack() {
        sut.markSuccessfulStartTime(0)
        sut.markTripResultsUsable(100)
        sut.trackIfReady(emptyList())

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    private fun listOfCardData() = listOf(ItinCardData(TripComponent()))
}