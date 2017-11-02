package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class FlightItinLayoverViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinLayoverViewModel

    private val createLayoutWidgetSubscriber = TestObserver<ItinTimeDurationViewModel.TimeDurationWidgetParams>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinLayoverViewModel(activity)
    }

    @Test
    fun testUpdateWidget1() {
        sut.createTimeDurationWidgetSubject.subscribe(createLayoutWidgetSubscriber)

        createLayoutWidgetSubscriber.assertNoValues()
        sut.updateWidget("PT7H4M")
        createLayoutWidgetSubscriber.assertValueCount(1)
        createLayoutWidgetSubscriber.assertValue(ItinTimeDurationViewModel.TimeDurationWidgetParams("7h 4m layover", "7 hour 4 minutes layover", R.drawable.itin_flight_layover_icon))
    }

    @Test
    fun testUpdateWidget2() {
        sut.createTimeDurationWidgetSubject.subscribe(createLayoutWidgetSubscriber)

        createLayoutWidgetSubscriber.assertNoValues()
        sut.updateWidget("PT1H16M")
        createLayoutWidgetSubscriber.assertValueCount(1)
        createLayoutWidgetSubscriber.assertValue(ItinTimeDurationViewModel.TimeDurationWidgetParams("1h 16m layover", "1 hour 16 minutes layover", R.drawable.itin_flight_layover_icon))
    }
}