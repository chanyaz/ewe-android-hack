package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel

open class CalendarWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputTextView(context, attrs) {

    init {
        setOnClickListener {
            showCalendarDialog()
        }
    }

    var viewModel: BaseSearchViewModel by notNullAndObservable {
        it.dateTextObservable.subscribeText(this)
    }

    var calendarDialog: CalendarDialogFragment? = null

    fun showCalendarDialog() {
        if (calendarDialog != null && calendarDialog!!.isShowInitiated) {
            return
        }
        calendarDialog = CalendarDialogFragment.createFragment(viewModel, viewModel.getCalendarRules())
        showCustomCalendarDialog()
    }

    fun hideCalendarDialog() {
        calendarDialog?.dismiss()
        this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
    }

    open fun showCustomCalendarDialog() {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        calendarDialog?.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }
}
