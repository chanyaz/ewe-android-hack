package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelGuestRatingFilterItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelGuestRatingFilterItemTest {

    private var guestRatingFilterItem: HotelGuestRatingFilterItem by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        guestRatingFilterItem = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_guest_rating_filter_item_test, null) as HotelGuestRatingFilterItem

        guestRatingFilterItem.filterGuestRating.text = "rating"
    }

    @Test
    fun testSelect() {
        guestRatingFilterItem.select()

        assertSelected()
    }

    @Test
    fun testDeselect() {
        guestRatingFilterItem.deselect()

        assertNotSelected()
    }

    @Test
    fun testToggle() {
        guestRatingFilterItem.deselect()
        assertNotSelected()

        guestRatingFilterItem.toggle()
        assertSelected()

        guestRatingFilterItem.toggle()
        assertNotSelected()
    }

    @Test
    fun testClickSubject() {
        val testObserver = TestObserver<Unit>()
        guestRatingFilterItem.clickedSubject.subscribe(testObserver)

        guestRatingFilterItem.filterGuestRating.callOnClick()
        testObserver.assertValuesAndClear(Unit)
    }

    private fun assertSelected() {
        assertTrue(guestRatingFilterItem.guestRatingSelected)
        assertEquals("rating Selected", guestRatingFilterItem.filterGuestRating.contentDescription)
    }

    private fun assertNotSelected() {
        assertFalse(guestRatingFilterItem.guestRatingSelected)
        assertEquals("rating Not selected", guestRatingFilterItem.filterGuestRating.contentDescription)
    }
}
