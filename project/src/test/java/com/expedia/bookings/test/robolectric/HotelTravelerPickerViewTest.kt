package com.expedia.bookings.test

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.vm.HotelTravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
public class HotelTravelerPickerViewTest {
    public var vm: HotelTravelerPickerViewModel by Delegates.notNull()
    public var hotelTravelerPicker: HotelTravelerPickerView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        hotelTravelerPicker = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_traveler_picker_test, null) as HotelTravelerPickerView
        vm = HotelTravelerPickerViewModel(activity, false)
        hotelTravelerPicker.viewmodel = vm
    }

    @Test
    fun testAdultIncrement() {
        for (i in 1..6) {
            assertEquals(expectedAdultText(i), getAdultText())
            incrementAdult()
        }

        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())
        assertEquals(expectedChildText(0), getChildText())
    }

    @Test
    fun testChildIncrement() {
        assertEquals(expectedAdultText(1), getAdultText())
        for (i in 1..4) {
            incrementChild()
            assertEquals(expectedChildText(i), getChildText())
        }

        assertFalse(isChildIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())

        incrementAdult()
        assertEquals(expectedAdultText(2), getAdultText())
        assertFalse(isAdultIncrementButtonEnabled())
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
        for (i in 1..5) {
            incrementAdult()
        }
        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())

        for (i in 1..5) {
            decrementAdult()
        }
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())

        for (i in 1..4) {
            incrementChild()
        }
        assertFalse(isChildIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertTrue(isChildDecrementButtonEnabled())

        for (i in 1..4) {
            decrementChild()
        }
        assertFalse(isChildDecrementButtonEnabled())
    }

    fun incrementAdult() {
        hotelTravelerPicker.adultPlus.performClick()
    }

    fun decrementAdult() {
        hotelTravelerPicker.adultMinus.performClick()
    }

    fun isAdultIncrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.adultPlus.isEnabled()
    }

    fun isAdultDecrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.adultMinus.isEnabled()
    }

    fun incrementChild() {
        hotelTravelerPicker.childPlus.performClick()
    }

    fun decrementChild() {
        hotelTravelerPicker.childMinus.performClick()
    }

    fun isChildIncrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.childPlus.isEnabled()
    }

    fun isChildDecrementButtonEnabled() : Boolean {
        return hotelTravelerPicker.childMinus.isEnabled()
    }

    fun getAdultText() : CharSequence {
        return hotelTravelerPicker.adultText.getText()
    }

    fun getChildText() : CharSequence {
        return hotelTravelerPicker.childText.getText()
    }

    fun expectedAdultText(count: Int) : String {
        return activity.getResources().getQuantityString(R.plurals.number_of_adults, count, count)
    }

    fun expectedChildText(count: Int) : String {
        return activity.getResources().getQuantityString(R.plurals.number_of_children, count, count)
    }
}
