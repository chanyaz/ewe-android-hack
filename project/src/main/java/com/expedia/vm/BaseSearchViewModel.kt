package com.expedia.vm

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

abstract class BaseSearchViewModel(val context: Context) {
    open val paramsBuilder: BaseSearchParams.Builder by Delegates.notNull()

    // Outputs
    val dateTextObservable = BehaviorSubject.create<CharSequence>()
    val dateInstructionObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
    val datesObservable = BehaviorSubject.create<Pair<LocalDate?, LocalDate?>>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoDestinationObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val errorMaxDatesObservable = PublishSubject.create<String>()
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
            paramsBuilder.adults(update.numberOfAdults)
            paramsBuilder.children(update.childrenAges)
        }
    }

    abstract fun getMaxSearchDurationDays(): Int;

    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        onDatesChanged(data)
    }

    val enableDateObserver = endlessObserver<Unit> {
        enableDateObservable.onNext(paramsBuilder.hasOriginAndDestination())
    }

    val enableTravelerObserver = endlessObserver<Unit> {
        enableTravelerObservable.onNext(paramsBuilder.hasOriginLocation())
    }

    open var requiredSearchParamsObserver = endlessObserver<Unit> { // open so HotelSearchViewModel can override it
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        destinationValidObservable.onNext(paramsBuilder.hasDestinationLocation())
        originValidObservable.onNext(paramsBuilder.hasOriginAndDestination())
    }

    val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.origin(suggestion)
        formattedOriginObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    open val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.destination(suggestion)
        formattedDestinationObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    fun startDate(): LocalDate? {
        return datesObservable?.value?.first
    }

    fun endDate(): LocalDate? {
        return datesObservable?.value?.second
    }

    open fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        datesObservable.onNext(dates)

        paramsBuilder.startDate(start)
        paramsBuilder.endDate(end)
        if (!isStartDateOnlyAllowed()) {
            if (start != null && end == null) {
                paramsBuilder.endDate(start.plusDays(1))
            }
        }

        dateTextObservable.onNext(computeDateText(start, end))
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
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
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
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    open fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates)
        } else if (end == null) {
            return context.resources.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    open fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }
}

