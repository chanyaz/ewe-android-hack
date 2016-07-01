package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseSearchViewModel(val context: Context) {

    // Outputs
    val dateAccessibilityObservable = BehaviorSubject.create<CharSequence>()
    val dateTextObservable = BehaviorSubject.create<CharSequence>()
    val dateInstructionObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
    val datesObservable = BehaviorSubject.create<Pair<LocalDate?, LocalDate?>>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoDestinationObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val errorMaxDurationObservable = PublishSubject.create<String>()
    val errorMaxRangeObservable = PublishSubject.create<String>()
    val enableDateObservable = PublishSubject.create<Boolean>()
    val enableTravelerObservable = PublishSubject.create<Boolean>()
    val travelersObserver = BehaviorSubject.create<TravelerParams>()
    val errorOriginSameAsDestinationObservable = PublishSubject.create<String>()

    val formattedOriginObservable = BehaviorSubject.create<String>()
    val formattedDestinationObservable = PublishSubject.create<String>()
    val destinationValidObservable = BehaviorSubject.create<Boolean>(false)
    val originValidObservable = BehaviorSubject.create<Boolean>(false)

    init {
        travelersObserver.subscribe { update ->
            getParamsBuilder().adults(update.numberOfAdults)
            getParamsBuilder().children(update.childrenAges)
        }
    }

    abstract fun getParamsBuilder(): BaseSearchParams.Builder
    abstract fun getMaxSearchDurationDays(): Int
    abstract fun getMaxDateRange(): Int
    abstract fun sameStartAndEndDateAllowed():Boolean


    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        onDatesChanged(data)
    }

    val enableDateObserver = endlessObserver<Unit> {
        enableDateObservable.onNext(getParamsBuilder().hasOriginAndDestination())
    }

    val enableTravelerObserver = endlessObserver<Unit> {
        enableTravelerObservable.onNext(getParamsBuilder().hasDestinationLocation())
    }

    open var requiredSearchParamsObserver = endlessObserver<Unit> { // open so HotelSearchViewModel can override it
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
        destinationValidObservable.onNext(getParamsBuilder().hasDestinationLocation())
        originValidObservable.onNext(getParamsBuilder().hasOriginAndDestination())
    }

    val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().origin(suggestion)
        formattedOriginObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    open val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        formattedDestinationObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    fun startDate(): LocalDate? {
        return datesObservable?.value?.first
    }

    fun endDate(): LocalDate? {
        return datesObservable?.value?.second
    }

    fun resetDates() {
        onDatesChanged(Pair(null, null))
    }

    open fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        datesObservable.onNext(dates)

        getParamsBuilder().startDate(start)
        getParamsBuilder().endDate(end)
        if (!isStartDateOnlyAllowed()) {
            if (start != null && end == null) {
                getParamsBuilder().endDate(start.plusDays(1))
            }
        }

        dateTextObservable.onNext(computeDateText(start, end))
        dateAccessibilityObservable.onNext(computeDateText(start, end, true))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
    }

    abstract fun isStartDateOnlyAllowed(): Boolean

    protected fun computeTopTextForToolTip(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return DateUtils.localDateToMMMd(start)
        } else {
            return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
        }
    }

    open fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_checkin_date);
        }

        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString))
        }
        return sb.build()
    }

    open fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        return computeDateText(start, end, false)
    }

    open fun computeDateText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): CharSequence {
        val dateRangeText = if (isContentDescription) computeDateRangeText(start, end, true) else computeDateRangeText(start, end)
        val sb = SpannableBuilder()

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)

            if (isContentDescription) {
                sb.append(Phrase.from(context, R.string.trip_search_date_range_cont_desc_TEMPLATE)
                        .put("date_range", dateRangeText)
                        .put("nights", context.resources.getString(R.string.nights_count_TEMPLATE, nightsString))
                        .format().toString())
            } else {
                sb.append(dateRangeText)
                sb.append(" ")
                sb.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
            }
        } else {
            sb.append(dateRangeText)
        }
        return sb.build()
    }

    open fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        return computeDateRangeText(start, end, false)
    }

    open fun computeDateRangeText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): String? {
        if (start == null && end == null) {
            var stringID = if (isContentDescription) R.string.packages_search_dates_cont_desc else R.string.select_dates
            return context.resources.getString(stringID)
        } else if (end == null) {
            return context.resources.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            var stringID = if (isContentDescription) R.string.packages_search_date_range_cont_desc_TEMPLATE else R.string.calendar_instructions_date_range_TEMPLATE
            return Phrase.from(context, stringID).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
        }
    }

    open fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    protected fun updateDbTravelers(params: BaseSearchParams) {
        // This is required for the checkout screen to correctly populate traveler entry screen.
        val travelerList = Db.getTravelers()
        if (travelerList.isNotEmpty()) {
            travelerList.clear()
        }
        for (i in 1..params.adults) {
            val traveler = Traveler()
            traveler.setPassengerCategory(PassengerCategory.ADULT)
            traveler.gender = Traveler.Gender.GENDER
            traveler.searchedAge = -1
            travelerList.add(traveler)
        }
        for (child in params.children) {
            val traveler = Traveler()
            traveler.gender = Traveler.Gender.GENDER
            traveler.setPassengerCategory(getPassengerCategory(child, params))
            traveler.searchedAge = child
            travelerList.add(traveler)
        }
        Db.setTravelers(travelerList)
    }

    protected open fun getPassengerCategory(childAge: Int, params: BaseSearchParams): PassengerCategory {
        if (childAge < 12) {
            return PassengerCategory.CHILD
        } else if (childAge < 18) {
            return PassengerCategory.ADULT_CHILD
        }

        throw IllegalArgumentException("$childAge is not a valid child age")
    }
}
