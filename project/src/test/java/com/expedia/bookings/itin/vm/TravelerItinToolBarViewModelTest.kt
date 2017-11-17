package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class TravelerItinToolBarViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: ItinToolbarViewModel
    lateinit var context: Context

    val toolbarTitleSubscriber = TestSubscriber<String>()
    val toolbarSubTitleSubscriber = TestSubscriber<String>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = TravelerItinToolBarViewModel(activity)
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