package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import android.widget.CheckedTextView
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TravelerPickerView
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
class TravelerPickerViewTest {
    var vm: TravelerPickerViewModel by Delegates.notNull()
    var travelerPicker: TravelerPickerView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        travelerPicker = android.view.LayoutInflater.from(activity).inflate(R.layout.traveler_picker_test, null) as TravelerPickerView
        vm = TravelerPickerViewModel(activity)
        travelerPicker.viewmodel = vm
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
    fun testInfantErrorMessageVisibility() {
        for (i in 1..3) {
            travelerPicker.childPlus.performClick()
        }
        travelerPicker.child1.setSelection(0)
        travelerPicker.child2.setSelection(0)
        travelerPicker.child3.setSelection(0)
        //error message should be visible if we show seating preference for infant(Eg: Flights)
        vm.showSeatingPreference = true
        travelerPicker.infantPreferenceSeatingSpinner.setSelection(1)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)

        //error message should be not be visible if we do not show seating preference for infant(Eg: Flights)
        vm.showSeatingPreference = false
        travelerPicker.child3.setSelection(0)
        assertEquals(View.GONE, travelerPicker.infantError.visibility)
    }

    @Test
    fun testInfants() {
        incrementChild(2)

        travelerPicker.child1.setSelection(0)
        travelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.GONE, travelerPicker.infantPreferenceSeatingSpinner.visibility)

        vm.showSeatingPreference = true
        travelerPicker.child1.setSelection(0)
        travelerPicker.child2.setSelection(1)
        travelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)
        travelerPicker.infantPreferenceSeatingSpinner.setSelection(1)
        assertEquals(View.GONE, travelerPicker.infantError.visibility)

        incrementChild(1)
        travelerPicker.child2.setSelection(0)
        travelerPicker.infantPreferenceSeatingSpinner.setSelection(0)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)
        decrementChild(1)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)
        decrementChild(1)
        assertEquals(View.GONE, travelerPicker.infantError.visibility)
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

        travelerPicker.child1.setSelection(1)
        travelerPicker.child2.setSelection(2)
        travelerPicker.child3.setSelection(3)
        travelerPicker.child4.setSelection(4)

        decrementChild(3)
        incrementAdult(3)

        assertEquals("1", travelerPicker.child1.selectedItem.toString())
        assertEquals("10", travelerPicker.child2.selectedItem.toString())
        assertEquals("10", travelerPicker.child3.selectedItem.toString())
        assertEquals("10", travelerPicker.child4.selectedItem.toString())
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

    @Test
    fun testChildAgeListText() {
        incrementChild(1)
        assertEquals(18, travelerPicker.child1.adapter.count)

        for (i in 17..0) {
            val expectedText = getExpectedChildAgeText(i)
            val actualText = (travelerPicker.child1.adapter.getDropDownView(i, null, travelerPicker.child1) as CheckedTextView).text
            assertEquals(expectedText, actualText)
        }
    }

    fun getExpectedChildAgeText(number: Int): String {
        if (number == 0)
            return "Less than 1 year old"
        else if (number == 1)
            return "1 year old"
        else return (number.toString() + " years old")
    }

    fun incrementAdult(count: Int) {
        for (i in 1..count) {
            travelerPicker.adultPlus.performClick()
        }
    }

    fun decrementAdult(count: Int) {
        for (i in 1..count) {
            travelerPicker.adultMinus.performClick()
        }
    }

    fun isAdultIncrementButtonEnabled(): Boolean {
        return travelerPicker.adultPlus.isEnabled
    }

    fun isAdultDecrementButtonEnabled(): Boolean {
        return travelerPicker.adultMinus.isEnabled
    }

    fun incrementChild(count: Int) {
        for (i in 1..count) {
            travelerPicker.childPlus.performClick()
        }
    }

    fun decrementChild(count: Int) {
        for (i in 1..count) {
            travelerPicker.childMinus.performClick()
        }
    }

    fun isChildIncrementButtonEnabled(): Boolean {
        return travelerPicker.childPlus.isEnabled
    }

    fun isChildDecrementButtonEnabled(): Boolean {
        return travelerPicker.childMinus.isEnabled
    }

    fun getAdultText(): CharSequence {
        return travelerPicker.adultText.text
    }

    fun getChildText(): CharSequence {
        return travelerPicker.childText.text
    }

    fun expectedAdultText(count: Int): String {
        return activity.resources.getQuantityString(R.plurals.number_of_adults, count, count)
    }

    fun expectedChildText(count: Int): String {
        return activity.resources.getQuantityString(R.plurals.number_of_children, count, count)
    }
}
