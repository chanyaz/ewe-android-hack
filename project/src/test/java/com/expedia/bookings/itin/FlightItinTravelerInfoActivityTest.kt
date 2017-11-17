package com.expedia.bookings.itin

import android.content.Context
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.activity.FlightItinTravelerInfoActivity
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinTravelerInfoActivityTest {

    lateinit var sut: FlightItinTravelerInfoActivity
    lateinit var context: Context
    private lateinit var mockAnalyticsProvider: AnalyticsProvider


    @Before
    fun setup() {
        sut = Robolectric.buildActivity(FlightItinTravelerInfoActivity::class.java).get()
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testOnFinish() {
        val sutSpy = Mockito.spy(sut)
        sutSpy.finishActivity()
        Mockito.verify(sutSpy, Mockito.times(1)).finish()
        Mockito.verify(sutSpy, Mockito.times(1)).overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    @Test
    fun testCreateIntent() {
        val testId = "988877742"
        val intent = FlightItinTravelerInfoActivity.createIntent(context, testId)
        assertEquals(intent.extras.getString("FLIGHT_ITIN_ID"), testId)
    }

    @Test
    fun testOmniture() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.trackOmniture()
        OmnitureTestUtils.assertStateTracked("App.Itinerary.Flight.TravelerInfo",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "itinerary")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Itinerary.Flight.TravelerInfo"))),
                mockAnalyticsProvider)
    }
}