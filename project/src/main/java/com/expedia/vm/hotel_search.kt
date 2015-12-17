package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
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
import java.util.ArrayList

class HotelSearchViewModel(val context: Context) {
    private val paramsBuilder = HotelSearchParams.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()
    val dateTextObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String,String>>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoOriginObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()


    // Inputs
    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        val (start, end) = data

        paramsBuilder.checkIn(start)
        paramsBuilder.checkOut(end)

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

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            searchParamsObservable.onNext(paramsBuilder.build())
        } else {
            if (!paramsBuilder.hasOrigin()) {
                errorNoOriginObservable.onNext(Unit)
            }
            else if (!paramsBuilder.hasStartAndEndDates()) {
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
                else R.string.calendar_tooltip_bottom_drag_to_modify
        val instructions = context.getResources().getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }
}

public data class HotelTravelerParams(val numberOfAdults: Int, val children: List<Int>)

public class HotelTravelerPickerViewModel(val context: Context) {
    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var numberOfAdults = 1
    private var numberOfChildren = 0
    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val travelerParamsObservable = BehaviorSubject.create(HotelTravelerParams(numberOfAdults, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<String>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()

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
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (allowed(adultChange = 1, childChange = 0)) {
            numberOfAdults++
            update()
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (allowed(adultChange = -1, childChange = 0)) {
            numberOfAdults--
            update()
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (allowed(adultChange = 0, childChange = 1)) {
            numberOfChildren++
            update()
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (allowed(adultChange = 0, childChange = -1)) {
            numberOfChildren--
            update()
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age
        update()
    }

    // Helpers
    private fun update() {
        val ages = ArrayList<Int>()
        for (i in 0..numberOfChildren - 1) {
            ages.add(childAges[i])
        }
        travelerParamsObservable.onNext(HotelTravelerParams(numberOfAdults, ages.toList()))
    }

    fun allowed(adultChange: Int, childChange: Int): Boolean {
        val adults = numberOfAdults + adultChange
        val childs = numberOfChildren + childChange

        if (adults < MIN_ADULTS) return false
        if (childs < MIN_CHILDREN) return false

        if (childs > MAX_CHILDREN) return false
        if (adults + childs > MAX_GUESTS) return false

        return true
    }
}