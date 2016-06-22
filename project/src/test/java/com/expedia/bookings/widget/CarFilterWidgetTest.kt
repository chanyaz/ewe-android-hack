package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CarFilterWidgetTest {

    var carFilterWidget by Delegates.notNull<CarFilterWidget>()
    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        carFilterWidget = LayoutInflater.from(activity).inflate(R.layout.test_car_filter_widget, null) as CarFilterWidget
    }

    @Test
    fun testFocusOnFilterToolbar() {
        assertFalse(carFilterWidget.toolbar.isFocused)
        carFilterWidget.setFocusToToolbarForAccessibility()
        assertTrue(carFilterWidget.toolbar.isFocused)
    }

    @Test
    fun checkTransmissionButtonsState() {
        carFilterWidget.bind(CarSearch(), null)

        assertTrue(carFilterWidget.all.isSelected)
        assertEquals("All transmissions", carFilterWidget.all.contentDescription)
        assertFalse(carFilterWidget.manual.isSelected)
        assertEquals("Manual transmission. Unselected", carFilterWidget.manual.contentDescription)
        assertFalse(carFilterWidget.auto.isSelected)
        assertEquals("Automatic transmission. Unselected", carFilterWidget.auto.contentDescription)

        carFilterWidget.manual.performClick()
        assertTrue(carFilterWidget.manual.isSelected)
        assertEquals("Manual transmission", carFilterWidget.manual.contentDescription)
        assertFalse(carFilterWidget.all.isSelected)
        assertEquals("All transmissions. Unselected", carFilterWidget.all.contentDescription)
        assertFalse(carFilterWidget.auto.isSelected)
        assertEquals("Automatic transmission. Unselected", carFilterWidget.auto.contentDescription)

        carFilterWidget.auto.performClick()
        assertTrue(carFilterWidget.auto.isSelected)
        assertEquals("Automatic transmission", carFilterWidget.auto.contentDescription)
        assertFalse(carFilterWidget.all.isSelected)
        assertEquals("All transmissions. Unselected", carFilterWidget.all.contentDescription)
        assertFalse(carFilterWidget.manual.isSelected)
        assertEquals("Manual transmission. Unselected", carFilterWidget.manual.contentDescription)
    }
}