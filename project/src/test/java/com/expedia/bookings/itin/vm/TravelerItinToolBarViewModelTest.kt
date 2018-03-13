package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerToolBarViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class TravelerItinToolBarViewModelTest {
    private lateinit var activity: Activity
    private lateinit var sut: ItinToolbarViewModel
    lateinit var context: Context

    val toolbarTitleSubscriber = TestObserver<String>()
    val toolbarSubTitleSubscriber = TestObserver<String>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinTravelerToolBarViewModel(activity)
        context = RuntimeEnvironment.application
    }

    @Test
    fun testUpdateWidget() {
        sut.toolbarTitleSubject.subscribe(toolbarTitleSubscriber)
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleSubscriber)
        toolbarTitleSubscriber.assertNoValues()
        toolbarSubTitleSubscriber.assertNoValues()
        sut.updateWidget(ItinToolbarViewModel.ToolbarParams(
                "cool title",
                "really cool sub",
                false
        ))
        toolbarTitleSubscriber.assertValue("cool title")
        toolbarSubTitleSubscriber.assertValue("really cool sub")
    }
}
