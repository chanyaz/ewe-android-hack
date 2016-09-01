package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.fragment.AccessibleDatePickerFragment
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BaseSearchViewModel

open class CalendarWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputTextView(context, attrs) {

    init {
        setOnClickListener {
            showCalendarDialog()
        }
    }

    var viewModel: BaseSearchViewModel by notNullAndObservable {
        it.dateTextObservable.subscribeText(this)
        viewModel.accessibleStartDateSetObservable.subscribe { startDateSet ->
            if (startDateSet && viewModel.getMaxSearchDurationDays() > 0) {
                showCalendarDialog()
            }
            else {
                it.a11yFocusSelectDatesObservable.onNext(Unit)
            }
        }
    }

    var calendarDialog: CalendarDialogFragment? = null
    var accessibleCalendarDialog: AccessibleDatePickerFragment? = null

    fun showCalendarDialog() {
        if (viewModel.isTalkbackActive()) {
            accessibleCalendarDialog = AccessibleDatePickerFragment(viewModel)
            showAccessibleCalendarDialog()
        } else {
            calendarDialog = CalendarDialogFragment.createFragment(viewModel)
            showCustomCalendarDialog()
        }
    }

    fun hideCalendarDialog() {
        if (viewModel.isTalkbackActive()) {
            accessibleCalendarDialog?.dismiss()
        } else {
            calendarDialog?.dismiss()
        }
        this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
    }

    open fun showAccessibleCalendarDialog() {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        if (viewModel.accessibleStartDateSetObservable.value) {
            announceForAccessibility(context.resources.getString(R.string.packages_search_datepicker_announce_accessibility_end_date));
        } else {
            announceForAccessibility(context.resources.getString(R.string.packages_search_datepicker_announce_accessibility_start_date));
        }
        accessibleCalendarDialog?.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    open fun showCustomCalendarDialog() {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        calendarDialog?.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }
}
