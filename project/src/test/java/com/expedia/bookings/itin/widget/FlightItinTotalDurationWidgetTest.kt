package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinTotalDurationViewModel
import com.expedia.bookings.itin.vm.ItinTimeDurationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinTotalDurationWidgetTest {
    lateinit var context: Context
    lateinit var sut: ItinTimeDurationWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_flight_itin_total_duration, null) as ItinTimeDurationWidget
        sut.viewModel = FlightItinTotalDurationViewModel(context)
    }

    @Test
    fun totalDurationTextAndContDesc() {
        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("Total Duration: 1d 12h 15m", "Total Duration: 1 day 12 hour 15 minutes", null))
        assertEquals(View.VISIBLE, sut.visibility)
        assertEquals("Total Duration: 1d 12h 15m", sut.durationText.text.toString())
        assertEquals("Total Duration: 1 day 12 hour 15 minutes", sut.durationText.contentDescription.toString())
    }

    @Test
    fun totalDurationTextAndContDescNullOrEmpty() {
        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("", "Total Duration: 1 day 12 hour 15 minutes", null))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("Total Duration: 1d 12h 15m", "", null))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("", "", null))
        assertEquals(View.GONE, sut.visibility)
    }
}
