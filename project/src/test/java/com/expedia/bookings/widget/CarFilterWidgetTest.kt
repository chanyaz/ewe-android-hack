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
        activity.setTheme(R.style.V2_Theme_Cars)
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
        assertFalse(carFilterWidget.manual.isSelected)
        assertFalse(carFilterWidget.auto.isSelected)

        carFilterWidget.manual.performClick()
        assertTrue(carFilterWidget.manual.isSelected)
        assertFalse(carFilterWidget.all.isSelected)
        assertFalse(carFilterWidget.auto.isSelected)

        carFilterWidget.auto.performClick()
        assertTrue(carFilterWidget.auto.isSelected)
        assertFalse(carFilterWidget.all.isSelected)
        assertFalse(carFilterWidget.manual.isSelected)

        assertEquals("All transmissions", carFilterWidget.all.contentDescription)
        assertEquals("Manual transmission", carFilterWidget.manual.contentDescription)
        assertEquals("Automatic transmission", carFilterWidget.auto.contentDescription)
    }
}