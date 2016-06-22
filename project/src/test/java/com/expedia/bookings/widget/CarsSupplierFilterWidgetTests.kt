package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class CarsSupplierFilterWidgetTests {

    @Test
    fun testSupplierFilterRow() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val carsSupplierFilterWidget = LayoutInflater.from(activity).inflate(R.layout.section_cars_supplier_filter_row, null) as CarsSupplierFilterWidget
        val supplierName = "Supplier Name"
        carsSupplierFilterWidget.bind(supplierName)
        assertEquals(supplierName, carsSupplierFilterWidget.vendorTitle.text)
        assertFalse(carsSupplierFilterWidget.vendorCheckBox.isChecked)
        assertEquals(supplierName, carsSupplierFilterWidget.vendorCheckBox.contentDescription)
    }
}