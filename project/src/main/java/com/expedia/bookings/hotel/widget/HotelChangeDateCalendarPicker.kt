package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelCalendarDirections
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.shared.CalendarStyleUtil
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

class HotelChangeDateCalendarPicker(context: Context, attrs: AttributeSet?) : CalendarPicker(context, attrs) {
    val datesUpdatedSubject = PublishSubject.create<HotelStayDates>()
    val doneEnabledSubject = PublishSubject.create<Boolean>()

    lateinit var directions: HotelCalendarDirections

    override fun onFinishInflate() {
        super.onFinishInflate()
        setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
    }

    fun bind(rules: CalendarRules, directions: HotelCalendarDirections) {
        this.directions = directions

        val maxDate = LocalDate.now().plusDays(rules.getMaxDateRange())
        setSelectableDateRange(rules.getFirstAvailableDate(), maxDate)
        setMaxSelectableDateRange(rules.getMaxSearchDurationDays())
        setSameStartAndEndDateAllowed(rules.sameStartAndEndDateAllowed())

        val monthView = findViewById<MonthView>(R.id.month)
        val dayOfWeek = findViewById<DaysOfWeekView>(R.id.days_of_week)
        dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

        CalendarStyleUtil.style(context, this, monthView, dayOfWeek)

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
                    datesUpdatedSubject.onNext(HotelStayDates(start, end))
                }

                doneEnabledSubject.onNext(start != null)
            } else {
                datesUpdatedSubject.onNext(HotelStayDates(start, end))
            }
        }

        this.setYearMonthDisplayedChangedListener {
            hideToolTip()
        }
    }

    fun setDates(hotelStayDates: HotelStayDates?) {
        setSelectedDates(hotelStayDates?.getStartDate(), hotelStayDates?.getEndDate())
        updateInstructions(hotelStayDates?.getStartDate(), hotelStayDates?.getEndDate())
    }

    private fun updateInstructions(startDate: LocalDate?, endDate: LocalDate?) {
        setInstructionText(directions.getDateInstructionText(startDate, endDate))
        val toolTipTitle = directions.getToolTipTitle(startDate, endDate)
        val toolTipSubtitle = directions.getToolTipInstructions(endDate)
        val toolTipContDesc = directions.getToolTipContDesc(startDate, endDate)

        setToolTipText(toolTipTitle, toolTipSubtitle, toolTipContDesc, true)
    }
}
