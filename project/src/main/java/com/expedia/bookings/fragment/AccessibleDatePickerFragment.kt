package com.expedia.bookings.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageButton
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.vm.BaseSearchViewModel
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate

open class AccessibleDatePickerFragment(val baseSearchViewModel: BaseSearchViewModel) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        baseSearchViewModel.a11yFocusSelectDatesObservable.onNext(Unit)
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        val startDate = baseSearchViewModel.startDate()
        if (baseSearchViewModel.accessibleStartDateSetObservable.value && startDate != null) {
            validateDates(startDate, startDate.plusDays(1))
            baseSearchViewModel.datesUpdated(startDate, startDate.plusDays(1))
            baseSearchViewModel.accessibleStartDateSetObservable.onNext(false)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = DatePickerDialog(activity, this, 0, 0, 0)
        val currentDate = LocalDate.now()
        val maxDate = currentDate.plusDays(baseSearchViewModel.getMaxDateRange())
        val startDate = baseSearchViewModel.startDate()

        if (baseSearchViewModel.accessibleStartDateSetObservable.value && startDate != null) {
            val endDate = startDate.plusDays(1)
            dialog.datePicker.updateDate(endDate.year, endDate.monthOfYear.minus(1), endDate.dayOfMonth)
            if (JodaUtils.isEqual(startDate, maxDate)) {
                setSelectableDateRange(dialog, endDate, endDate)
            } else {
                setSelectableDateRange(dialog, endDate, minimumDateRange(startDate, maxDate))
            }
        } else {
            if (startDate != null) {
                dialog.datePicker.updateDate(startDate.year, startDate.monthOfYear.minus(1), startDate.dayOfMonth)
            } else {
                dialog.datePicker.updateDate(currentDate.year, currentDate.monthOfYear.minus(1), currentDate.dayOfMonth)
            }
            setSelectableDateRange(dialog, currentDate, maxDate)
        }

        setAccessibilityOnFirstElement(dialog)

        return dialog
    }

    fun setAccessibilityOnFirstElement(dialog: DatePickerDialog) {
        if (baseSearchViewModel.isTalkbackActive()) {
            // Here, Plus ImageButton of Month is present in View Hierarchy starting from
            // dialog.datePicker -> LinearLayout -> NumberPicker -> Month widget -> ImageButton(Plus)
            // Also, accessing view directly using findViewById() returns NULL as, the View is not painted on Screen at this point
            var childView = getChildView(dialog.datePicker)
            while (childView != null && childView is ViewGroup) {
                childView = getChildView(childView)
            }

            if (childView != null && childView.visibility == View.VISIBLE && childView is ImageButton) {
                dialog.setOnShowListener { (childView as View).setAccessibilityHoverFocus(200) }
            }
        }
    }

    fun getChildView(viewGroup: ViewGroup): View? {
        if (viewGroup.childCount > 0) {
            return viewGroup.getChildAt(0)
        }
        return null
    }

    fun minimumDateRange(startDate: LocalDate, maxDate: LocalDate): LocalDate {
        val maxDurationDate = startDate.plusDays(baseSearchViewModel.getMaxSearchDurationDays())
        if (maxDurationDate.isBefore(maxDate)) return maxDurationDate else return maxDate
    }

    fun setSelectableDateRange(dialog: DatePickerDialog, minDate: LocalDate, maxDate: LocalDate) {
        dialog.datePicker.minDate = minDate.toDateTimeAtStartOfDay(DateTimeZone.getDefault()).millis
        dialog.datePicker.maxDate = maxDate.toDateTimeAtStartOfDay(DateTimeZone.getDefault()).millis
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (view.isShown) {
            val startDate = baseSearchViewModel.startDate()
            val date = LocalDate(year, monthOfYear.plus(1), dayOfMonth)

            if (baseSearchViewModel.accessibleStartDateSetObservable.value) {
                validateDates(startDate, date)
                baseSearchViewModel.datesUpdated(startDate, date)
                baseSearchViewModel.accessibleStartDateSetObservable.onNext(false)
            } else {
                validateDates(date, null)
                baseSearchViewModel.datesUpdated(date, null)
                if (baseSearchViewModel.getMaxSearchDurationDays() > 0) {
                    baseSearchViewModel.accessibleStartDateSetObservable.onNext(true)
                }
            }
        }
    }

    fun validateDates(start: LocalDate?, end: LocalDate?) {
        val currentDate = LocalDate.now()
        val maxDate = currentDate.plusDays(baseSearchViewModel.getMaxDateRange())

        if (start == null && end != null) {
            throw IllegalArgumentException("Can't set an end date without a start date!  end=" + end)
        } else if (start != null && start.isBefore(currentDate)) {
            throw IllegalArgumentException("Can't set a start date BEFORE current date!  start=" + start + " current=" + currentDate)
        } else if (start != null && end != null && end.isAfter(maxDate)) {
            if (!JodaUtils.isEqual(start, maxDate)) throw IllegalArgumentException("Can't set an end date AFTER max date!  max=" + maxDate + " end=" + end)
        } else if (start != null && end != null && end.isBefore(start)) {
            throw IllegalArgumentException("Can't set an end date BEFORE a start date!  start=" + start + " end=" + end)
        }
    }
}