package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoViewModel
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import kotlinx.android.synthetic.main.itin_traveler_info_widget.view.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinTravelerInfoWidgetTest {
    lateinit var context: Context
    lateinit var sut: FlightItinTravelerInfoWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_itin_traveler_info, null) as FlightItinTravelerInfoWidget
        sut.viewModel = FlightItinTravelerInfoViewModel()
    }

    @Test
    fun testTicketNumber() {
        assertEquals("", sut.travelerTicketNumber.text)
        assertEquals(View.GONE, sut.travelerTicketNumber.visibility)
        sut.viewModel.ticketNumberSubject.onNext("12345")
        assertEquals("12345", sut.travelerTicketNumber.text)
        assertEquals(View.VISIBLE, sut.travelerTicketNumber.visibility)
    }

    @Test
    fun testEmail() {
        assertEquals("", sut.travelerEmail.text)
        assertEquals(View.GONE, sut.emailContainer.visibility)
        assertEquals(View.GONE, sut.divider.visibility)
        assertEquals(View.GONE, sut.email_phone_container.visibility)
        sut.viewModel.travelerEmailSubject.onNext("test123@123.com")
        assertEquals("test123@123.com", sut.travelerEmail.text)
        assertEquals(View.VISIBLE, sut.emailContainer.visibility)
        assertEquals(View.VISIBLE, sut.divider.visibility)
        assertEquals(View.VISIBLE, sut.email_phone_container.visibility)
    }

    @Test
    fun testPhoneNumber() {
        assertEquals("", sut.travelerPhone.text)
        assertEquals(View.GONE, sut.phoneContainer.visibility)
        assertEquals(View.GONE, sut.divider.visibility)
        sut.viewModel.travelerPhoneSubject.onNext("+1 2345555542")
        assertEquals("+1 2345555542", sut.travelerPhone.text)
        assertEquals(View.VISIBLE, sut.phoneContainer.visibility)
        assertEquals(View.VISIBLE, sut.divider.visibility)
    }

    @Test
    fun testInfant() {
        assertEquals("", sut.infantText.text)
        assertEquals(View.GONE, sut.infantText.visibility)
        sut.viewModel.infantInLapSubject.onNext("Infant in Lap")
        assertEquals("Infant in Lap", sut.infantText.text)
        assertEquals(View.VISIBLE, sut.infantText.visibility)
    }

    @Test
    fun testResetWidget() {
        sut.infantText.visibility = View.VISIBLE
        sut.divider.visibility = View.VISIBLE
        sut.phoneContainer.visibility = View.VISIBLE
        sut.emailContainer.visibility = View.VISIBLE
        sut.travelerTicketNumber.visibility = View.VISIBLE
        sut.resetWidget()
        assertEquals(View.GONE, sut.travelerTicketNumber.visibility)
        assertEquals(View.GONE, sut.emailContainer.visibility)
        assertEquals(View.GONE, sut.phoneContainer.visibility)
        assertEquals(View.GONE, sut.divider.visibility)
        assertEquals(View.GONE, sut.infantText.visibility)
    }
}
