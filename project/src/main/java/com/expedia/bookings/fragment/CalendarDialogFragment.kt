package com.expedia.bookings.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import rx.Observable
import org.joda.time.LocalDate

open class CalendarDialogFragment(val baseSearchViewModel: BaseSearchViewModel) : DialogFragment() {

    var oldCalendarSelection: Pair<LocalDate, LocalDate>? = null;
    var userTappedDone = false

    companion object {
        fun createFragment(searchViewModel: BaseSearchViewModel): CalendarDialogFragment {
            val fragment = CalendarDialogFragment(searchViewModel)
            // Because we are passing baseSearchViewModel as an argument in the constructor, on ChangeOfConfig (or any activity resuling in
            // recreation of FlightActivity) fragment requires value of the baseSearchVieModel, hence retaining old instance.
            fragment.retainInstance = true
            return fragment
        }
    }

    val calendarDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_calendar_search, null)
        view
    }

    val calendar: CalendarPicker by lazy {
        val calendarPickerView = calendarDialogView.findViewById(R.id.calendar) as CalendarPicker
        val maxDate = LocalDate.now().plusDays(baseSearchViewModel.getMaxDateRange())
        calendarPickerView.setSelectableDateRange(baseSearchViewModel.getFirstAvailableDate(), maxDate)
        calendarPickerView.setMaxSelectableDateRange(baseSearchViewModel.getMaxSearchDurationDays())

        val monthView = calendarPickerView.findViewById(R.id.month) as MonthView
        val dayOfWeek = calendarPickerView.findViewById(R.id.days_of_week) as DaysOfWeekView
        dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

        BaseSearchPresenter.styleCalendar(context, calendarPickerView, monthView, dayOfWeek)

        calendarPickerView.setDateChangedListener { start, end ->
            if (calendar.visibility == CardView.VISIBLE) {
                if (start != null && JodaUtils.isEqual(start, end) && !baseSearchViewModel.sameStartAndEndDateAllowed()) {
                    if (!JodaUtils.isEqual(end, maxDate)) {
                        calendarPickerView.setSelectedDates(start, end.plusDays(1))
                    } else {
                        // Do not select an end date beyond the allowed range
                        calendarPickerView.setSelectedDates(start, null)
                    }

                } else {
                    baseSearchViewModel.datesUpdated(start, end)
                }
                updateDoneVisibilityForDate(start)

            } else {
                baseSearchViewModel.datesUpdated(start, end)
            }
        }
        calendarPickerView.setYearMonthDisplayedChangedListener {
            calendarPickerView.hideToolTip()
        }

        Observable.zip(baseSearchViewModel.calendarTooltipTextObservable, baseSearchViewModel.calendarTooltipContDescObservable, {
            tooltipText, tooltipContDescription ->
            val (top, bottom) = tooltipText
            object {
                val top = top
                val bottom = bottom
                val tooltipContDescription = tooltipContDescription
            }
        }).subscribe(endlessObserver { calendarPickerView.setToolTipText(it.top, it.bottom, it.tooltipContDescription, true) })

        baseSearchViewModel.dateInstructionObservable.subscribe({
            calendar.setInstructionText(it)
        })

        calendarPickerView.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendarPickerView
    }

    private fun updateDoneVisibilityForDate(start: LocalDate?) {
        if (dialog != null) { //callback may happen before we're visible on the screen
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = start != null
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (!userTappedDone) {
            calendar.visibility = CardView.GONE // ensures tooltip does not reopen
            baseSearchViewModel.datesUpdated(oldCalendarSelection?.first, oldCalendarSelection?.second)
            calendar.setSelectedDates(oldCalendarSelection?.first, oldCalendarSelection?.second)
            oldCalendarSelection = null
        }
        userTappedDone = false
        calendar.hideToolTip()
        baseSearchViewModel.a11yFocusSelectDatesObservable.onNext(Unit)
    }

    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        calendar
        removeParentView()
        builder.setView(calendarDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldCalendarSelection = null
            calendar.visibility = CardView.INVISIBLE
            calendar.hideToolTip()
            if (calendar.startDate != null && calendar.endDate == null) {
                val endDate = if (!baseSearchViewModel.isStartDateOnlyAllowed()) calendar.startDate.plusDays(1) else null
                calendar.setSelectedDates(calendar.startDate, endDate)
            }
            userTappedDone = true
            baseSearchViewModel.dateSetObservable.onNext(Unit)
            dialog.dismiss()
        })

        var dialog: AlertDialog = builder.create()
        dialog.setOnShowListener() {
            setMaxSelectableDateRange()
            calendar.visibility = CardView.VISIBLE
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = calendar.startDate != null
            oldCalendarSelection = Pair(calendar.startDate, calendar.endDate)
            calendar.setInstructionText(baseSearchViewModel.getDateInstructionText(calendar.startDate, calendar.endDate))

            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        calendar.setSelectedDates(baseSearchViewModel.startDate(), baseSearchViewModel.endDate())

        if (savedInstanceState != null) {
            dismiss()
        }
        return dialog
    }

    private fun removeParentView() {
        if (calendarDialogView.parent != null)
            (calendarDialogView.parent as ViewGroup).removeAllViews()
    }

    private fun setMaxSelectableDateRange() {
        calendar.setMaxSelectableDateRange(baseSearchViewModel.getMaxSearchDurationDays())
    }
}
