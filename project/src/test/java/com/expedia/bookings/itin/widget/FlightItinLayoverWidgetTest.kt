package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinLayoverViewModel
import com.expedia.bookings.itin.vm.ItinTimeDurationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinLayoverWidgetTest {
    lateinit var context: Context
    lateinit var sut: ItinTimeDurationWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_flight_itin_layover, null) as ItinTimeDurationWidget
        sut.viewModel = FlightItinLayoverViewModel(context)
    }

    @Test
    fun testLayoverTextAndContDesc() {
        sut.viewModel.createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams("1d 15h 7m layover", "1 day 15 hour 7 minutes layover", null))
        assertEquals(View.VISIBLE, sut.visibility)
        assertEquals("1d 15h 7m layover", sut.durationText.text.toString())
        assertEquals("1 day 15 hour 7 minutes layover", sut.durationText.contentDescription.toString())
    }

    @Test
    fun testLayoverTextAndContDescNullOrEmpty() {
        sut.viewModel.createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams("", "1 day 15 hour 7 minutes layover", null))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams("1d 15h 7m layover", "", null))
        assertEquals(View.GONE, sut.visibility)

        sut.viewModel.createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams("", "", null))
        assertEquals(View.GONE, sut.visibility)
    }
}
