package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.text.Html
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelSearchViewModel(val context: Context) {
    private val paramsBuilder = HotelSearchParams.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()
    val externalSearchParamsObservable = BehaviorSubject.create<Boolean>()
    val dateTextObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String,String>>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoOriginObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val enableDateObservable = PublishSubject.create<Boolean>()
    val enableTravelerObservable = PublishSubject.create<Boolean>()

    val enableDateObserver = endlessObserver<Unit> {
        enableDateObservable.onNext(paramsBuilder.hasOrigin())
    }

    val enableTravelerObserver = endlessObserver<Unit> {
        enableTravelerObservable.onNext(paramsBuilder.hasOrigin())
    }


    // Inputs
    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        val (start, end) = data

        paramsBuilder.checkIn(start)
        if (start != null && end == null) {
            paramsBuilder.checkOut(start.plusDays(1))
        } else {
            paramsBuilder.checkOut(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
    }

    var requiredSearchParamsObserver = endlessObserver<Unit>{
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
    }

    val travelersObserver = endlessObserver<HotelTravelerParams> { update ->
        paramsBuilder.adults(update.numberOfAdults)
        paramsBuilder.children(update.children)
    }

    val suggestionObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.suggestion(suggestion)
        locationTextObservable.onNext(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        paramsBuilder.suggestion(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            searchParamsObservable.onNext(paramsBuilder.build())
        } else {
            if (!paramsBuilder.hasOrigin()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    // Helpers
    private fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates)
        } else if (end == null) {
            return context.getResources().getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.getResources().getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.getResources().getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    private fun computeTopTextForToolTip(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return DateUtils.localDateToMMMd(start)
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.hotel_calendar_bottom_drag_to_modify
        val instructions = context.getResources().getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    init {
        val intent = (context as Activity).getIntent()
        externalSearchParamsObservable.onNext(!intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS))
    }
}

public data class HotelTravelerParams(val numberOfAdults: Int, val children: List<Int>)

public class HotelTravelerPickerViewModel(val context: Context) {
    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val travelerParamsObservable = BehaviorSubject.create(HotelTravelerParams(1, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<CharSequence>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()
    val adultPlusObservable = BehaviorSubject.create<Boolean>()
    val adultMinusObservable = BehaviorSubject.create<Boolean>()
    val childPlusObservable = BehaviorSubject.create<Boolean>()
    val childMinusObservable = BehaviorSubject.create<Boolean>()

    init {
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.children.size()
            guestsTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_guests, total, total)
            )

            adultTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_children, travelers.children.size(), travelers.children.size())
            )

            adultPlusObservable.onNext(total < MAX_GUESTS)
            childPlusObservable.onNext(total < MAX_GUESTS && travelers.children.size() < MAX_CHILDREN)
            adultMinusObservable.onNext(travelers.numberOfAdults > MIN_ADULTS)
            childMinusObservable.onNext(travelers.children.size() > MIN_CHILDREN)
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultPlusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults + 1, hotelTravelerParams.children))
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults - 1, hotelTravelerParams.children))
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.children.plus(10)))
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.children.subList(0, hotelTravelerParams.children.size() - 1)))
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age

        val hotelTravelerParams = travelerParamsObservable.getValue()
        travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, (0..hotelTravelerParams.children.size() - 1).map { childAges[it] }))
    }

}