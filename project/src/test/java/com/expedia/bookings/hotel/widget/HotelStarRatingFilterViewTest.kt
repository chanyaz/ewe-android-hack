package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelStarRatingFilterItem
import com.expedia.bookings.widget.StarRatingValue
import com.expedia.bookings.widget.HotelStarRatingFilterView
import com.expedia.bookings.widget.OnHotelStarRatingFilterChangedListener
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelStarRatingFilterViewTest {

    private var starRatingFilterView: HotelStarRatingFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelStarRatingFilterChangedListener {
        var starRatingValueArray = ArrayList<StarRatingValue>()
        var starRatingSelectedArray = ArrayList<Boolean>()
        var doTrackingArray = ArrayList<Boolean>()

        override
        fun onHotelStarRatingFilterChanged(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean) {
            starRatingValueArray.add(starRatingValue)
            starRatingSelectedArray.add(selected)
            doTrackingArray.add(doTracking)
        }

        fun clear() {
            starRatingValueArray.clear()
            starRatingSelectedArray.clear()
            doTrackingArray.clear()
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        starRatingFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_star_rating_filter_view_test, null) as HotelStarRatingFilterView

        starRatingFilterView.setOnHotelStarRatingFilterChangedListener(listener)
        listener.clear()
    }

    @Test
    fun testUpdateStarRating() {
        for (guestRatingValue in StarRatingValue.values()) {
            val starRatings = getUserFilterChoicesStarRatings(guestRatingValue)

            starRatingFilterView.update(starRatings)
            assertListenerStarRatingValue(guestRatingValue, true, false, 1, 0)
            assertStarRatingValueSelected(guestRatingValue, false)

            listener.clear()
            starRatingFilterView.reset()
        }
    }

    @Test
    fun testUpdateSameStarRating() {
        starRatingFilterView.update(UserFilterChoices.StarRatings(one = true))

        assertStarRatingValueSelected(StarRatingValue.One, false)

        starRatingFilterView.update(UserFilterChoices.StarRatings(one = true))

        assertStarRatingValueSelected(StarRatingValue.One, false)
    }

    @Test
    fun testUpdateSameStarRatingWithFalse() {
        starRatingFilterView.update(UserFilterChoices.StarRatings(two = true))

        assertStarRatingValueSelected(StarRatingValue.Two, false)

        starRatingFilterView.update(UserFilterChoices.StarRatings(two = false))

        assertStarRatingValueDeselected(StarRatingValue.Two, false)
    }

    @Test
    fun testUpdateAllStarRating() {
        starRatingFilterView.update(UserFilterChoices.StarRatings( true, true, true, true, true))

        assertListenerStarRatingValue(StarRatingValue.One, true, false, 5, 0)
        assertListenerStarRatingValue(StarRatingValue.Two, true, false, 5, 1)
        assertListenerStarRatingValue(StarRatingValue.Three, true, false, 5, 2)
        assertListenerStarRatingValue(StarRatingValue.Four, true, false, 5, 3)
        assertListenerStarRatingValue(StarRatingValue.Five, true, false, 5, 4)

        assertStarRatingValues(true, true, true, true, true)
    }

    @Test
    fun testReset() {
        starRatingFilterView.update(UserFilterChoices.StarRatings( true, true, true, true, true))
        assertEquals(5, listener.starRatingValueArray.size)

        starRatingFilterView.reset()

        assertEquals(5, listener.starRatingValueArray.size)

        assertStarRatingValues(false, false, false, false, false)
    }

    @Test
    fun testClickStarRating() {
        for (starRatingValue in StarRatingValue.values()) {
            val filterItem = getHotelStarRatingFilterItem(starRatingValue)

            filterItem.filterStar.callOnClick()
            assertStarRatingValueSelected(starRatingValue, true)

            filterItem.filterStar.callOnClick()
            assertStarRatingValueDeselected(starRatingValue, true)

            listener.clear()
            starRatingFilterView.reset()
        }
    }

    @Test
    fun testClickSameStarRating() {
        starRatingFilterView.filterThree.filterStar.callOnClick()

        assertStarRatingValueSelected(StarRatingValue.Three, true)

        starRatingFilterView.filterThree.filterStar.callOnClick()

        assertStarRatingValueDeselected(StarRatingValue.Three, true)
    }

    @Test
    fun testClickAllStarRating() {
        starRatingFilterView.filterOne.filterStar.callOnClick()
        starRatingFilterView.filterTwo.filterStar.callOnClick()
        starRatingFilterView.filterThree.filterStar.callOnClick()
        starRatingFilterView.filterFour.filterStar.callOnClick()
        starRatingFilterView.filterFive.filterStar.callOnClick()

        assertListenerStarRatingValue(StarRatingValue.One, true, true, 5, 0)
        assertListenerStarRatingValue(StarRatingValue.Two, true, true, 5, 1)
        assertListenerStarRatingValue(StarRatingValue.Three, true, true, 5, 2)
        assertListenerStarRatingValue(StarRatingValue.Four, true, true, 5, 3)
        assertListenerStarRatingValue(StarRatingValue.Five, true, true, 5, 4)

        assertStarRatingValues(true, true, true, true, true)
    }

    private fun getUserFilterChoicesStarRatings(starRatingValue: StarRatingValue): UserFilterChoices.StarRatings {
        return when (starRatingValue) {
            StarRatingValue.One ->
                UserFilterChoices.StarRatings(one = true)
            StarRatingValue.Two ->
                UserFilterChoices.StarRatings(two = true)
            StarRatingValue.Three ->
                UserFilterChoices.StarRatings(three = true)
            StarRatingValue.Four ->
                UserFilterChoices.StarRatings(four = true)
            StarRatingValue.Five ->
                UserFilterChoices.StarRatings(five = true)
        }
    }

    private fun getHotelStarRatingFilterItem(starRatingValue: StarRatingValue): HotelStarRatingFilterItem {
        return when (starRatingValue) {
            StarRatingValue.One ->
                starRatingFilterView.filterOne
            StarRatingValue.Two ->
                starRatingFilterView.filterTwo
            StarRatingValue.Three ->
                starRatingFilterView.filterThree
            StarRatingValue.Four ->
                starRatingFilterView.filterFour
            StarRatingValue.Five ->
                starRatingFilterView.filterFive
        }
    }

    private fun assertStarRatingValueSelected(starRatingValue: StarRatingValue, doTracking: Boolean) {
        assertListenerStarRatingValue(starRatingValue, true, doTracking, 1, 0)
        when (starRatingValue) {
            StarRatingValue.One ->
                assertStarRatingValues(true, false, false, false, false)
            StarRatingValue.Two ->
                assertStarRatingValues(false, true, false, false, false)
            StarRatingValue.Three ->
                assertStarRatingValues(false, false, true, false, false)
            StarRatingValue.Four ->
                assertStarRatingValues(false, false, false, true, false)
            StarRatingValue.Five ->
                assertStarRatingValues(false, false, false, false, true)
        }
    }

    private fun assertStarRatingValueDeselected(guestRatingValue: StarRatingValue, doTracking: Boolean) {
        assertListenerStarRatingValue(guestRatingValue, false, doTracking, 2, 1)
        assertStarRatingValues(false, false, false, false, false)
    }

    private fun assertListenerStarRatingValue(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean,
                                              listenerTriggerCount: Int, listenerIndex: Int) {
        assertEquals(listenerTriggerCount, listener.starRatingValueArray.size)
        assertEquals(starRatingValue, listener.starRatingValueArray[listenerIndex])
        assertEquals(selected, listener.starRatingSelectedArray[listenerIndex])
        assertEquals(doTracking, listener.doTrackingArray[listenerIndex])
    }

    private fun assertStarRatingValues(starRatingOne: Boolean, starRatingTwo: Boolean, starRatingThree: Boolean, starRatingFour: Boolean, starRatingFive: Boolean) {
        assertEquals(starRatingOne, starRatingFilterView.filterOne.starSelected)
        assertEquals(starRatingOne, starRatingFilterView.starRatings.one)

        assertEquals(starRatingTwo, starRatingFilterView.filterTwo.starSelected)
        assertEquals(starRatingTwo, starRatingFilterView.starRatings.two)

        assertEquals(starRatingThree, starRatingFilterView.filterThree.starSelected)
        assertEquals(starRatingThree, starRatingFilterView.starRatings.three)

        assertEquals(starRatingFour, starRatingFilterView.filterFour.starSelected)
        assertEquals(starRatingFour, starRatingFilterView.starRatings.four)

        assertEquals(starRatingFive, starRatingFilterView.filterFive.starSelected)
        assertEquals(starRatingFive, starRatingFilterView.starRatings.five)
    }
}
