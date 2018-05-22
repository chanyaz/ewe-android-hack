package com.mobiata.android.time.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mobiata.android.time.R
import com.mobiata.android.time.util.CalendarConstants
import com.mobiata.android.time.util.CalendarUtils
import com.mobiata.android.util.Ui
import org.joda.time.LocalDate
import org.joda.time.YearMonth

class HolidayWidget(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {
    var holidayInfo = emptyMap<LocalDate, String>()

    private val COLS = CalendarConstants.COLS
    private val ROWS = CalendarConstants.ROWS
    private val todayDate = LocalDate.now()

    private var firstVisibleDay: LocalDate? = null
    private var lastVisibleDay: LocalDate? = null

    val holidayRowLayoutContainer: LinearLayout by lazy {
        Ui.findView<LinearLayout>(this, R.id.holiday_row_layout)
    }

    fun setDisplayedYearMonth(displayYearMonth: YearMonth) {
        holidayRowLayoutContainer.removeAllViews()
        val visibleDays = CalendarUtils.computeVisibleDays(displayYearMonth, ROWS, COLS)
        firstVisibleDay = visibleDays[0][0]
        lastVisibleDay = visibleDays[ROWS - 1][COLS - 1]
        holidayInfo.forEach { date, holidayName ->
            if (isHolidayVisible(date)) {
                populateHolidayRow(date, holidayName)
            }
        }
        if (holidayRowLayoutContainer.childCount <= 0) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }

    private fun isHolidayVisible(date: LocalDate) =
            (date.isAfter(firstVisibleDay) && date.isBefore(lastVisibleDay)) || date.isEqual(firstVisibleDay) || date.isEqual(lastVisibleDay)

    @Suppress("SetTextI18n")
    private fun populateHolidayRow(date: LocalDate, holidayName: String) {
        val holidayRowLayout = View.inflate(context, R.layout.holiday_calendar_row, null) as LinearLayout
        val holidayRowTextView = holidayRowLayout.findViewById<TextView>(R.id.holiday_row_text)
        val holidayRowImageView = holidayRowLayout.findViewById<ImageView>(R.id.holiday_row_image)
        val formattedDate = date.toString("MMM dd, E")
        holidayRowTextView.text = "$formattedDate - $holidayName"
        if (date.isBefore(todayDate)) {
            holidayRowImageView.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.gray600), PorterDuff.Mode.SRC_IN)
            holidayRowTextView.setTextColor(ContextCompat.getColor(context, R.color.gray600))
        } else {
            holidayRowImageView.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.blue2), PorterDuff.Mode.SRC_IN)
            holidayRowTextView.setTextColor(ContextCompat.getColor(context, R.color.gray900))
        }
        holidayRowLayoutContainer.addView(holidayRowLayout)
    }
}
