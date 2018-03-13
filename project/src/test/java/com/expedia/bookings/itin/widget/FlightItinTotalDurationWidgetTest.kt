package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.flight.details.FlightItinTotalDurationViewModel
import com.expedia.bookings.itin.common.ItinTimeDurationViewModel
import com.expedia.bookings.itin.common.ItinTimeDurationWidget
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
                        .TimeDurationWidgetParams("1d 12h 15m", "1 day 12 hour 15 minutes", null, ItinTimeDurationViewModel.DurationType.TOTAL_DURATION))
        assertEquals(View.VISIBLE, sut.visibility)
        assertEquals("Total duration: 1d 12h 15m", sut.durationText.text.toString())
        assertEquals("Total duration: 1 day 12 hour 15 minutes", sut.durationText.contentDescription.toString())
    }

    @Test
    fun totalDurationTextAndContDescNullOrEmpty() {
        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("", "Total duration: 1 day 12 hour 15 minutes", null, ItinTimeDurationViewModel.DurationType.TOTAL_DURATION))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("Total duration: 1d 12h 15m", "", null, ItinTimeDurationViewModel.DurationType.TOTAL_DURATION))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject
                .onNext(ItinTimeDurationViewModel
                        .TimeDurationWidgetParams("", "", null, ItinTimeDurationViewModel.DurationType.TOTAL_DURATION))
        assertEquals(View.GONE, sut.visibility)
    }
}
