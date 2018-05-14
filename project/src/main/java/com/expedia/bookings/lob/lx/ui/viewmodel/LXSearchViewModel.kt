package com.expedia.bookings.lob.lx.ui.viewmodel

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.shared.util.CalendarDateFormatter
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.LxCalendarRules
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isLXMultipleDatesSearchEnabled
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class LXSearchViewModel(context: Context) : BaseSearchViewModel(context) {
    override fun getCalendarRules(): CalendarRules {
        return LxCalendarRules(context)
    }

    val lxParamsBuilder = LxSearchParams.Builder()
    val searchParamsObservable = PublishSubject.create<LxSearchParams>()

    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        locationTextObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            var modQualified = Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated()
            val lxSearchParams = getParamsBuilder().modQualified(modQualified).build()
            searchParamsObservable.onNext(lxSearchParams)
        } else {
            if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun getParamsBuilder(): LxSearchParams.Builder {
        return lxParamsBuilder
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates
        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        if (isLXMultipleDatesSearchEnabled()) {
            calendarTooltipTextObservable.onNext(getToolTipText(start, end))
            calendarTooltipContDescObservable.onNext(getToolTipContDesc(start, end))
        }
        if (start != null && end == null) {
            if (isLXMultipleDatesSearchEnabled()) {
                end = start
            } else {
                end = start.plusDays(context.resources.getInteger(R.integer.lx_default_search_range))
            }
        }

        super.onDatesChanged(Pair(start, end))
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            if (isLXMultipleDatesSearchEnabled()) {
                return context.getString(R.string.select_lx_trip_start_dates)
            } else {
                return context.getString(R.string.select_lx_search_dates)
            }
        } else if (end == null && isLXMultipleDatesSearchEnabled()) {
            return getNoEndDateText(start, false)
        }
        if (isLXMultipleDatesSearchEnabled()) {
            return getCompleteDateText(start!!, end!!, false)
        } else {
            return LocaleBasedDateFormatUtils.localDateToMMMd(start!!)
        }
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (isLXMultipleDatesSearchEnabled()) {
            if (end == null) {
                return context.getString(R.string.lx_calendar_tooltip_bottom)
            } else if (end == start!!.plusDays(Constants.LX_CALENDAR_MAX_DATE_SELECTION)) {
                return context.getString(R.string.lx_calendar_tooltip_maximum_days_limit)
            }
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        if (isLXMultipleDatesSearchEnabled()) {
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_dates), "")
            } else {
                return context.getString(R.string.select_dates)
            }
        } else {
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_start_date), "")
            } else {
                return context.getString(R.string.select_start_date)
            }
        }
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        var selectCheckoutText: String by Delegates.notNull<String>()
        if (isLXMultipleDatesSearchEnabled()) {
            selectCheckoutText = Phrase.from(context, R.string.select_lx_trip_end_date_TEMPLATE)
                    .put("selecteddate", LocaleBasedDateFormatUtils.localDateToMMMd(start!!))
                    .format().toString()
        } else {
            selectCheckoutText = LocaleBasedDateFormatUtils.localDateToMMMMd(start!!)
        }
        if (forContentDescription) {
            if (isLXMultipleDatesSearchEnabled()) {
                return CalendarDateFormatter.getDateAccessibilityText(context, selectCheckoutText, "")
            } else {
                return getDateAccessibilityText(context.getString(R.string.select_start_date), LocaleBasedDateFormatUtils.localDateToMMMMd(start))
            }
        }
        return selectCheckoutText
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        var completeDateText: String by Delegates.notNull<String>()
        if (isLXMultipleDatesSearchEnabled()) {
            completeDateText = createCompleteDateText(start, end, forContentDescription)
        } else {
            completeDateText = LocaleBasedDateFormatUtils.localDateToMMMMd(start)
        }
        if (forContentDescription) {
            if (isLXMultipleDatesSearchEnabled()) {
                return CalendarDateFormatter.getDateAccessibilityText(context, context.getString(R.string.select_dates),
                        completeDateText)
            } else {
                return getDateAccessibilityText(context.getString(R.string.select_start_date), completeDateText)
            }
        }
        return completeDateText
    }

    private fun createCompleteDateText(start: LocalDate, end: LocalDate, isContentDescription: Boolean): String {
        val dateTextBuilder = SpannableBuilder()
        val daysString = getNoOfDaysText(start, end)
        val dateRangeText = if (isContentDescription) {
            CalendarDateFormatter.formatStartToEnd(context, start, end)
        } else {
            CalendarDateFormatter.formatStartDashEnd(context, start, end)
        }

        dateTextBuilder.append(dateRangeText)
        dateTextBuilder.append(" ")
        dateTextBuilder.append(context.resources.getString(R.string.nights_count_TEMPLATE, daysString), RelativeSizeSpan(0.8f))
        return dateTextBuilder.build().toString()
    }

    fun getToolTipContDesc(startDate: LocalDate?, endDate: LocalDate?): String {
        if (startDate == null && endDate == null) {
            return context.getString(R.string.select_dates_proper_case)
        } else if (endDate == null) {
            return Phrase.from(context, R.string.calendar_start_date_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate!!))
                    .put("instructiontext", getCalendarToolTipInstructions(startDate, endDate))
                    .format().toString()
        }
        return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                .put("selecteddate", CalendarDateFormatter.formatStartToEnd(context, startDate!!, endDate))
                .format().toString()
    }

    fun getNoOfDaysText(start: LocalDate?, end: LocalDate?): String {
        val daysCount = JodaUtils.daysBetween(start, end) + 1
        return if (daysCount == 1) {
            context.getString(R.string.select_lx_trip_length_single_day)
        } else {
            Phrase.from(context, R.string.select_lx_trip_length_multiple_days_TEMPLATE)
                    .put("noofdays", daysCount)
                    .format().toString()
        }
    }
}
