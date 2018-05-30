package com.expedia.bookings.itin.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinTimingsWidgetTest {
    lateinit var sut: ItinTimingsWidget<ItinLx>
    private lateinit var mockVM: MockViewModel<ItinLx>
    lateinit var context: Context

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_itin_timings_widget, null) as ItinTimingsWidget<ItinLx>
        mockVM = MockViewModel()
        sut.setupViewModel(mockVM)
    }

    @Test
    fun testVisibilityTextAndContentDesc() {
        assertEquals(View.GONE, sut.startTitle.visibility)
        assertEquals(View.GONE, sut.startDate.visibility)
        assertEquals(View.GONE, sut.startTime.visibility)
        assertEquals(View.GONE, sut.endTitle.visibility)
        assertEquals(View.GONE, sut.endDate.visibility)
        assertEquals(View.GONE, sut.endTime.visibility)

        val arrow = sut.findViewById<ImageView>(R.id.center_arrow)
        assertEquals(context.getString(R.string.itin_flight_summary_arrow_cont_desc), arrow.contentDescription)

        assertEquals("", sut.startTitle.text)
        assertEquals("", sut.startDate.text)
        assertEquals("", sut.startTime.text)
        assertEquals("", sut.endTitle.text)
        assertEquals("", sut.endDate.text)
        assertEquals("", sut.endTime.text)

        val startTime = "startTime"
        val startTitle = "startTitle"
        val startDate = "startDate"
        val endTime = "endTime"
        val endTitle = "endTitle"
        val endDate = "endDate"

        mockVM.startTimeSubject.onNext(startTime)
        mockVM.startTitleSubject.onNext(startTitle)
        mockVM.startDateSubject.onNext(startDate)
        mockVM.endTimeSubject.onNext(endTime)
        mockVM.endTitleSubject.onNext(endTitle)
        mockVM.endDateSubject.onNext(endDate)

        assertEquals(View.VISIBLE, sut.startTitle.visibility)
        assertEquals(View.VISIBLE, sut.startDate.visibility)
        assertEquals(View.VISIBLE, sut.startTime.visibility)
        assertEquals(View.VISIBLE, sut.endTitle.visibility)
        assertEquals(View.VISIBLE, sut.endDate.visibility)
        assertEquals(View.VISIBLE, sut.endTime.visibility)

        assertEquals(startTitle, sut.startTitle.text)
        assertEquals(startDate, sut.startDate.text)
        assertEquals(startTime, sut.startTime.text)
        assertEquals(endTitle, sut.endTitle.text)
        assertEquals(endDate, sut.endDate.text)
        assertEquals(endTime, sut.endTime.text)
    }

    private class MockViewModel<T : ItinLOB> : ItinTimingsWidgetViewModel<T>() {
        override val itinObserver: LiveDataObserver<T> = LiveDataObserver { }
    }
}
