package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.itin.common.ItinTimeDurationViewModel
import com.expedia.bookings.itin.flight.details.FlightItinTotalDurationViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class FlightItinTotalDurationViewModelTest {

    private lateinit var activity: Activity
    private lateinit var sut: FlightItinTotalDurationViewModel

    private val createTotalDurationWidgetSubscriber = TestObserver<ItinTimeDurationViewModel.TimeDurationWidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinTotalDurationViewModel(activity)
    }

    @Test
    fun testUpdateDurationWidget() {
        sut.createTimeDurationWidgetSubject.subscribe(createTotalDurationWidgetSubscriber)

        createTotalDurationWidgetSubscriber.assertNoValues()
        sut.updateWidget("PT2H35M")
        createTotalDurationWidgetSubscriber.assertValueCount(1)
        createTotalDurationWidgetSubscriber.assertValue(ItinTimeDurationViewModel.TimeDurationWidgetParams("2h 35m", "2 hour 35 minutes", null, ItinTimeDurationViewModel.DurationType.TOTAL_DURATION))
    }
}
