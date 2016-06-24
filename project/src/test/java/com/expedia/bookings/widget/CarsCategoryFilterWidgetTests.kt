package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.CarCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class CarsCategoryFilterWidgetTests {

    @Test
    fun testCategoryFilterRow() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val carsCategoryFilterWidget = LayoutInflater.from(activity).inflate(R.layout.section_cars_category_filter_row, null) as CarsCategoryFilterWidget
        carsCategoryFilterWidget.bind(CarCategory.COMPACT)
        assertEquals("Compact", carsCategoryFilterWidget.categoryTitle.text)
        assertFalse(carsCategoryFilterWidget.categoryCheckBox.isChecked)
        assertEquals("Compact", carsCategoryFilterWidget.categoryCheckBox.contentDescription)
    }
}