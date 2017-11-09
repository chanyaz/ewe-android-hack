package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber

@RunWith (RobolectricRunner::class)
class FlightItinToolbarViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: ItinToolbarViewModel

    val toolbarTitleSubscriber = TestSubscriber<String>()
    val toolbarSubTitleSubscriber = TestSubscriber<String>()
    val shareIconSubscriber = TestSubscriber<Boolean>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinToolbarViewModel()
    }

    @Test
    fun testUpdateWidget() {
        sut.toolbarTitleSubject.subscribe(toolbarTitleSubscriber)
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleSubscriber)
        sut.shareIconVisibleSubject.subscribe(shareIconSubscriber)

        toolbarTitleSubscriber.assertNoValues()
        toolbarSubTitleSubscriber.assertNoValues()
        shareIconSubscriber.assertNoValues()
        val destination = Phrase.from(activity, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", "Vancouver").format().toString()
        sut.updateWidget(ItinToolbarViewModel.ToolbarParams(destination, "Aug 21", true))
        toolbarTitleSubscriber.assertValue("Flight to Vancouver")
        toolbarSubTitleSubscriber.assertValue("Aug 21")
        shareIconSubscriber.assertValue(true)
    }
}