package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CarFilterWidgetTest {

    @Test
    fun testFocusOnFilterToolbar() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        
        val carFilterWidget = LayoutInflater.from(activity).inflate(R.layout.test_car_filter_widget, null) as CarFilterWidget
        assertFalse(carFilterWidget.toolbar.isFocused)
        carFilterWidget.setFocusToToolbarForAccessibility()
        assertTrue(carFilterWidget.toolbar.isFocused)
    }
}