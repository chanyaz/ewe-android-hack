package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelStarRatingFilterItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelStarRatingFilterItemTest {

    private var starRatingFilterItem: HotelStarRatingFilterItem by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        starRatingFilterItem = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_star_rating_filter_item_test, null) as HotelStarRatingFilterItem
    }

    @Test
    fun testSelect() {
        starRatingFilterItem.select()

        assertSelected()
    }

    @Test
    fun testDeselect() {
        starRatingFilterItem.deselect()

        assertNotSelected()
    }

    @Test
    fun testToggle() {
        starRatingFilterItem.deselect()
        assertNotSelected()

        starRatingFilterItem.toggle()
        assertSelected()

        starRatingFilterItem.toggle()
        assertNotSelected()
    }

    @Test
    fun testClickSubject() {
        val testObserver = TestObserver<Unit>()
        starRatingFilterItem.clickedSubject.subscribe(testObserver)

        starRatingFilterItem.filterStar.callOnClick()
        testObserver.assertValuesAndClear(Unit)
    }

    private fun assertSelected() {
        assertTrue(starRatingFilterItem.starSelected)
        val ratingString = if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) "circle" else "rating"
        assertEquals("$ratingString Selected", starRatingFilterItem.filterStar.contentDescription)
    }

    private fun assertNotSelected() {
        assertFalse(starRatingFilterItem.starSelected)
        val ratingString = if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) "circle" else "rating"
        assertEquals("$ratingString Not selected", starRatingFilterItem.filterStar.contentDescription)
    }
}
