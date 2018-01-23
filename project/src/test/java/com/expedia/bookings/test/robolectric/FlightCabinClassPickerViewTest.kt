package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.widget.FlightCabinClassPickerView
import org.junit.Before
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightServiceClassType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFails

@RunWith(RobolectricRunner::class)
class FlightCabinClassPickerViewTest {
    var flightCabinClassPicker: FlightCabinClassPickerView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        flightCabinClassPicker = android.view.LayoutInflater.from(activity).inflate(R.layout.flight_cabin_class_picker_test, null) as FlightCabinClassPickerView
    }

    @Test
    fun testSelectedClass() {
        flightCabinClassPicker.firstClassRadioButton.performClick()
        assertEquals(FlightServiceClassType.CabinCode.FIRST, flightCabinClassPicker.getSelectedClass())

        flightCabinClassPicker.businessClassRadioButton.performClick()
        assertEquals(FlightServiceClassType.CabinCode.BUSINESS, flightCabinClassPicker.getSelectedClass())

        flightCabinClassPicker.premiumEcoClassRadioButton.performClick()
        assertEquals(FlightServiceClassType.CabinCode.PREMIUM_COACH, flightCabinClassPicker.getSelectedClass())

        flightCabinClassPicker.economyClassRadioButton.performClick()
        assertEquals(FlightServiceClassType.CabinCode.COACH, flightCabinClassPicker.getSelectedClass())

        flightCabinClassPicker.radioGroup.check(0)
        assertFails {
            flightCabinClassPicker.getSelectedClass()
        }
    }

    @Test
    fun testRadioButtonIdByClass() {
        assertEquals(flightCabinClassPicker.firstClassRadioButton.id, flightCabinClassPicker.getIdByClass(FlightServiceClassType.CabinCode.FIRST))
        assertEquals(flightCabinClassPicker.businessClassRadioButton.id, flightCabinClassPicker.getIdByClass(FlightServiceClassType.CabinCode.BUSINESS))
        assertEquals(flightCabinClassPicker.premiumEcoClassRadioButton.id, flightCabinClassPicker.getIdByClass(FlightServiceClassType.CabinCode.PREMIUM_COACH))
        assertEquals(flightCabinClassPicker.economyClassRadioButton.id, flightCabinClassPicker.getIdByClass(FlightServiceClassType.CabinCode.COACH))
    }
}
