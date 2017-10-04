package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinConfirmationViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: FlightItinConfirmationViewModel
    lateinit private var context: Context

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinConfirmationViewModel(activity)
        context = RuntimeEnvironment.application
    }
    @Test
    fun testUpdateConfirmationStatus() {
        assertEquals(sut.updateConfirmationStatus(TicketingStatus.CANCELLED), "Cancelled")
        assertEquals(sut.updateConfirmationStatus(TicketingStatus.VOIDED), "Cancelled")
        assertEquals(sut.updateConfirmationStatus(TicketingStatus.INPROGRESS), "Ticketing in progress")
        assertEquals(sut.updateConfirmationStatus(TicketingStatus.COMPLETE), "Confirmation")
        assertEquals(sut.updateConfirmationStatus(TicketingStatus.NONE), "Confirmation")
    }

}
