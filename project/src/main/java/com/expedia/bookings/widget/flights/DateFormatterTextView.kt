package com.expedia.bookings.widget.flights

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import org.joda.time.LocalDate

class DateFormatterTextView(context: Context, attr: AttributeSet): TextView(context, attr) {
    val attributes: TypedArray
    lateinit var startDate: LocalDate
    var endDate: LocalDate? = null

    init {
        attributes = context.obtainStyledAttributes(attr, R.styleable.DateFormatterTextView)
    }

    fun setDate(startDate: LocalDate, endDate: LocalDate? = null) {
        val invalidStyle = attributes.getString(R.styleable.DateFormatterTextView_invalidStyle)
        val invalidColor = attributes.getColor(R.styleable.DateFormatterTextView_invalidColor, 0x000000)
        attributes.recycle()
        this.startDate = startDate
        this.endDate = endDate
        val currentDate = LocalDate.now()
        val startDateFormatted = DateFormatUtils.formatLocalDateToMMMdBasedOnLocale(startDate)
        val spanBuilder = SpannableBuilder()

        if (startDate.isBefore(currentDate)) {
            spanBuilder.append(startDateFormatted, getTypeface(invalidStyle),
                    ForegroundColorSpan(invalidColor))
        } else {
            spanBuilder.append(startDateFormatted, StyleSpan(Typeface.NORMAL))
        }

        endDate?.let {
            val endDateFormatted = DateFormatUtils.formatLocalDateToMMMdBasedOnLocale(endDate)
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
