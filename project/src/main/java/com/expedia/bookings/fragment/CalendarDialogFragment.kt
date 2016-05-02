package com.expedia.bookings.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.HotelSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate

class CalendarDialogFragment(val baseSearchViewModel: BaseSearchViewModel) : DialogFragment() {

    var oldCalendarSelection: Pair<LocalDate, LocalDate>? = null;
    var userTappedDone = false

    companion object {
        fun createFragment(searchViewModel: BaseSearchViewModel): CalendarDialogFragment {
            val fragment = CalendarDialogFragment(searchViewModel)
            return fragment
        }
    }

    val calendarDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_hotel_calendar_search, null)
        view
    }

    val calendar: CalendarPicker by lazy {
        val calendarPickerView = calendarDialogView.findViewById(R.id.calendar) as CalendarPicker
        val maxDate = LocalDate.now().plusDays(baseSearchViewModel.getMaxDateRange())
        calendarPickerView.setSelectableDateRange(LocalDate.now(), maxDate)
        calendarPickerView.setMaxSelectableDateRange(baseSearchViewModel.getMaxSearchDurationDays())

        val monthView = calendarPickerView.findViewById(R.id.month) as MonthView
        val dayOfWeek = calendarPickerView.findViewById(R.id.days_of_week) as DaysOfWeekView
        dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

        BaseSearchPresenter.styleCalendar(context, calendarPickerView, monthView, dayOfWeek)

        calendarPickerView.setDateChangedListener { start, end ->
            if (calendar.visibility == CardView.VISIBLE) {
                if (start != null && JodaUtils.isEqual(start, end)) {
                    if (!JodaUtils.isEqual(end, maxDate)) {
                        calendarPickerView.setSelectedDates(start, end.plusDays(1))
                    } else {
                        // Do not select an end date beyond the allowed range
                        calendarPickerView.setSelectedDates(start, null)
                    }
                } else {
                    baseSearchViewModel.datesObserver.onNext(Pair(start, end))
                }
                updateDoneVisibilityForDate(start)

            } else {
                baseSearchViewModel.datesObserver.onNext(Pair(start, end))
            }
        }
        calendarPickerView.setYearMonthDisplayedChangedListener {
            calendarPickerView.hideToolTip()
        }

        baseSearchViewModel.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendarPickerView.setToolTipText(top, bottom, true)
        })

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
            baseSearchViewModel.datesObserver.onNext(oldCalendarSelection)
            calendar.setSelectedDates(oldCalendarSelection?.first, oldCalendarSelection?.second)
            oldCalendarSelection = null
        }
        userTappedDone = false
        calendar.hideToolTip()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        calendar
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
            dialog.dismiss()
        })

        var dialog: AlertDialog = builder.create()
        dialog.setOnShowListener() {
            calendar.visibility = CardView.VISIBLE
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = calendar.startDate != null
            oldCalendarSelection = Pair(calendar.startDate, calendar.endDate)
            calendar.setInstructionText(baseSearchViewModel.computeDateInstructionText(calendar.startDate, calendar.endDate))

            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        calendar.setSelectedDates(baseSearchViewModel.startDate(), baseSearchViewModel.endDate())

        return dialog
    }
}