package com.expedia.bookings.itin.widget.common

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinViewReceiptViewModel
import com.expedia.bookings.itin.common.ItinViewReceiptWidget
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinViewReceiptWidgetTest {

    lateinit var activity: AppCompatActivity
    lateinit var viewReceiptWidget: ItinViewReceiptWidget

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.ItinTheme)
        viewReceiptWidget = LayoutInflater.from(activity).inflate(R.layout.test_view_receipt_widget, null) as ItinViewReceiptWidget
        viewReceiptWidget.viewModel = MockHotelItinViewReceiptViewModel()
    }

    @Test
    fun testViewReceiptButtonVisibility() {
        assertEquals(View.GONE, viewReceiptWidget.viewReceiptButton.visibility)
        viewReceiptWidget.viewModel.showReceiptSubject.onNext(Unit)
        assertEquals(View.VISIBLE, viewReceiptWidget.viewReceiptButton.visibility)
        assertEquals("View receipt Button", viewReceiptWidget.viewReceiptButton.contentDescription)
    }

    @Test
    fun testViewReceiptButtonClick() {
        val testObserver = TestObserver<Unit>()
        viewReceiptWidget.viewModel.viewReceiptClickSubject.subscribe(testObserver)
        viewReceiptWidget.viewModel.showReceiptSubject.onNext(Unit)
        testObserver.assertNoValues()
        viewReceiptWidget.viewReceiptButton.performClick()
        testObserver.assertValueCount(1)
    }

    class MockHotelItinViewReceiptViewModel : ItinViewReceiptViewModel {
        override val viewReceiptClickSubject: PublishSubject<Unit> = PublishSubject.create()
        override val webViewIntentSubject: PublishSubject<Intent> = PublishSubject.create()
        override val showReceiptSubject: PublishSubject<Unit> = PublishSubject.create()
    }
}
