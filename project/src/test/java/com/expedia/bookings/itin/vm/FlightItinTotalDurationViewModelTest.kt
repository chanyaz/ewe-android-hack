package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightItinTotalDurationViewModelTest {

    lateinit private var activity: Activity
    lateinit private var sut: FlightItinTotalDurationViewModel

    private val createTotalDurationWidgetSubscriber = TestSubscriber<ItinTimeDurationViewModel.TimeDurationWidgetParams>()

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
        createTotalDurationWidgetSubscriber.assertValue(ItinTimeDurationViewModel.TimeDurationWidgetParams("Total duration: 2h 35m", "Total duration: 2 hour 35 minutes", null))
    }
}