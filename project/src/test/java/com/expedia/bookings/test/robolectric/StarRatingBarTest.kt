package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.widget.StarRatingBar
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class StarRatingBarTest {

    var ratingBar: StarRatingBar by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    var spacing: Float by Delegates.notNull()
    var drawable: Drawable by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        ratingBar = android.view.LayoutInflater.from(activity).inflate(R.layout.star_rating_bar_test, null) as StarRatingBar
        spacing = ratingBar.getStarSpacing()
        drawable = ratingBar.getStarDrawable()
    }

    @Test
    fun testMeasurementFullStar() {
        val expectedWidth = 5 * drawable.intrinsicWidth + 4 * spacing
        val expectedHeight = drawable.intrinsicHeight

        ratingBar.setRating(5.0f)

        ratingBar.measure(View.MeasureSpec.makeMeasureSpec(expectedWidth.toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(expectedHeight.toInt(), View.MeasureSpec.EXACTLY))

        assertEquals(expectedWidth.toInt(), ratingBar.measuredWidth)
        assertEquals(expectedHeight.toInt(), ratingBar.measuredHeight)
    }

    @Test
    fun testMeasurementHalfStar() {
        val expectedWidth = 4.5 * drawable.intrinsicWidth + 3 * spacing
        val expectedHeight = drawable.intrinsicHeight

        ratingBar.setRating(4.5f)

        ratingBar.measure(View.MeasureSpec.makeMeasureSpec(expectedWidth.toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(expectedHeight.toInt(), View.MeasureSpec.EXACTLY))

        assertEquals(expectedWidth.toInt(), ratingBar.measuredWidth)
        assertEquals(expectedHeight.toInt(), ratingBar.measuredHeight)
    }
}
