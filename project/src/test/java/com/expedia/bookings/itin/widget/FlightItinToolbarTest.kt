package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.itin.vm.FlightItinToolbarViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinToolbarTest {

    lateinit var context: Context
    lateinit var sut: ItinToolbar

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_itin_toolbar, null) as ItinToolbar
        sut.viewModel = FlightItinToolbarViewModel()
    }

    @Test
    fun testTitle() {
        sut.viewModel.toolbarTitleSubject.onNext("Las Vegas")
        assertEquals("Las Vegas", sut.toolbarTitleText.text.toString())
    }

    @Test
    fun testSubTitle() {
        sut.viewModel.toolbarSubTitleSubject.onNext("Nov 22")
        assertEquals("Nov 22", sut.toolbarSubTitleText.text.toString())
    }

    @Test
    fun testShareIconVisible() {
        sut.viewModel.shareIconVisibleSubject.onNext(false)
        assertEquals(View.GONE, sut.toolbarShareIcon.visibility)
        sut.viewModel.shareIconVisibleSubject.onNext(true)
        assertEquals(View.VISIBLE, sut.toolbarShareIcon.visibility)
    }

    @Test
    fun testShareIconClick() {
        val testSubscriber = TestSubscriber.create<Unit>()
        sut.viewModel.shareIconClickedSubject.subscribe(testSubscriber)

        testSubscriber.assertNoValues()
        sut.toolbarShareIcon.performClick()
        testSubscriber.assertValueCount(1)
    }
}