package com.expedia.bookings.itin

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.itin.common.ItinPageUsableTracking
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
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

        assertStateTracked("App.Itinerary", withEventsString("event63,event220,event221=0.10"), mockAnalyticsProvider)
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
