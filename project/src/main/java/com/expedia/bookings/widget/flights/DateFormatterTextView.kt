package com.expedia.bookings.widget.flights

import android.content.Context
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import org.joda.time.LocalDate

class DateFormatterTextView(context: Context, attr: AttributeSet): TextView(context, attr) {
    lateinit var startDate: LocalDate
    var endDate: LocalDate? = null
    private var invalidStyle: String
    private var invalidColor: Int

    init {
        val attributes = context.obtainStyledAttributes(attr, R.styleable.DateFormatterTextView)
        invalidStyle = attributes.getString(R.styleable.DateFormatterTextView_invalidStyle)
        invalidColor = attributes.getColor(R.styleable.DateFormatterTextView_invalidColor, 0x000000)
        attributes.recycle()
    }

    fun setDate(startDate: LocalDate, endDate: LocalDate? = null) {
        this.startDate = startDate
        this.endDate = endDate
        val currentDate = LocalDate.now()
        val startDateFormatted = LocaleBasedDateFormatUtils.localDateToMMMd(this.startDate)
        val spanBuilder = SpannableBuilder()

        if (startDate.isBefore(currentDate)) {
            spanBuilder.append(startDateFormatted, getTypeface(invalidStyle),
                    ForegroundColorSpan(invalidColor))
        } else {
            spanBuilder.append(startDateFormatted, StyleSpan(Typeface.NORMAL))
        }

        endDate?.let {
            val endDateFormatted = LocaleBasedDateFormatUtils.localDateToMMMd(endDate)
            if (it.isBefore(currentDate)) {
                spanBuilder.append("  -  " + endDateFormatted, getTypeface(invalidStyle),
                        ForegroundColorSpan(invalidColor))
            } else {
                spanBuilder.append("  -  " + endDateFormatted, StyleSpan(Typeface.NORMAL))
            }
        }
        text = spanBuilder.build()
    }

    fun getTypeface(styleStr: String): StyleSpan {
        when(styleStr) {
            "italic" -> return StyleSpan(Typeface.ITALIC)
            else -> return StyleSpan(Typeface.NORMAL)
        }
    }
}
