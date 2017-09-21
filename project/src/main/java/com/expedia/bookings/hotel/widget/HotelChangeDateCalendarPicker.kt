package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelCalendarInstructions
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.shared.CalendarListener
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class HotelChangeDateCalendarPicker(context: Context, attrs: AttributeSet?) : CalendarPicker(context, attrs) {
    val doneEnabledSubject = PublishSubject.create<Boolean>()

    lateinit var instructions: HotelCalendarInstructions

    override fun onFinishInflate() {
        super.onFinishInflate()
        setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
    }

    fun bind(rules: CalendarRules, listener: CalendarListener,
             instructions: HotelCalendarInstructions) {
        this.instructions = instructions

        val maxDate = LocalDate.now().plusDays(rules.getMaxDateRange())
        setSelectableDateRange(rules.getFirstAvailableDate(), maxDate)
        setMaxSelectableDateRange(rules.getMaxSearchDurationDays())

        val monthView = findViewById<MonthView>(R.id.month)
        val dayOfWeek = findViewById<DaysOfWeekView>(R.id.days_of_week)
        dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

        BaseSearchPresenter.styleCalendar(context, this, monthView, dayOfWeek)

        this.setDateChangedListener { start, end ->
            updateInstructions(start, end)
            if (visibility == View.VISIBLE) {
                if (start != null && JodaUtils.isEqual(start, end) && !rules.sameStartAndEndDateAllowed()) {
                    if (!JodaUtils.isEqual(end, maxDate)) {
                        setSelectedDates(start, end.plusDays(1))
                    } else {
                        // Do not select an end date beyond the allowed range
                        setSelectedDates(start, null)
                    }

                } else {
                    listener.datesUpdated(start, end)
                }

                doneEnabledSubject.onNext(start != null)
            } else {
                listener.datesUpdated(start, end)
            }
        }

        this.setYearMonthDisplayedChangedListener {
            hideToolTip()
        }
    }

    fun setDates(startDate: LocalDate?, endDate: LocalDate?) {
        setSelectedDates(startDate, endDate)
        updateInstructions(startDate, endDate)
    }

    private fun updateInstructions(startDate: LocalDate?, endDate: LocalDate?) {
        setInstructionText(instructions.getDateInstructionText(startDate, endDate))
        val toolTipTitle = instructions.getToolTipTitle(startDate, endDate)
        val toolTipSubtitle = instructions.getToolTipInstructions(endDate)
        val toolTipContDesc = instructions.getToolTipContDesc(startDate, endDate)

        setToolTipText(toolTipTitle, toolTipSubtitle, toolTipContDesc, true)
    }
}