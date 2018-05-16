package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.GuestRatingValue
import com.expedia.bookings.widget.HotelGuestRatingFilterItem
import com.expedia.bookings.widget.HotelGuestRatingFilterView
import com.expedia.bookings.widget.OnHotelGuestRatingFilterChangedListener
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelGuestRatingFilterViewTest {

    private var guestRatingFilterView: HotelGuestRatingFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelGuestRatingFilterChangedListener {
        var guestRatingValueArray = ArrayList<GuestRatingValue>()
        var guestRatingSelectedArray = ArrayList<Boolean>()
        var doTrackingArray = ArrayList<Boolean>()

        override
        fun onHotelGuestRatingFilterChanged(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean) {
            guestRatingValueArray.add(guestRatingValue)
            guestRatingSelectedArray.add(selected)
            doTrackingArray.add(doTracking)
        }

        fun clear() {
            guestRatingValueArray.clear()
            guestRatingSelectedArray.clear()
            doTrackingArray.clear()
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        guestRatingFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_guest_rating_filter_view_test, null) as HotelGuestRatingFilterView

        guestRatingFilterView.setOnHotelGuestRatingFilterChangedListener(listener)
        listener.clear()
    }

    @Test
    fun testUpdateGuestRating() {
        for (guestRatingValue in GuestRatingValue.values()) {
            val guestRatings = getUserFilterChoicesGuestRatings(guestRatingValue)

            guestRatingFilterView.update(guestRatings)
            assertListenerGuestRatingValue(guestRatingValue, true, false, 1, 0)
            assertGuestRatingValueSelected(guestRatingValue, false)

            listener.clear()
            guestRatingFilterView.reset()
        }
    }

    @Test
    fun testUpdateGuestRatingToggleOtherRating() {
        for (guestRatingValue in GuestRatingValue.values()) {
            val guestRatings = getUserFilterChoicesGuestRatings(guestRatingValue)
            for (otherGuestRatingValue in GuestRatingValue.values()) {
                if (otherGuestRatingValue != guestRatingValue) {
                    val otherGuestRatings = getUserFilterChoicesGuestRatings(otherGuestRatingValue)
                    guestRatingFilterView.update(otherGuestRatings)

                    assertGuestRatingValueSelected(otherGuestRatingValue, false)

                    guestRatingFilterView.update(guestRatings)

                    assertGuestRatingValueDeselectedThenSelected(otherGuestRatingValue, guestRatingValue, false)

                    listener.clear()
                    guestRatingFilterView.reset()
                }
            }
        }
    }

    @Test
    fun testUpdateSameGuestRating() {
        guestRatingFilterView.update(UserFilterChoices.GuestRatings(four = true))

        assertGuestRatingValueSelected(GuestRatingValue.Four, false)

        guestRatingFilterView.update(UserFilterChoices.GuestRatings(four = true))

        assertGuestRatingValueDeselected(GuestRatingValue.Four, false)
    }

    @Test
    fun testUpdateSameGuestRatingWithFalse() {
        guestRatingFilterView.update(UserFilterChoices.GuestRatings(four = true))

        assertGuestRatingValueSelected(GuestRatingValue.Four, false)

        guestRatingFilterView.update(UserFilterChoices.GuestRatings(four = false))

        assertGuestRatingValueSelected(GuestRatingValue.Four, false)
    }

    @Test
    fun testUpdateAllGuestRating() {
        guestRatingFilterView.update(UserFilterChoices.GuestRatings( true, true, true))

        assertListenerGuestRatingValue(GuestRatingValue.Three, true, false, 5, 0)
        assertListenerGuestRatingValue(GuestRatingValue.Three, false, false, 5, 1)
        assertListenerGuestRatingValue(GuestRatingValue.Four, true, false, 5, 2)
        assertListenerGuestRatingValue(GuestRatingValue.Four, false, false, 5, 3)
        assertListenerGuestRatingValue(GuestRatingValue.Five, true, false, 5, 4)

        assertGuestRatingValues(false, false, true)
    }

    @Test
    fun testReset() {
        guestRatingFilterView.update(UserFilterChoices.GuestRatings( true, true, true))
        assertEquals(5, listener.guestRatingValueArray.size)

        guestRatingFilterView.reset()

        assertEquals(5, listener.guestRatingValueArray.size)

        assertGuestRatingValues(false, false, false)
    }

    @Test
    fun testGuestRatingClick() {
        for (guestRatingValue in GuestRatingValue.values()) {
            val filterItem = getHotelGuestRatingFilterItem(guestRatingValue)

            filterItem.filterGuestRating.callOnClick()
            assertGuestRatingValueSelected(guestRatingValue, true)

            filterItem.filterGuestRating.callOnClick()
            assertGuestRatingValueDeselected(guestRatingValue, true)

            listener.clear()
            guestRatingFilterView.reset()
        }
    }

    @Test
    fun testGuestRatingClickToggleOtherRating() {
        for (guestRatingValue in GuestRatingValue.values()) {
            val filterItem = getHotelGuestRatingFilterItem(guestRatingValue)

            for (otherGuestRatingValue in GuestRatingValue.values()) {
                if (otherGuestRatingValue != guestRatingValue) {
                    val otherFilterItem = getHotelGuestRatingFilterItem(otherGuestRatingValue)

                    otherFilterItem.filterGuestRating.callOnClick()

                    assertGuestRatingValueSelected(otherGuestRatingValue, true)

                    filterItem.filterGuestRating.callOnClick()

                    assertGuestRatingValueDeselectedThenSelected(otherGuestRatingValue, guestRatingValue, true)

                    listener.clear()
                    guestRatingFilterView.reset()
                }
            }
        }
    }

    @Test
    fun testClickSameGuestRating() {
        guestRatingFilterView.filterFive.filterGuestRating.callOnClick()

        assertGuestRatingValueSelected(GuestRatingValue.Five, true)

        guestRatingFilterView.filterFive.filterGuestRating.callOnClick()

        assertGuestRatingValueDeselected(GuestRatingValue.Five, true)
    }

    @Test
    fun testClickAllGuestRating() {
        guestRatingFilterView.filterThree.filterGuestRating.callOnClick()
        guestRatingFilterView.filterFour.filterGuestRating.callOnClick()
        guestRatingFilterView.filterFive.filterGuestRating.callOnClick()

        assertListenerGuestRatingValue(GuestRatingValue.Three, true, true, 5, 0)
        assertListenerGuestRatingValue(GuestRatingValue.Three, false, true, 5, 1)
        assertListenerGuestRatingValue(GuestRatingValue.Four, true, true, 5, 2)
        assertListenerGuestRatingValue(GuestRatingValue.Four, false, true, 5, 3)
        assertListenerGuestRatingValue(GuestRatingValue.Five, true, true, 5, 4)

        assertGuestRatingValues(false, false, true)
    }

    @Test
    fun testFilterButtonText() {
        assertEquals("3.0 +", guestRatingFilterView.filterThree.filterGuestRating.text.toString())
        assertEquals("4.0 +", guestRatingFilterView.filterFour.filterGuestRating.text.toString())
        assertEquals("5.0", guestRatingFilterView.filterFive.filterGuestRating.text.toString())
    }

    private fun getUserFilterChoicesGuestRatings(guestRatingValue: GuestRatingValue): UserFilterChoices.GuestRatings {
        return when (guestRatingValue) {
            GuestRatingValue.Three ->
                UserFilterChoices.GuestRatings(three = true)
            GuestRatingValue.Four ->
                UserFilterChoices.GuestRatings(four = true)
            GuestRatingValue.Five ->
                UserFilterChoices.GuestRatings(five = true)
        }
    }

    private fun getHotelGuestRatingFilterItem(guestRatingValue: GuestRatingValue): HotelGuestRatingFilterItem {
        return when (guestRatingValue) {
            GuestRatingValue.Three ->
                guestRatingFilterView.filterThree
            GuestRatingValue.Four ->
                guestRatingFilterView.filterFour
            GuestRatingValue.Five ->
                guestRatingFilterView.filterFive
        }
    }

    private fun assertGuestRatingValueSelected(guestRatingValue: GuestRatingValue, doTracking: Boolean) {
        assertListenerGuestRatingValue(guestRatingValue, true, doTracking, 1, 0)
        when (guestRatingValue) {
            GuestRatingValue.Three ->
                assertGuestRatingValues(true, false, false)
            GuestRatingValue.Four ->
                assertGuestRatingValues(false, true, false)
            GuestRatingValue.Five ->
                assertGuestRatingValues(false, false, true)
        }
    }

    private fun assertGuestRatingValueDeselected(guestRatingValue: GuestRatingValue, doTracking: Boolean) {
        assertListenerGuestRatingValue(guestRatingValue, false, doTracking, 2, 1)
        assertGuestRatingValues(false, false, false)
    }

    private fun assertGuestRatingValueDeselectedThenSelected(deselectedGuestRatingValue: GuestRatingValue, selectedGuestRatingValue: GuestRatingValue, doTracking: Boolean) {
        assertListenerGuestRatingValue(deselectedGuestRatingValue, false, doTracking, 3, 1)
        assertListenerGuestRatingValue(selectedGuestRatingValue, true, doTracking, 3, 2)
        when (selectedGuestRatingValue) {
            GuestRatingValue.Three ->
                assertGuestRatingValues(true, false, false)
            GuestRatingValue.Four ->
                assertGuestRatingValues(false, true, false)
            GuestRatingValue.Five ->
                assertGuestRatingValues(false, false, true)
        }
    }

    private fun assertListenerGuestRatingValue(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean,
                                               listenerTriggerCount: Int, listenerIndex: Int) {
        assertEquals(listenerTriggerCount, listener.guestRatingValueArray.size)
        assertEquals(guestRatingValue, listener.guestRatingValueArray[listenerIndex])
        assertEquals(selected, listener.guestRatingSelectedArray[listenerIndex])
        assertEquals(doTracking, listener.doTrackingArray[listenerIndex])
    }

    private fun assertGuestRatingValues(guestRatingThree: Boolean, guestRatingFour: Boolean, guestRatingFive: Boolean) {
        assertEquals(guestRatingThree, guestRatingFilterView.filterThree.guestRatingSelected)
        assertEquals(guestRatingThree, guestRatingFilterView.guestRatings.three)

        assertEquals(guestRatingFour, guestRatingFilterView.filterFour.guestRatingSelected)
        assertEquals(guestRatingFour, guestRatingFilterView.guestRatings.four)

        assertEquals(guestRatingFive, guestRatingFilterView.filterFive.guestRatingSelected)
        assertEquals(guestRatingFive, guestRatingFilterView.guestRatings.five)
    }
}
