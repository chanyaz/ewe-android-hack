package com.expedia.vm

import android.content.Context
import android.support.annotation.CallSuper
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.HolidayCalendarResponse
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.shared.util.CalendarDateFormatter
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate

abstract class BaseSearchViewModel(val context: Context) {
    // Outputs
    var dateTextObservable = BehaviorSubject.create<CharSequence>()
    val dateAccessibilityObservable = BehaviorSubject.create<CharSequence>()
    val dateInstructionObservable = PublishSubject.create<CharSequence>()
    val dateSetObservable = PublishSubject.create<Unit>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
    val calendarTooltipContDescObservable = PublishSubject.create<String>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoDestinationObservable = PublishSubject.create<Unit>()
    val errorNoOriginObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val errorMaxDurationObservable = PublishSubject.create<String>()
    val errorMaxRangeObservable = PublishSubject.create<String>()
    val travelersObservable = BehaviorSubject.create<TravelerParams>()
    val errorOriginSameAsDestinationObservable = PublishSubject.create<String>()
    val hasValidDatesObservable = PublishSubject.create<Boolean>()
    var holidayCalendarResponse = HolidayCalendarResponse()
    val formattedOriginObservable = PublishSubject.create<String>()
    val formattedDestinationObservable = PublishSubject.create<String>()

    var a11yFocusSelectDatesObservable = BehaviorSubject.create<Unit>()
    val dateSelectionChanged = PublishSubject.create<Boolean>()

    protected var selectedDates: Pair<LocalDate?, LocalDate?> = Pair(null, null)

    init {
        updateTraveler()
    }

    abstract fun getCalendarRules(): CalendarRules
    abstract fun getParamsBuilder(): BaseSearchParams.Builder
    abstract fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence

    protected abstract fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String
    protected abstract fun getEmptyDateText(forContentDescription: Boolean): String
    protected abstract fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String
    protected abstract fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String

    open val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        setOriginText(suggestion)
    }

    open val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        setDestinationText(suggestion)
    }

    fun datesUpdated(startDate: LocalDate?, endDate: LocalDate?) {
        onDatesChanged(Pair(startDate, endDate))
    }

    fun startDate(): LocalDate? {
        return selectedDates.first
    }

    fun endDate(): LocalDate? {
        return selectedDates.second
    }

    open fun isTalkbackActive(): Boolean {
        return AccessibilityUtil.isTalkBackEnabled(context)
    }

    protected open var requiredSearchParamsObserver = endlessObserver<Unit> {
        // open so HotelSearchViewModel can override it
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
    }

    protected open fun updateTraveler() {
        travelersObservable.subscribe { update ->
            getParamsBuilder().adults(update.numberOfAdults)
            getParamsBuilder().children(update.childrenAges)
            requiredSearchParamsObserver.onNext(Unit)
        }
    }

    protected fun setOriginText(suggestion: SuggestionV4) {
        getParamsBuilder().origin(suggestion)
        val origin = SuggestionStrUtils.formatAirportName(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        formattedOriginObservable.onNext(origin)
        requiredSearchParamsObserver.onNext(Unit)
    }

    protected fun setDestinationText(suggestion: SuggestionV4) {
        getParamsBuilder().destination(suggestion)
        val destination = SuggestionStrUtils.formatAirportName(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        formattedDestinationObservable.onNext(destination)
        requiredSearchParamsObserver.onNext(Unit)
    }

    @CallSuper
    protected open fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        setSelectedDate(dates)
        requiredSearchParamsObserver.onNext(Unit)
    }

    protected fun setSelectedDate(dates: Pair<LocalDate?, LocalDate?>) {
        selectedDates = dates

        getParamsBuilder().startDate(dates.first)
        getParamsBuilder().endDate(dates.second)
    }

    protected fun getToolTipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val instructionsText = getCalendarToolTipInstructions(start, end)
        if (start == null && end == null) {
            return Pair(context.resources.getString(R.string.select_dates_proper_case), instructionsText)
        } else if (end == null) {
            return Pair(LocaleBasedDateFormatUtils.localDateToMMMd(start!!), instructionsText)
        } else {
            val dateText = getStartDashEndDateString(start!!, end)
            return Pair(dateText, instructionsText)
        }
    }

    protected open fun getToolTipContentDescription(startDate: LocalDate?, endDate: LocalDate?, isRoundTripSearch: Boolean = true): String {
        if (startDate == null && endDate == null) {
            return context.resources.getString(R.string.select_dates_proper_case)
        } else if (endDate == null && isRoundTripSearch) {
            return Phrase.from(context, R.string.calendar_start_date_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate!!))
                    .put("instructiontext", getCalendarToolTipInstructions(startDate, endDate))
                    .format().toString()
        } else if (endDate == null && !isRoundTripSearch) {
            return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", LocaleBasedDateFormatUtils.localDateToMMMd(startDate!!))
                    .format().toString()
        } else {
            return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", getStartToEndDateString(startDate!!, endDate!!))
                    .format().toString()
        }
    }

    protected fun getCalendarCardDateText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): String {
        if (start == null && end == null) {
            return getEmptyDateText(isContentDescription)
        } else if (end == null) {
            return getNoEndDateText(start, isContentDescription)
        } else {
            return getCompleteDateText(start!!, end, isContentDescription)
        }
    }

    protected fun getDateAccessibilityText(datesLabel: String, durationDescription: String): String {
        return CalendarDateFormatter.getDateAccessibilityText(context, datesLabel, durationDescription)
    }

    protected fun getStartDashEndDateString(start: LocalDate, end: LocalDate): String {
        return CalendarDateFormatter.formatStartDashEnd(context, start, end)
    }

    protected fun getStartToEndDateString(start: LocalDate, end: LocalDate): String {
        return CalendarDateFormatter.formatStartToEnd(context, start, end)
    }

    protected fun getStartDashEndDateWithDayString(start: LocalDate, end: LocalDate): String {
        val startDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(start)
        val endDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(end)
        return Phrase.from(context, R.string.calendar_instructions_date_range_flight_extra_spacing_TEMPLATE)
                .put("startdate", startDate)
                .put("enddate", endDate)
                .format().toString()
    }

    protected fun getStartToEndDateWithDayString(start: LocalDate, end: LocalDate): String {
        val startDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(start)
        val endDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(end)
        // need to explicitly use "to" for screen readers
        return Phrase.from(context, R.string.start_to_end_date_range_cont_desc_TEMPLATE)
                .put("startdate", startDate)
                .put("enddate", endDate)
                .format().toString()
    }

    fun clearDestinationLocation() {
        getParamsBuilder().destination(null)
        formattedDestinationObservable.onNext("")
        requiredSearchParamsObserver.onNext(Unit)
    }
}
