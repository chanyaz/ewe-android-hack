package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelNameFilterViewTest {

    private var nameFilterView: HotelNameFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelNameFilterChangedListener {
        var hotelName: CharSequence = ""
        var doTracking = false

        override
        fun onHotelNameFilterChanged(hotelName: CharSequence, doTracking: Boolean) {
            this.hotelName = hotelName
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        nameFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_name_filter_view_test, null) as HotelNameFilterView

        nameFilterView.setOnHotelNameChangedListener(listener)
    }

    @Test
    fun testUpdateName() {
        val name = "your"
        nameFilterView.updateName(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
    }

    @Test
    fun testReset() {
        var name = "name"
        nameFilterView.updateName(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)

        nameFilterView.reset()
        name = ""
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
    }

    @Test
    fun testResetBlank() {
        val name = ""
        val listenerName = "immutable"

        nameFilterView.updateName(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())

        listener.hotelName = listenerName
        listener.doTracking = true
        assertEquals(listenerName, listener.hotelName.toString())
        assertTrue(listener.doTracking)

        nameFilterView.reset()
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(listenerName, listener.hotelName.toString())
        assertTrue(listener.doTracking)
    }

    @Test
    fun testResetFocus() {
        nameFilterView.filterHotelName.requestFocus()
        assertTrue(nameFilterView.filterHotelName.isFocused)
        nameFilterView.resetFocus()
        assertFalse(nameFilterView.filterHotelName.isFocused)
    }

    @Test
    fun testOnTextChangedWithFocus() {
        nameFilterView.filterHotelName.requestFocus()

        var name = "a"
        nameFilterView.filterHotelName.setText(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertTrue(listener.doTracking)
        assertEquals(View.VISIBLE, nameFilterView.clearNameButton.visibility)

        name = ""
        nameFilterView.filterHotelName.setText(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertTrue(listener.doTracking)
        assertEquals(View.GONE, nameFilterView.clearNameButton.visibility)
    }

    @Test
    fun testOnTextChangedWithoutFocus() {
        nameFilterView.resetFocus()

        var name = "a"
        nameFilterView.filterHotelName.setText(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
        assertEquals(View.VISIBLE, nameFilterView.clearNameButton.visibility)

        name = ""
        nameFilterView.filterHotelName.setText(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
        assertEquals(View.GONE, nameFilterView.clearNameButton.visibility)
    }

    @Test
    fun testClearNameButton() {
        var name = "b"
        nameFilterView.filterHotelName.setText(name)
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
        assertEquals(View.VISIBLE, nameFilterView.clearNameButton.visibility)

        name = ""
        nameFilterView.clearNameButton.callOnClick()
        assertEquals(name, nameFilterView.filterHotelName.text.toString())
        assertEquals(name, listener.hotelName.toString())
        assertFalse(listener.doTracking)
        assertEquals(View.GONE, nameFilterView.clearNameButton.visibility)
    }
}
