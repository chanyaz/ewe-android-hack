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

class DateFormatterTextView(context: Context, attr: AttributeSet) : TextView(context, attr) {
    private val NORMAL = 0
    private val ITALIC = 1
    var invalidStyle: Int = 0
    var invalidColor: Int = 0

    init {
        val attributes = context.obtainStyledAttributes(attr, R.styleable.DateFormatterTextView)
        invalidStyle = attributes.getInt(R.styleable.DateFormatterTextView_invalidStyle, NORMAL)
        invalidColor = attributes.getColor(R.styleable.DateFormatterTextView_invalidColor, 0x000000)
        attributes.recycle()
    }

    fun setDate(startDate: LocalDate, endDate: LocalDate? = null) {
        val currentDate = LocalDate.now()
        val startDateFormatted = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)
        val spanBuilder = SpannableBuilder()

        if (startDate.isBefore(currentDate)) {
            spanBuilder.append(startDateFormatted, getTypeface(invalidStyle),
                    ForegroundColorSpan(invalidColor))
        } else {
            spanBuilder.append(startDateFormatted, StyleSpan(Typeface.NORMAL))
        }

        endDate?.let {
            val endDateFormatted = LocaleBasedDateFormatUtils.localDateToMMMd(it)
            if (it.isBefore(currentDate)) {
                spanBuilder.append("  -  " + endDateFormatted, getTypeface(invalidStyle),
                        ForegroundColorSpan(invalidColor))
            } else {
                spanBuilder.append("  -  " + endDateFormatted, StyleSpan(Typeface.NORMAL))
            }
        }
        text = spanBuilder.build()
    }

    fun getTypeface(styleType: Int): StyleSpan {
        when (styleType) {
            ITALIC -> return StyleSpan(Typeface.ITALIC)
            else -> return StyleSpan(Typeface.NORMAL)
        }
    }
}
