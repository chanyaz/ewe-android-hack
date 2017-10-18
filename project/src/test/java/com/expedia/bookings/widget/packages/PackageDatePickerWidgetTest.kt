package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.hotel.util.HotelCalendarRules
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


    fun getMockViewModel(): BaseSearchViewModel {
        val mockViewModel = Mockito.mock(BaseSearchViewModel::class.java)
        Mockito.`when`(mockViewModel.getCalendarRules()).thenReturn(HotelCalendarRules(activity))
        mockViewModel.dateTextObservable = BehaviorSubject.create<CharSequence>()
        return mockViewModel
    }

    class TestCalendarWidgetV2(context: Context, attrs: AttributeSet?) : CalendarWidgetV2(context, attrs) {
        var showCustomCalendarCalls = false

        override fun showCustomCalendarDialog() {
            super.showCustomCalendarDialog()
            showCustomCalendarCalls = true
        }
    }
}
