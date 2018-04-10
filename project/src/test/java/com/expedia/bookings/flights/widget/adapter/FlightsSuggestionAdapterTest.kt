package com.expedia.bookings.flights.widget.adapter

import com.expedia.bookings.flights.vm.FlightsSuggestionViewModel
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.FlightSuggestionAdapterViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightsSuggestionAdapterTest {

    private val context = RuntimeEnvironment.application
    @Test
    fun testSuggestionVMType() {
        val adapter = FlightsSuggestionAdapter(FlightSuggestionAdapterViewModel(context, Mockito.mock(SuggestionV4Services::class.java), false, null))
        assertTrue(adapter.getSuggestionViewModel(context) is FlightsSuggestionViewModel)
    }
}
