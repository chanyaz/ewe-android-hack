
package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.FlightTravelerPickerView
import com.expedia.vm.FlightTravelerPickerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightTravelerPickerViewTest {
    var vm: FlightTravelerPickerViewModel by Delegates.notNull()
    var travelerPicker: FlightTravelerPickerView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        travelerPicker = android.view.LayoutInflater.from(activity).inflate(R.layout.flight_traveler_picker_test, null) as FlightTravelerPickerView
        vm = FlightTravelerPickerViewModel(activity)
        travelerPicker.viewmodel = vm
    }

    @Test
    fun testAdultIncrement() {
        for (i in 1..6) {
            assertEquals(expectedAdultText(i), getAdultText())
            incrementAdult(1)
        }

        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isYouthIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())
        assertFalse(isInfantIncrementButtonEnabled())
        assertEquals(expectedYouthText(0), getYouthText())
        assertEquals(expectedChildText(0), getChildText())
        assertEquals(expectedInfantText(0), getInfantText())
    }

    @Test
    fun testYouthIncrement() {
        assertEquals(expectedAdultText(1), getAdultText())
        for (i in 1..4) {
            incrementYouth(1)
            assertEquals(expectedYouthText(i), getYouthText())
        }
        assertFalse(isYouthIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())


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
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())

    }

    @Test
    fun testInfantIncrement() {
        assertEquals(expectedAdultText(1), getAdultText())
        for (i in 1..4) {
            incrementInfant(1)
            assertEquals(expectedInfantText(i), getInfantText())
        }
        assertFalse(isInfantIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        vm.showSeatingPreference=true
        setInfantPreferenceInLap()
        assertTrue(isInfantInLapPreferenceSelected())
        assertEquals(View.VISIBLE,travelerPicker.infantPreferenceSeatingView.visibility)

    }

    @Test
    fun testInfantErrorMessageVisibility() {
        incrementInfant(3)
        vm.showSeatingPreference = true
        setInfantPreferenceInSeat()
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)

        vm.showSeatingPreference = false
        setInfantPreferenceInLap()
        assertEquals(View.GONE, travelerPicker.infantError.visibility)
    }

    @Test
    fun testInfantsErrors() {
        incrementInfant(2)
        vm.showSeatingPreference = true
        setInfantPreferenceInLap()
        assertEquals(View.VISIBLE,travelerPicker.infantPreferenceSeatingView.visibility)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)
        setInfantPreferenceInSeat()
        assertEquals(View.GONE, travelerPicker.infantError.visibility)

        incrementInfant(1)
        assertEquals(View.VISIBLE, travelerPicker.infantError.visibility)
        assertTrue(isInfantInSeatPreferenceSelected())
        decrementInfant(1)
        assertEquals(View.GONE, travelerPicker.infantError.visibility)
        decrementInfant(1)
        assertEquals(View.GONE, travelerPicker.infantError.visibility)
        decrementInfant(1)
        assertEquals(View.GONE,travelerPicker.infantPreferenceSeatingView.visibility)

    }
    @Test
    fun testInLapAndInSeat(){
        incrementInfant(1)
        setInfantPreferenceInLap()
        assertTrue(isInfantInLapPreferenceSelected())
        assertFalse(isInfantInSeatPreferenceSelected())

        setInfantPreferenceInSeat()
        assertTrue(isInfantInSeatPreferenceSelected())
        assertFalse(isInfantInLapPreferenceSelected())

        incrementInfant(1)
        assertTrue(isInfantInSeatPreferenceSelected())
        assertFalse(isInfantInLapPreferenceSelected())

    }

    @Test
    fun testChildrenList() {

        incrementYouth(2)
        assertEquals("16, 16", vm.travelerParamsObservable.value.childrenAges.joinToString())
        incrementChild(1)
        assertEquals("16, 16, 10", vm.travelerParamsObservable.value.childrenAges.joinToString())
        incrementInfant(1)
        assertEquals("16, 16, 10, 1", vm.travelerParamsObservable.value.childrenAges.joinToString())

        decrementYouth(1)
        assertEquals("16, 10, 1", vm.travelerParamsObservable.value.childrenAges.joinToString())
        decrementYouth(1)
        assertEquals("10, 1", vm.travelerParamsObservable.value.childrenAges.joinToString())
        decrementChild(1)
        assertEquals("1", vm.travelerParamsObservable.value.childrenAges.joinToString())
        decrementInfant(1)
        assertEquals("", vm.travelerParamsObservable.value.childrenAges.joinToString())

    }

    @Test
    fun testDefaults() {
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())
        assertFalse(isYouthDecrementButtonEnabled())
        assertFalse(isInfantDecrementButtonEnabled())

        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())
    }

    @Test
    fun testBounds() {
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())

        incrementAdult(5)

        assertFalse(isAdultIncrementButtonEnabled())
        assertFalse(isChildIncrementButtonEnabled())
        assertFalse(isYouthIncrementButtonEnabled())
        assertFalse(isInfantIncrementButtonEnabled())

        decrementAdult(5)

        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())
        assertFalse(isYouthDecrementButtonEnabled())
        assertFalse(isInfantDecrementButtonEnabled())

        incrementChild(4)

        assertFalse(isChildIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isYouthDecrementButtonEnabled())
        assertFalse(isInfantDecrementButtonEnabled())
        assertTrue(isChildDecrementButtonEnabled())

        decrementChild(4)
        assertFalse(isChildDecrementButtonEnabled())

        incrementYouth(4)

        assertFalse(isYouthIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isInfantIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())
        assertFalse(isInfantDecrementButtonEnabled())
        assertTrue(isYouthDecrementButtonEnabled())

        decrementYouth(4)
        assertFalse(isYouthDecrementButtonEnabled())

        incrementInfant(4)

        assertFalse(isInfantIncrementButtonEnabled())
        assertTrue(isAdultIncrementButtonEnabled())
        assertTrue(isChildIncrementButtonEnabled())
        assertTrue(isYouthIncrementButtonEnabled())
        assertFalse(isAdultDecrementButtonEnabled())
        assertFalse(isChildDecrementButtonEnabled())
        assertFalse(isYouthDecrementButtonEnabled())
        assertTrue(isInfantDecrementButtonEnabled())

        decrementInfant(4)
        assertFalse(isInfantDecrementButtonEnabled())
    }

    fun incrementAdult(count: Int) {
        for (i in 1..count) {
            travelerPicker.adultCountSelector.travelerPlus.performClick()
        }
    }

    fun decrementAdult(count: Int) {
        for (i in 1..count) {
            travelerPicker.adultCountSelector.travelerMinus.performClick()
        }
    }

    fun isAdultIncrementButtonEnabled() : Boolean {
        return travelerPicker.adultCountSelector.travelerPlus.isEnabled
    }

    fun isAdultDecrementButtonEnabled() : Boolean {
        return travelerPicker.adultCountSelector.travelerMinus.isEnabled
    }

    fun incrementChild(count: Int) {
        for (i in 1..count) {
            travelerPicker.childCountSelector.travelerPlus.performClick()
        }
    }

    fun decrementChild(count: Int) {
        for (i in 1..count) {
            travelerPicker.childCountSelector.travelerMinus.performClick()
        }
    }

    fun incrementYouth(count: Int) {
        for (i in 1..count) {
            travelerPicker.youthCountSelector.travelerPlus.performClick()
        }
    }

    fun decrementYouth(count: Int) {
        for (i in 1..count) {
            travelerPicker.youthCountSelector.travelerMinus.performClick()
        }
    }
    fun incrementInfant(count: Int) {
        for (i in 1..count) {
            travelerPicker.infantCountSelector.travelerPlus.performClick()
        }
    }

    fun decrementInfant(count: Int) {
        for (i in 1..count) {
            travelerPicker.infantCountSelector.travelerMinus.performClick()
        }
    }

    fun isChildIncrementButtonEnabled() : Boolean {
        return travelerPicker.childCountSelector.travelerPlus.isEnabled
    }

    fun isChildDecrementButtonEnabled() : Boolean {
        return travelerPicker.childCountSelector.travelerMinus.isEnabled
    }

    fun isYouthDecrementButtonEnabled() : Boolean {
        return travelerPicker.youthCountSelector.travelerMinus.isEnabled
    }

    fun isYouthIncrementButtonEnabled() : Boolean {
        return travelerPicker.youthCountSelector.travelerPlus.isEnabled
    }

    fun isInfantDecrementButtonEnabled() : Boolean {
        return travelerPicker.infantCountSelector.travelerMinus.isEnabled
    }

    fun isInfantIncrementButtonEnabled() : Boolean {
        return travelerPicker.infantCountSelector.travelerPlus.isEnabled
    }

    fun setInfantPreferenceInLap() : Unit {
        travelerPicker.infantInLap.setChecked(true)
    }

    fun setInfantPreferenceInSeat() : Unit {
        travelerPicker.infantInSeat.setChecked(true)
    }

    fun getAdultText() : CharSequence {
        return travelerPicker.adultCountSelector.travelerText.text
    }

    fun getYouthText() : CharSequence {
        return travelerPicker.youthCountSelector.travelerText.text
    }

    fun getChildText() : CharSequence {
        return travelerPicker.childCountSelector.travelerText.text
    }

    fun getInfantText() : CharSequence {
        return travelerPicker.infantCountSelector.travelerText.text
    }
    fun expectedAdultText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_adults, count, count)
    }

    fun expectedYouthText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_youth, count, count)
    }
    fun expectedInfantText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_infant, count, count)
    }

    fun expectedChildText(count: Int) : String {
        return activity.resources.getQuantityString(R.plurals.number_of_children, count, count)
    }
    fun isInfantInLapPreferenceSelected() : Boolean {
        return (travelerPicker.infantInLap.isChecked)
    }
    fun isInfantInSeatPreferenceSelected() : Boolean {
        return (travelerPicker.infantInSeat.isChecked)
    }

}
