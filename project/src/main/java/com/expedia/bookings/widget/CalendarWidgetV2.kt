package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject

public class CalendarWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputCardView(context, attrs) {
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
        calendarPickerView.setDateChangedListener { start, end ->
            if (JodaUtils.isEqual(start, end)) {
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
            calendarDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = start != null
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
        calendarPickerView.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendarPickerView
    }

    val calendarDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        calendar
        builder.setView(calendarDialogView)
        builder.setTitle(R.string.select_dates)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldCalendarSelection = null
            calendar.hideToolTip()
            dialog.dismiss()
        })
        var dialog: AlertDialog = builder.create()
        dialog.setOnShowListener() {
            calendar.visibility = VISIBLE
            calendarDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = calendar.startDate != null
            oldCalendarSelection = Pair(calendar.startDate, calendar.endDate)
        }
        dialog.setOnDismissListener {
            if (oldCalendarSelection != null && oldCalendarSelection?.first != null) {
                calendar.visibility = GONE // ensures tooltip does not reopen
                //if it's not null, the user dismissed the dialog, otherwise we clear it on Done
                hotelSearchViewModelSubject.value.datesObserver.onNext(oldCalendarSelection)
                calendar.setSelectedDates(oldCalendarSelection?.first, oldCalendarSelection?.second)
                oldCalendarSelection = null
            }
            calendar.hideToolTip()
        }

        dialog
    }

    init {
        this.setOnClickListener {
            calendarDialog.show()
        }
    }
}