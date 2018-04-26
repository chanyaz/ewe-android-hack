package com.expedia.bookings.itin.widget.common

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivityWithToolbar
import com.expedia.bookings.itin.common.AbstractToolbar
import com.expedia.bookings.itin.helpers.MockItinToolbarViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AbstractToolbarTest {

    lateinit var sut: AbstractToolbar
    private lateinit var activity: WebViewActivityWithToolbar

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(WebViewActivityWithToolbar::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        sut = LayoutInflater.from(activity).inflate(R.layout.test_abstract_toolbar, null) as AbstractToolbar
        sut.viewModel = MockItinToolbarViewModel()
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
        val testSubscriber = TestObserver.create<Unit>()
        sut.viewModel.shareIconClickedSubject.subscribe(testSubscriber)

        testSubscriber.assertNoValues()
        sut.toolbarShareIcon.performClick()
        testSubscriber.assertValueCount(1)
    }
}
