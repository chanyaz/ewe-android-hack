package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver

@RunWith (RobolectricRunner::class)
class FlightItinToolbarViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: ItinToolbarViewModel

    val toolbarTitleSubscriber = TestObserver<String>()
    val toolbarSubTitleSubscriber = TestObserver<String>()
    val shareIconSubscriber = TestObserver<Boolean>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinToolbarViewModel(activity)
    }

    @Test
    fun testUpdateWidget() {
        sut.toolbarTitleSubject.subscribe(toolbarTitleSubscriber)
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleSubscriber)
        sut.shareIconVisibleSubject.subscribe(shareIconSubscriber)

        toolbarTitleSubscriber.assertNoValues()
        toolbarSubTitleSubscriber.assertNoValues()
        shareIconSubscriber.assertNoValues()
        sut.updateWidget(ItinToolbarViewModel.ToolbarParams("Vancouver", "Aug 21", true))
        toolbarTitleSubscriber.assertValue("Flight to Vancouver")
        toolbarSubTitleSubscriber.assertValue("Aug 21")
        shareIconSubscriber.assertValue(true)
    }
}