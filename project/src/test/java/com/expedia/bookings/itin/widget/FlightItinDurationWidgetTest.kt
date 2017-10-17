package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinDurationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinDurationWidgetTest {
    lateinit var context: Context
    lateinit var sut: FlightItinDurationWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_flight_itin_duration, null) as FlightItinDurationWidget
        sut.viewModel = FlightItinDurationViewModel()
    }

    @Test
    fun testTotalDurationText() {
        sut.viewModel.updateWidget(FlightItinDurationViewModel.WidgetParams("Total Duration: 1d 12h 15m",
                "Total Duration: 1 day 12 hours 15 minutes"))

        assertEquals("Total Duration: 1d 12h 15m", sut.flightDurationTextview.text)
        assertEquals("Total Duration: 1 day 12 hours 15 minutes", sut.flightDurationTextview.contentDescription)
    }

}