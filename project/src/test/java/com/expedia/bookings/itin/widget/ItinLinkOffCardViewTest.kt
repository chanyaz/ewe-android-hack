package com.expedia.bookings.itin.widget

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinLinkOffCardViewTest {

    lateinit var context: Context
    lateinit var sut: ItinLinkOffCardView
    lateinit var heading: TextView
    lateinit var subHeading: TextView

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_itin_linkoff_card, null) as ItinLinkOffCardView
        heading = sut.heading
        subHeading = sut.subheading
    }

    @Test
    fun testHideSubheading() {
        assertEquals(View.VISIBLE, subHeading.visibility)
        assertEquals(0, heading.minLines)
        assertEquals(Gravity.TOP or Gravity.START, heading.gravity)
        sut.hideSubheading()
        assertEquals(View.GONE, subHeading.visibility)
        assertEquals(2, heading.minLines)
        assertEquals(Gravity.CENTER_VERTICAL or Gravity.START, heading.gravity)
    }

    @Test
    fun testWrapSubHeading() {
        sut.wrapSubHeading()
        assertEquals(1, subHeading.maxLines)
        assertEquals(TextUtils.TruncateAt.END, subHeading.ellipsize)
    }
}
