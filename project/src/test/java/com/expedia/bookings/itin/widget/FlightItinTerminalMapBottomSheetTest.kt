package com.expedia.bookings.itin.widget

import android.support.v4.app.FragmentActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.TerminalMapActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinTerminalMapBottomSheetTest {
    lateinit var activity: FragmentActivity
    lateinit var sut: FlightItinTerminalMapBottomSheet
    lateinit var departureAirportText: TextView
    lateinit var arrivalAirportText: TextView

    @Test
    fun testCodesAvailable() {
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", "SEA")
        SupportFragmentTestUtil.startFragment(sut)
        departureAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_departure_airport)
        arrivalAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_arrival_airport)

        assertEquals(View.VISIBLE, departureAirportText.visibility)
        assertEquals(View.VISIBLE, arrivalAirportText.visibility)
        assertEquals("SFO terminal map", departureAirportText.text.toString())
        assertEquals("SEA terminal map", arrivalAirportText.text.toString())
    }

    @Test
    fun testCodeNull() {
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", null)
        SupportFragmentTestUtil.startFragment(sut)
        assertNotNull(sut)
        departureAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_departure_airport)
        arrivalAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_arrival_airport)

        assertEquals(View.VISIBLE, departureAirportText.visibility)
        assertEquals(View.GONE, arrivalAirportText.visibility)
        assertEquals("SFO terminal map", departureAirportText.text.toString())
    }

    @Test
    fun testCodeEmpty() {
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", "")
        SupportFragmentTestUtil.startFragment(sut)
        departureAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_departure_airport)
        arrivalAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_arrival_airport)

        assertEquals(View.VISIBLE, departureAirportText.visibility)
        assertEquals(View.GONE, arrivalAirportText.visibility)
        assertEquals("SFO terminal map", departureAirportText.text.toString())
    }

    @Test
    fun testCodeNoMap() {
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", "PBI")
        SupportFragmentTestUtil.startFragment(sut)
        departureAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_departure_airport)
        arrivalAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_arrival_airport)

        assertEquals(View.VISIBLE, departureAirportText.visibility)
        assertEquals(View.GONE, arrivalAirportText.visibility)
        assertEquals("SFO terminal map", departureAirportText.text.toString())
    }

    @Test
    fun testClick() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", "")
        SupportFragmentTestUtil.startFragment(sut)
        departureAirportText = sut.dialog.findViewById<TextView>(R.id.terminal_map_departure_airport)

        departureAirportText.performClick()
        val intent = shadowOf(activity).nextStartedActivity
        val shadowIntent = shadowOf(intent)
        assertEquals(TerminalMapActivity::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testCheckIfMapIsAvailable() {
        sut = FlightItinTerminalMapBottomSheet.newInstance("SFO", "")
        assertTrue(sut.checkIfMapIsAvailable("SFO"))
        assertFalse(sut.checkIfMapIsAvailable("PBI"))
        assertFalse(sut.checkIfMapIsAvailable(""))
    }
}
