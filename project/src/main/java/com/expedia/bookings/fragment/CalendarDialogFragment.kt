package com.expedia.bookings.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.isRecentSearchesForFlightsEnabled
import com.expedia.bookings.widget.shared.CalendarStyleUtil
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate

open class CalendarDialogFragment() : DialogFragment() {

    var baseSearchViewModel: BaseSearchViewModel? = null
    var rules: CalendarRules? = null
    var isShowInitiated = false

    constructor(vm: BaseSearchViewModel, rules: CalendarRules) : this() {
        baseSearchViewModel = vm
        this.rules = rules
    }
    var oldCalendarSelection: Pair<LocalDate, LocalDate>? = null
    var userTappedDone = false

    companion object {
        fun createFragment(searchViewModel: BaseSearchViewModel, rules: CalendarRules): CalendarDialogFragment {
            val fragment = CalendarDialogFragment(searchViewModel, rules)
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
        val calendarPickerView = calendarDialogView.findViewById<CalendarPicker>(R.id.calendar)
        val vm = baseSearchViewModel
        if (vm != null && rules != null) {
            val maxDate = LocalDate.now().plusDays(rules!!.getMaxDateRange())
            calendarPickerView.setSelectableDateRange(rules!!.getFirstAvailableDate(), maxDate)
            calendarPickerView.setMaxSelectableDateRange(rules!!.getMaxSearchDurationDays())

            val monthView = calendarPickerView.findViewById<MonthView>(R.id.month)
            val dayOfWeek = calendarPickerView.findViewById<DaysOfWeekView>(R.id.days_of_week)
            dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

            CalendarStyleUtil.style(context, calendarPickerView, monthView, dayOfWeek)

            calendarPickerView.setDateChangedListener { start, end ->
                if (calendar.visibility == CardView.VISIBLE) {
                    if (start != null && JodaUtils.isEqual(start, end) && !rules!!.sameStartAndEndDateAllowed()) {
                        if (!JodaUtils.isEqual(end, maxDate)) {
                            calendarPickerView.setSelectedDates(start, end.plusDays(1))
                        } else {
                            // Do not select an end date beyond the allowed range
                            calendarPickerView.setSelectedDates(start, null)
                        }
                    } else {
                        vm.datesUpdated(start, end)
                    }
                    updateDoneVisibilityForDate(start)
                } else {
                    vm.datesUpdated(start, end)
                }
            }
            calendarPickerView.setYearMonthDisplayedChangedListener {
                calendarPickerView.hideToolTip()
            }

            ObservableOld.zip(vm.calendarTooltipTextObservable, vm.calendarTooltipContDescObservable, {
                tooltipText, tooltipContDescription ->
                val (top, bottom) = tooltipText
                object {
                    val top = top
                    val bottom = bottom
                    val tooltipContDescription = tooltipContDescription
                }
            }).subscribe(endlessObserver { calendarPickerView.setToolTipText(it.top, it.bottom, it.tooltipContDescription, true) })

            vm.dateInstructionObservable.subscribe({
                calendar.setInstructionText(it)
            })

            calendarPickerView.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        }
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
            baseSearchViewModel?.datesUpdated(oldCalendarSelection?.first, oldCalendarSelection?.second)
            calendar.setSelectedDates(oldCalendarSelection?.first, oldCalendarSelection?.second)
            oldCalendarSelection = null
        }

        userTappedDone = false
        calendar.hideToolTip()
        baseSearchViewModel?.a11yFocusSelectDatesObservable?.onNext(Unit)
        isShowInitiated = false
    }

    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        isShowInitiated = true
        super.show(manager, tag)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        if (savedInstanceState != null) {
            val a = builder.create()
            val handler = Handler()
            handler.postDelayed({
                a.dismiss()
            }, 200)

            return a
        }
        calendar
        removeParentView()
        builder.setView(calendarDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, _ ->
            calendar.visibility = CardView.INVISIBLE
            calendar.hideToolTip()
            if (calendar.startDate != null && calendar.endDate == null) {
                val endDate = if (!(rules?.isStartDateOnlyAllowed() ?: false)) calendar.startDate.plusDays(1) else null
                calendar.setSelectedDates(calendar.startDate, endDate)
            }
            if (isRecentSearchesForFlightsEnabled(context)) {
                baseSearchViewModel?.dateSelectionChanged?.onNext(!isCalenderDatesChanged(oldCalendarSelection!!, Pair(calendar.startDate, calendar.endDate)))
            }
            oldCalendarSelection = null
            userTappedDone = true
            baseSearchViewModel?.dateSetObservable?.onNext(Unit)
            dialog.dismiss()
        })

        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            setMaxSelectableDateRange()
            calendar.visibility = CardView.VISIBLE
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = calendar.startDate != null
            oldCalendarSelection = Pair(calendar.startDate, calendar.endDate)
            calendar.setInstructionText(baseSearchViewModel?.getDateInstructionText(calendar.startDate, calendar.endDate))

            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        calendar.setSelectedDates(baseSearchViewModel?.startDate(), baseSearchViewModel?.endDate())

        return dialog
    }

    private fun removeParentView() {
        if (calendarDialogView.parent != null)
            (calendarDialogView.parent as ViewGroup).removeAllViews()
    }

    private fun setMaxSelectableDateRange() {
        calendar.setMaxSelectableDateRange(rules?.getMaxSearchDurationDays() ?: 0)
    }

    fun isCalenderDatesChanged(calender1: Pair<LocalDate?, LocalDate?>, calender2: Pair<LocalDate?, LocalDate?>): Boolean {
        val isStartDateEqual = compareDates(calender1.first, calender2.first)
        val isEndDateEqual = compareDates(calender1.second, calender2.second)
        return isStartDateEqual && isEndDateEqual
    }

    private fun compareDates(date1: LocalDate?, date2: LocalDate?): Boolean {
        if (date1 == null && date2 == null) {
            return true
        } else if ((date1 == null && date2 != null) || (date1 != null && date2 == null)) {
            return false
        } else {
            return date1?.isEqual(date2) ?: false
        }
    }
}
