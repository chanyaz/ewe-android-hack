package com.expedia.bookings.data.trips

import android.content.Context
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.CustomMatchers.Companion.hasEntries
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest.mapThat
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class ItineraryManagerTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun testHasUpcomingOrInProgressTrip() {
        val now = LocalDateTime.now().toDateTime()
        DateTimeUtils.setCurrentMillisFixed(now.millis)
        val startDates = ArrayList<DateTime>()
        startDates.add(now.minusDays(3))
        val endDates = ArrayList<DateTime>()
        endDates.add(now.plusDays(12))
        assertEquals(true, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))

        startDates.clear()
        endDates.clear()

        startDates.add(now.plusDays(12))
        endDates.add(now.plusDays(19))
        assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))

        startDates.clear()
        endDates.clear()

        startDates.add(now.plusDays(7))
        endDates.add(now.plusDays(10))
        assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallMade() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallMade()
        assertLinkTracked("Trips Call", "App.Itinerary.Call.Made", "event286", mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.LASTMINUTE, MultiBrand.TRAVELOCITY,
            MultiBrand.ORBITZ, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.CHEAPTICKETS,
            MultiBrand.EBOOKERS, MultiBrand.VOYAGES))
    fun testOmnitureTrackingTripRefreshCallSuccessWithHotel() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        SettingUtils.save(context, context.resources.getString(R.string.preference_trips_hotel_scheduled_notifications), true)
        OmnitureTracking.trackItinTripRefreshCallSuccess(true)
        assertLinkTrackedWithExposure("Trips Call", "App.Itinerary.Call.Success", "event287", "15315.0.1", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallSuccessWithoutHotel() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallSuccess(false)
        assertLinkTracked("Trips Call", "App.Itinerary.Call.Success", "event287", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallFailure() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallFailure("Trip details response is null")
        assertLinkTrackedWithError("Trips Call", "App.Itinerary.Call.Failure", "event288", "Trip details response is null", mockAnalyticsProvider)
    }

    private fun assertLinkTracked(linkName: String, rfrrId: String, event: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private fun assertLinkTrackedWithExposure(linkName: String, rfrrId: String, event: String, evar34: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event,
                "&&v34" to evar34
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private fun assertLinkTrackedWithError(linkName: String, rfrrId: String, event: String, error: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event,
                "&&c36" to error
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }
}
