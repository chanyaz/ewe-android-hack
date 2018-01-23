package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.Ui
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ExpirationPickerTest {
    @Test
    fun testButtonContentDescription() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val view = LayoutInflater.from(activity).inflate(R.layout.widget_expiration_picker, null)
        val mMonthUp = Ui.findView<View>(view, R.id.month_up)
        val mMonthDown = Ui.findView<View>(view, R.id.month_down)
        val mYearUp = Ui.findView<View>(view, R.id.year_up)
        val mYearDown = Ui.findView<View>(view, R.id.year_down)

        assertEquals("Increase Month", mMonthUp.contentDescription)
        assertEquals("Decrease Month", mMonthDown.contentDescription)
        assertEquals("Increase Year", mYearUp.contentDescription)
        assertEquals("Decrease Year", mYearDown.contentDescription)
    }
}
