package com.expedia.bookings.hotel.fragment

import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class ChangeDatesDialogFragmentTest {
    private val context = RuntimeEnvironment.application
    private val testFragment = ChangeDatesDialogFragment()

    private lateinit var fragmentManager: FragmentManager

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager

        testFragment.show(fragmentManager, "dummy_tag")
    }

    @Test
    fun testNoPresetDates() {
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)

        assertNotNull(testFragment.pickerView)
        testFragment.pickerView?.let { picker ->
            assertNull(picker.startDate)
            assertNull(picker.endDate)
        }
    }

    @Test
    fun testWithPresetDates() {
        val expectedStart = LocalDate.now()
        val expectedEnd = LocalDate.now().plusDays(3)
        testFragment.presetDates(HotelStayDates(expectedStart, expectedEnd))

        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)

        assertNotNull(testFragment.pickerView)
        testFragment.pickerView?.let { picker ->
            assertEquals(expectedStart, picker.startDate)
            assertEquals(expectedEnd, picker.endDate)
        }
    }
}
