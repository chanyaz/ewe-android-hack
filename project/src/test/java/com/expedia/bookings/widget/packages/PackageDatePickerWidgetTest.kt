package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import com.expedia.bookings.fragment.AccessibleDatePickerFragment
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.CalendarWidgetV2
import com.expedia.vm.BaseSearchViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import rx.subjects.BehaviorSubject
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
        val mockViewModel = getMockViewModel()
        Mockito.`when`(mockViewModel.isTalkbackActive()).thenReturn(false)
        calendarWidget.viewModel = mockViewModel
        calendarWidget.showCalendarDialog()
        assertEquals(true, calendarWidget.showCustomCalendarCalls)
        assertEquals(false, calendarWidget.showAccessibleCalendarCalls)
    }

    @Test
    fun testAccessibleDatePicker() {
        val mockViewModel = getMockViewModel()
        Mockito.`when`(mockViewModel.isTalkbackActive()).thenReturn(true)
        calendarWidget.viewModel = mockViewModel
        calendarWidget.showCalendarDialog()
        assertEquals(true, calendarWidget.showAccessibleCalendarCalls)
        assertEquals(false, calendarWidget.showCustomCalendarCalls)
    }

    @Test
    fun testShowCustomCalendarDialog() {
        val mockCalendarDialog = Mockito.mock(CalendarDialogFragment::class.java)
        calendarWidget.calendarDialog = mockCalendarDialog
        calendarWidget.showCustomCalendarDialog()
        Mockito.verify(mockCalendarDialog).show(Mockito.any<FragmentManager>(), Mockito.anyString())
    }

    @Test
    fun testShowAccessibleCalendarDialog() {
        val mockCalendarDialog = Mockito.mock(AccessibleDatePickerFragment::class.java)
        val mockViewModel = getMockViewModel()
        calendarWidget.viewModel = mockViewModel
        calendarWidget.accessibleCalendarDialog = mockCalendarDialog
        calendarWidget.showAccessibleCalendarDialog()
        Mockito.verify(mockCalendarDialog).show(Mockito.any<FragmentManager>(), Mockito.anyString())
    }

    @Test
    fun testHideCustomCalendar() {
        val mockCalendarDialog = Mockito.mock(CalendarDialogFragment::class.java)
        val mockAccessibleCalendarDialog = Mockito.mock(AccessibleDatePickerFragment::class.java)
        val mockViewModel = getMockViewModel()
        Mockito.`when`(mockViewModel.isTalkbackActive()).thenReturn(false)
        calendarWidget.viewModel = mockViewModel
        calendarWidget.calendarDialog = mockCalendarDialog
        calendarWidget.accessibleCalendarDialog = mockAccessibleCalendarDialog
        calendarWidget.hideCalendarDialog()
        Mockito.verify(mockCalendarDialog).dismiss()
        Mockito.verify(mockAccessibleCalendarDialog, Mockito.never()).dismiss()
    }

    @Test
    fun testHideAccessibleCalendar() {
        val mockCalendarDialog = Mockito.mock(CalendarDialogFragment::class.java)
        val mockAccessibleCalendarDialog = Mockito.mock(AccessibleDatePickerFragment::class.java)
        val mockViewModel = getMockViewModel()
        Mockito.`when`(mockViewModel.isTalkbackActive()).thenReturn(true)
        calendarWidget.viewModel = mockViewModel
        calendarWidget.calendarDialog = mockCalendarDialog
        calendarWidget.accessibleCalendarDialog = mockAccessibleCalendarDialog
        calendarWidget.hideCalendarDialog()
        Mockito.verify(mockAccessibleCalendarDialog).dismiss()
        Mockito.verify(mockCalendarDialog, Mockito.never()).dismiss()
    }

    fun getMockViewModel(): BaseSearchViewModel {
        val mockViewModel = Mockito.mock(BaseSearchViewModel::class.java)
        mockViewModel.dateTextObservable = BehaviorSubject.create<CharSequence>()
        mockViewModel.accessibleStartDateSetObservable = BehaviorSubject.create<Boolean>(false)
        mockViewModel.a11yFocusSelectDatesObservable = BehaviorSubject.create<Unit>()
        return mockViewModel
    }

    class TestCalendarWidgetV2(context: Context, attrs: AttributeSet?) : CalendarWidgetV2(context, attrs) {
        var showCustomCalendarCalls = false
        var showAccessibleCalendarCalls = false

        override fun showAccessibleCalendarDialog() {
            super.showAccessibleCalendarDialog()
            showAccessibleCalendarCalls = true
        }

        override fun showCustomCalendarDialog() {
            super.showCustomCalendarDialog()
            showCustomCalendarCalls = true
        }
    }
}
