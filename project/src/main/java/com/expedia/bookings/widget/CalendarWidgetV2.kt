package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject

class CalendarWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputCardView(context, attrs) {
    var oldCalendarSelection: Pair<LocalDate, LocalDate>? = null;
    val hotelSearchViewModelSubject = BehaviorSubject.create<HotelSearchViewModel>()
    val calendarDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_hotel_calendar_search, null)
        view
    }

    val calendar: CalendarPicker by lazy {
        val calendarPickerView = calendarDialogView.findViewById(R.id.calendar) as CalendarPicker
        val maxDate = LocalDate.now().plusDays(resources.getInteger(R.integer.calendar_max_selectable_date_range))
        calendarPickerView.setSelectableDateRange(LocalDate.now(), maxDate)
        calendarPickerView.setMaxSelectableDateRange(resources.getInteger(R.integer.calendar_max_selectable_date_range))

        val monthView = calendarPickerView.findViewById(R.id.month) as MonthView
        val dayOfWeek = calendarPickerView.findViewById(R.id.days_of_week) as DaysOfWeekView
        dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

        BaseSearchPresenter.styleCalendar(context, calendarPickerView, monthView, dayOfWeek)

        calendarPickerView.setDateChangedListener { start, end ->
            if (start != null && JodaUtils.isEqual(start, end)) {
                if (!JodaUtils.isEqual(end, maxDate)) {
                    calendarPickerView.setSelectedDates(start, end.plusDays(1))
                } else {
                    // Do not select an end date beyond the allowed range
                    calendarPickerView.setSelectedDates(start, null)
                }
            } else {
                hotelSearchViewModelSubject.value.datesObserver.onNext(Pair(start, end))
            }

            //only enable the done button if at least start is selected - we'll default end date if necessary
            calendarDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = start != null
        }
        calendarPickerView.setYearMonthDisplayedChangedListener {
            calendarPickerView.hideToolTip()
        }

        hotelSearchViewModelSubject.value.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendarPickerView.setToolTipText(top, bottom, true)
        })

        hotelSearchViewModelSubject.value.searchParamsObservable.subscribe {
            calendarPickerView.hideToolTip()
        }
        hotelSearchViewModelSubject.value.dateTextObservable.subscribeText(this.text)

        hotelSearchViewModelSubject.value.dateInstructionObservable.subscribe({
            calendar.setInstructionText(it)
        })

        calendarPickerView.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendarPickerView
    }

    val calendarDialog: AlertDialog by lazy {
        var userTappedDone = false
        val builder = AlertDialog.Builder(context)
        calendar
        builder.setView(calendarDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldCalendarSelection = null
            calendar.hideToolTip()
            if(calendar.startDate != null && calendar.endDate == null) {
                calendar.setSelectedDates(calendar.startDate, calendar.startDate.plusDays(1))
            }
            userTappedDone = true
            dialog.dismiss()
        })

        var dialog: AlertDialog = builder.create()
        dialog.setOnShowListener() {
            calendar.visibility = VISIBLE
            calendarDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = calendar.startDate != null
            oldCalendarSelection = Pair(calendar.startDate, calendar.endDate)
            calendar.setInstructionText(HotelSearchViewModel.computeDateInstructionText(context, calendar.startDate, calendar.endDate))

            dialog.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener {
            if (!userTappedDone) {
                calendar.visibility = GONE // ensures tooltip does not reopen
                hotelSearchViewModelSubject.value.datesObserver.onNext(oldCalendarSelection)
                calendar.setSelectedDates(oldCalendarSelection?.first, oldCalendarSelection?.second)
                oldCalendarSelection = null
            }
            userTappedDone = false
            calendar.hideToolTip()
        }

        // force the window to be the full width so that it's usable on phones
        // only phones, only portrait (looks bad on tablets)
 
        dialog
    }

}