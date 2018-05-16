package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelServerFilterView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelAmenityFilterViewTest {

    private var hotelServerFilterView: HotelServerFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelAmenityFilterChangedListener {
        var amenities = HashMap<Amenity, Boolean>()
        var doTracking = false

        override
        fun onHotelAmenityFilterChanged(amenity: Amenity, selected: Boolean, doTracking: Boolean) {
            amenities[amenity] = selected
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelServerFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_filter_view_test, null) as HotelServerFilterView

        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            amenityGridItem.setOnHotelAmenityFilterChangedListener(listener)
        }
        listener.amenities.clear()

        AbacusTestUtils.bucketTests(AbacusUtils.HotelAmenityFilter)
    }

    @Test
    fun testInitText() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            assertEquals(amenityGridItem.textView.text, activity.getString(amenityGridItem.amenity.filterDescriptionId!!))
        }
    }

    @Test
    fun testSelect() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            amenityGridItem.select()

            assertTrue(amenityGridItem.icon.isSelected)
            assertTrue(listener.amenities[amenityGridItem.amenity]!!)
            assertFalse(listener.doTracking)
        }
    }

    @Test
    fun testDeselect() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            amenityGridItem.deselect()

            assertFalse(amenityGridItem.icon.isSelected)
            assertTrue(listener.amenities.isEmpty())
            assertFalse(listener.doTracking)
        }
    }

    @Test
    fun testDisable() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            amenityGridItem.disable()

            assertFalse(amenityGridItem.icon.isEnabled)
            assertFalse(amenityGridItem.textView.isEnabled)
        }
    }

    @Test
    fun testEnable() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem
            amenityGridItem.enable()

            assertTrue(amenityGridItem.icon.isEnabled)
            assertTrue(amenityGridItem.textView.isEnabled)
        }
    }

    @Test
    fun testIsAmenityEnabled() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem

            assertTrue(amenityGridItem.isAmenityEnabled())

            amenityGridItem.disable()

            assertFalse(amenityGridItem.isAmenityEnabled())

            amenityGridItem.enable()

            assertTrue(amenityGridItem.isAmenityEnabled())
        }
    }

    @Test
    fun testClickAmenityIcon() {
        for (i in 0 until hotelServerFilterView.amenitiesGridView.childCount) {
            val amenityGridItem = hotelServerFilterView.amenitiesGridView.getChildAt(i) as HotelAmenityGridItem

            assertFalse(amenityGridItem.isSelected)

            amenityGridItem.icon.callOnClick()

            assertTrue(amenityGridItem.isSelected)
            assertTrue(listener.amenities[amenityGridItem.amenity]!!)
            assertTrue(listener.doTracking)

            amenityGridItem.icon.callOnClick()

            assertFalse(amenityGridItem.isSelected)
            assertFalse(listener.amenities[amenityGridItem.amenity]!!)
            assertTrue(listener.doTracking)
        }
    }
}
