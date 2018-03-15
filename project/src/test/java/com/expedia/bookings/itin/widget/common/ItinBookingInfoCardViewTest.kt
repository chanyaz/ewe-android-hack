package com.expedia.bookings.itin.widget.common

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinBookingInfoCardView
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinBookingInfoCardViewTest {

    private val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
    private val testView = LayoutInflater.from(activity).inflate(R.layout.test_itin_booking_info_card_view, null) as ItinBookingInfoCardView

    private val mockIcon = R.drawable.ic_itin_credit_card_icon
    private val mockHeading = "Test Heading"
    private val mockSubheading = "Test Subheading"
    private var viewClicked = false
    private fun mockClickListener(): () -> Unit = {
        viewClicked = true
    }

    @Test
    fun testViewWithSubheading() {
        val vm = MockViewModelWithSubheading(mockIcon, mockHeading, mockSubheading, mockClickListener())
        testView.viewModel = vm

        assertEquals(mockIcon, Shadows.shadowOf(testView.icon.drawable).createdFromResId)
        assertEquals(mockHeading, testView.heading.text)
        assertEquals(View.VISIBLE, testView.subheading.visibility)
        assertEquals(mockSubheading, testView.subheading.text)

        testView.performClick()
        assertTrue(viewClicked)
    }

    @Test
    fun testViewWithoutSubheading() {
        val vm = MockViewModelWithSubheading(mockIcon, mockHeading, null, mockClickListener())
        testView.viewModel = vm

        assertEquals(mockIcon, Shadows.shadowOf(testView.icon.drawable).createdFromResId)
        assertEquals(mockHeading, testView.heading.text)
        assertEquals(View.GONE, testView.subheading.visibility)

        testView.performClick()
        assertTrue(viewClicked)
    }

    private class MockViewModelWithSubheading(
        override val iconImage: Int,
        override val headingText: String,
        override val subheadingText: String?,
        override val cardClickListener: () -> Unit
    ) : ItinBookingInfoCardViewModel
}
