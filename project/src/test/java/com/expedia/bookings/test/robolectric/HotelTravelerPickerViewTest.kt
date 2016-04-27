package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.vm.TravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelTravelerPickerViewTest {
    var vm: TravelerPickerViewModel by Delegates.notNull()
    var hotelTravelerPicker: HotelTravelerPickerView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        hotelTravelerPicker = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_traveler_picker_test, null) as HotelTravelerPickerView
        vm = TravelerPickerViewModel(activity)
        hotelTravelerPicker.viewmodel = vm
    }

    @Test
    fun testAdultIncrement() {
        for (i in 1..6) {
            assertEquals(expectedAdultText(i), getAdultText())
            incrementAdult(1)
        }

        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())
        assertEquals(expectedChildText(0), getChildText())
    }

    @Test
    fun testInfants() {
        incrementChild(2)

        hotelTravelerPicker.child1.setSelection(0)
        hotelTravelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.GONE, hotelTravelerPicker.infantPreferenceSeatingSpinner.visibility)

        vm.showSeatingPreference = true
        hotelTravelerPicker.child1.setSelection(0)
        hotelTravelerPicker.child2.setSelection(1)
        hotelTravelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.VISIBLE, hotelTravelerPicker.infantError.visibility)
        hotelTravelerPicker.infantPreferenceSeatingSpinner.setSelection(1)
        assertEquals(View.GONE, hotelTravelerPicker.infantError.visibility)

        incrementChild(1)
        hotelTravelerPicker.child2.setSelection(0)
        hotelTravelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.VISIBLE, hotelTravelerPicker.infantError.visibility)
        decrementChild(1)
        assertEquals(View.VISIBLE, hotelTravelerPicker.infantError.visibility)
        decrementChild(1)
        assertEquals(View.GONE, hotelTravelerPicker.infantError.visibility)
    }

    @Test
    fun testChildIncrement() {
        assertEquals(expectedAdultText(1), getAdultText())
        for (i in 1..4) {
            incrementChild(1)
            assertEquals(expectedChildText(i), getChildText())
        }

        assertFalse(isChildIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())

        incrementAdult(1)
        assertEquals(expectedAdultText(2), getAdultText())
        assertFalse(isAdultIncrementButtonEnabled())
    }

    @Test
    fun testChildDefault() {
        incrementChild(4)

        hotelTravelerPicker.child1.setSelection(1)
        hotelTravelerPicker.child2.setSelection(2)
        hotelTravelerPicker.child3.setSelection(3)
        hotelTravelerPicker.child4.setSelection(4)

        decrementChild(3)
        incrementAdult(3)

        assertEquals("1", hotelTravelerPicker.child1.selectedItem.toString())
        assertEquals("10", hotelTravelerPicker.child2.selectedItem.toString())
        assertEquals("10", hotelTravelerPicker.child3.selectedItem.toString())
        assertEquals("10", hotelTravelerPicker.child4.selectedItem.toString())
    }

    @Test
    fun testDefaults() {
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())

        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
    }

    @Test
    fun testBounds() {
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())

        incrementAdult(5)

        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())

        decrementAdult(5)

        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())

        incrementChild(4)

        assertFalse(isChildIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertTrue(isChildDecrementButtonEnabled())

        decrementChild(4)
        assertFalse(isChildDecrementButtonEnabled())
    }

    fun incrementAdult(count: Int) {
        for (i in 1..count) {
            hotelTravelerPicker.adultPlus.performClick()
        }
    }

    fun decrementAdult(count: Int) {
        for (i in 1..count) {
            hotelTravelerPicker.adultMinus.performClick()
        }
    }

    fun isAdultIncrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.adultPlus.isEnabled
    }

    fun isAdultDecrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.adultMinus.isEnabled
    }

    fun incrementChild(count: Int) {
        for (i in 1..count) {
            hotelTravelerPicker.childPlus.performClick()
        }
    }

    fun decrementChild(count: Int) {
        for (i in 1..count) {
            hotelTravelerPicker.childMinus.performClick()
        }
    }

    fun isChildIncrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.childPlus.isEnabled
    }

    fun isChildDecrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.childMinus.isEnabled
    }

    fun getAdultText() : CharSequence {
        return hotelTravelerPicker.adultText.text
    }

    fun getChildText() : CharSequence {
        return hotelTravelerPicker.childText.text
    }

    fun expectedAdultText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_adults, count, count)
    }

    fun expectedChildText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_children, count, count)
    }
}
