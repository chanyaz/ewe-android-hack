package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFilterVipViewTest {

    private var vipFilterView: HotelFilterVipView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelVipFilterChangedListener {
        var vipChecked = false
        var doTracking = false

        override
        fun onHotelVipFilterChanged(vipChecked: Boolean, doTracking: Boolean) {
            this.vipChecked = vipChecked
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        vipFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_filter_vip_view_test, null) as HotelFilterVipView

        vipFilterView.setOnHotelVipFilterChangedListener(listener)
    }

    @Test
    fun testUpdateVipChecked() {
        vipFilterView.update(true)
        assertTrue(vipFilterView.filterHotelVip.isChecked)
        assertTrue(listener.vipChecked)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testUpdateVipNotChecked() {
        vipFilterView.update(false)
        assertFalse(vipFilterView.filterHotelVip.isChecked)
        assertFalse(listener.vipChecked)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testReset() {
        vipFilterView.update(true)
        vipFilterView.reset()
        assertFalse(vipFilterView.filterHotelVip.isChecked)
        // reset doesn't call listener
        assertTrue(listener.vipChecked)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testClick() {
        vipFilterView.callOnClick()
        assertTrue(vipFilterView.filterHotelVip.isChecked)
        assertTrue(listener.vipChecked)
        assertTrue(listener.doTracking)

        vipFilterView.callOnClick()
        assertFalse(vipFilterView.filterHotelVip.isChecked)
        assertFalse(listener.vipChecked)
        assertTrue(listener.doTracking)
    }
}
