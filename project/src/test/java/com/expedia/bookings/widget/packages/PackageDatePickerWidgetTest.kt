package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.packages.vm.PackageSearchViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.CalendarWidgetV2
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageDatePickerWidgetTest {
    var calendarWidget: TestCalendarWidgetV2 by Delegates.notNull()

    private var activity: FragmentActivity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        calendarWidget = TestCalendarWidgetV2(activity, null)
    }

    @Test
    fun testCustomDatePicker() {
        val mockViewModel = Mockito.spy(PackageSearchViewModel(activity))
        calendarWidget.viewModel = mockViewModel
        calendarWidget.showCalendarDialog()
        assertEquals(true, calendarWidget.showCustomCalendarCalls)
    }

    @Test
    fun testShowCustomCalendarDialog() {
        val mockCalendarDialog = Mockito.mock(CalendarDialogFragment::class.java)
        calendarWidget.calendarDialog = mockCalendarDialog
        calendarWidget.showCustomCalendarDialog()
        Mockito.verify(mockCalendarDialog).show(Mockito.any<FragmentManager>(), Mockito.anyString())
    }

    class TestCalendarWidgetV2(context: Context, attrs: AttributeSet?) : CalendarWidgetV2(context, attrs) {
        var showCustomCalendarCalls = false

        override fun showCustomCalendarDialog() {
            super.showCustomCalendarDialog()
            showCustomCalendarCalls = true
        }
    }
}
