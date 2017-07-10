package com.expedia.vm

import android.content.Context
import android.support.annotation.CallSuper
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseSearchViewModel(val context: Context) {
    // Outputs
    val dateAccessibilityObservable = BehaviorSubject.create<CharSequence>()
    var dateTextObservable = BehaviorSubject.create<CharSequence>()
    val dateInstructionObservable = PublishSubject.create<CharSequence>()
    val dateSetObservable = PublishSubject.create<Unit>()
    val abortTimerObservable = PublishSubject.create<Unit>()
    val calendarTooltipContDescObservable = PublishSubject.create<String>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
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

    val formattedOriginObservable = PublishSubject.create<String>()
    val formattedDestinationObservable = PublishSubject.create<String>()

    var a11yFocusSelectDatesObservable = BehaviorSubject.create<Unit>()

    protected var selectedDates: Pair<LocalDate?, LocalDate?> = Pair(null, null)

    init {
        updateTraveler()
    }

    abstract fun getParamsBuilder(): BaseSearchParams.Builder
    abstract fun getMaxSearchDurationDays(): Int
    abstract fun getMaxDateRange(): Int
    abstract fun sameStartAndEndDateAllowed(): Boolean
    abstract fun isStartDateOnlyAllowed(): Boolean
    abstract fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence

    protected abstract fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?) : String
    protected abstract fun getEmptyDateText(forContentDescription: Boolean) : String
    protected abstract fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean) : String
    protected abstract fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean) : String


    open val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().origin(suggestion)
        val origin = SuggestionStrUtils.formatAirportName(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        formattedOriginObservable.onNext(origin)
        requiredSearchParamsObserver.onNext(Unit)
    }

    open val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        val destination = SuggestionStrUtils.formatAirportName(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        formattedDestinationObservable.onNext(destination)
        requiredSearchParamsObserver.onNext(Unit)
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

    open fun getFirstAvailableDate(): LocalDate {
        return LocalDate.now()
    }

    open fun isTalkbackActive(): Boolean {
        return AccessibilityUtil.isTalkBackEnabled(context)
    }

    open protected var requiredSearchParamsObserver = endlessObserver<Unit> { // open so HotelSearchViewModel can override it
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
    }

    open protected fun updateTraveler() {
        travelersObservable.subscribe { update ->
            getParamsBuilder().adults(update.numberOfAdults)
            getParamsBuilder().children(update.childrenAges)
        }
    }

    @CallSuper
    open protected fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        selectedDates = dates

        getParamsBuilder().startDate(start)
        getParamsBuilder().endDate(end)
        requiredSearchParamsObserver.onNext(Unit)
    }

    protected fun getToolTipText(start: LocalDate?, end: LocalDate?) : Pair<String, String> {
        val instructionsText = getCalendarToolTipInstructions(start, end)
        if (start == null && end == null) {
            return Pair(context.resources.getString(R.string.select_dates_proper_case), instructionsText)
        } else if (end == null) {
            return Pair(DateUtils.localDateToMMMd(start), instructionsText)
        } else {
            val dateText = getStartDashEndDateString(start!!, end)
            return Pair(dateText, instructionsText)
        }
    }

    open protected fun getToolTipContentDescription(startDate: LocalDate?, endDate: LocalDate?, isRoundTripSearch: Boolean = true): String {
        if (startDate == null && endDate == null) {
            return context.resources.getString(R.string.select_dates_proper_case)
        } else if (endDate == null && isRoundTripSearch) {
            return Phrase.from(context, R.string.calendar_start_date_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", DateUtils.localDateToMMMd(startDate))
                    .put("instructiontext", getCalendarToolTipInstructions(startDate, endDate))
                    .format().toString()
        } else if (endDate == null && !isRoundTripSearch) {
            return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", DateUtils.localDateToMMMd(startDate))
                    .format().toString()
        } else {
            return Phrase.from(context, R.string.calendar_complete_tooltip_cont_desc_TEMPLATE)
                    .put("selecteddate", getStartToEndDateString(startDate!!, endDate!!))
                    .format().toString()
        }
    }

    protected fun getCalendarCardDateText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean) : String {
        if (start == null && end == null) {
            return getEmptyDateText(isContentDescription)
        } else if (end == null) {
            return getNoEndDateText(start, isContentDescription)
        } else {
            return getCompleteDateText(start!!, end, isContentDescription)
        }
    }

    protected fun getDateAccessibilityText(datesLabel: String, durationDescription: String) : String {
        return Phrase.from(context, R.string.search_dates_cont_desc_TEMPLATE)
                .put("dates_label", datesLabel)
                .put("duration_description", durationDescription).format().toString()
    }

    protected fun getStartDashEndDateString(start: LocalDate, end: LocalDate) : String {
        return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(start))
                .put("enddate", DateUtils.localDateToMMMd(end))
                .format().toString()
    }

    protected fun getStartToEndDateString(start: LocalDate, end: LocalDate) : String {
        // need to explicitly use "to" for screen readers
        return Phrase.from(context, R.string.search_date_range_cont_desc_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(start))
                .put("enddate", DateUtils.localDateToMMMd(end))
                .format().toString()
    }
}
