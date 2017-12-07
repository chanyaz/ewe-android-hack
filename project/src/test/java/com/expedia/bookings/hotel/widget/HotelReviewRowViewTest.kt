package com.expedia.bookings.hotel.widget

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class HotelReviewRowViewTest {
    val testView = HotelReviewRowView(RuntimeEnvironment.application)

    @Test
    fun testIsIndicator() {
        assertTrue("FAILURE: RatingBar must be an indicator otherwise" +
                " users can slide the static rating", testView.ratingBar.isIndicator)
    }
}