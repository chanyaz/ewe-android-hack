package com.expedia.bookings.widget.packages

import android.view.View
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.PackageBundleHotelWidget
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelWidgetTest{
    val context = RuntimeEnvironment.application

    lateinit var testHotelWidget: PackageBundleHotelWidget

    @Before
    fun setup() {
        testHotelWidget = PackageBundleHotelWidget(context, null)
    }

    @Test
    fun testCancel() {
        testHotelWidget.cancel()

        assertEquals(View.GONE, testHotelWidget.hotelLoadingBar.visibility)
        assertEquals(View.VISIBLE, testHotelWidget.hotelsDatesGuestInfoText.visibility)
    }

    @Test
    fun testToggleHotelWidget() {
        testHotelWidget.toggleHotelWidget(1f, true)
        assertTrue(testHotelWidget.rowContainer.isEnabled)
        assertTrue(testHotelWidget.hotelDetailsIcon.isEnabled)
        assertTrue(testHotelWidget.isEnabled)
        assertEquals(1f, testHotelWidget.hotelsText.alpha)
        assertEquals(1f, testHotelWidget.hotelsDatesGuestInfoText.alpha)
        assertEquals(1f, testHotelWidget.hotelLuggageIcon.alpha)
        assertEquals(1f, testHotelWidget.hotelDetailsIcon.alpha)

        testHotelWidget.toggleHotelWidget(1f, false)
        assertTrue (!testHotelWidget.rowContainer.isEnabled)
        assertTrue(!testHotelWidget.hotelDetailsIcon.isEnabled)
        assertTrue(!testHotelWidget.isEnabled)

    }
}
