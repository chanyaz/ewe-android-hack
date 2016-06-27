package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.utils.Constants
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BaseSearchViewModel

class CalendarWidgetV2(context: Context, attrs: AttributeSet) : SearchInputCardView(context, attrs) {

    init {
        setOnClickListener {
            showCalendarDialog()
        }
    }

    var viewModel: BaseSearchViewModel by notNullAndObservable {
        it.dateTextObservable.subscribeText(this.text)
    }

    var calendarDialog: CalendarDialogFragment? = null

    fun showCalendarDialog() {
        calendarDialog = CalendarDialogFragment.createFragment(viewModel)

        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        calendarDialog?.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    fun hideCalendarDialog() {
        calendarDialog?.dismiss()
        this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
    }
}