package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerPickerViewModel(val context: Context) {
    var showSeatingPreference = false
    var lob = LineOfBusiness.HOTELS
        set(value) {
            field = value
            val travelers = travelerParamsObservable.value
            makeTravelerText(travelers)
        }

    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val travelerParamsObservable = BehaviorSubject.create(TravelerParams(1, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<CharSequence>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()
    val adultPlusObservable = BehaviorSubject.create<Boolean>()
    val adultMinusObservable = BehaviorSubject.create<Boolean>()
    val childPlusObservable = BehaviorSubject.create<Boolean>()
    val childMinusObservable = BehaviorSubject.create<Boolean>()
    val infantPreferenceSeatingObservable = BehaviorSubject.create<Boolean>(false)
    val isInfantInLapObservable = BehaviorSubject.create<Boolean>(false)
    val tooManyInfants = PublishSubject.create<Boolean>()

    init {
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.childrenAges.size
            makeTravelerText(travelers)

            adultTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_children, travelers.childrenAges.size, travelers.childrenAges.size)
            )

            adultPlusObservable.onNext(total < MAX_GUESTS)
            childPlusObservable.onNext(total < MAX_GUESTS && travelers.childrenAges.size < MAX_CHILDREN)
            adultMinusObservable.onNext(travelers.numberOfAdults > MIN_ADULTS)
            childMinusObservable.onNext(travelers.childrenAges.size > MIN_CHILDREN)
            validateInfants()
        }
        isInfantInLapObservable.subscribe { inLap ->
            validateInfants()
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultPlusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults + 1, hotelTravelerParams.childrenAges))
            trackTravelerPickerClick("Add.Adult")
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults - 1, hotelTravelerParams.childrenAges))
            trackTravelerPickerClick("Remove.Adult")
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.childrenAges.plus(childAges[hotelTravelerParams.childrenAges.size])))
            trackTravelerPickerClick("Add.Child")
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.childrenAges.subList(0, hotelTravelerParams.childrenAges.size - 1)))
            trackTravelerPickerClick("Remove.Child")
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age
        val hotelTravelerParams = travelerParamsObservable.value
        val children = hotelTravelerParams.childrenAges.toIntArray()
        if (children.size > which) {
            children[which] = childAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, children.toList()))
    }

    private fun validateInfants() {
        val hotelTravelerParams = travelerParamsObservable.value
        val numberOfInfants = hotelTravelerParams.childrenAges.count { childAge -> childAge < 2 }
        val numChildrenOver12 = hotelTravelerParams.childrenAges.count { childAge -> childAge >= 12 }
        infantPreferenceSeatingObservable.onNext(numberOfInfants > 0)
        tooManyInfants.onNext(isInfantInLapObservable.value && (numberOfInfants > (hotelTravelerParams.numberOfAdults + numChildrenOver12)))
    }

    fun makeTravelerText(travelers: TravelerParams) {
        val total = travelers.numberOfAdults + travelers.childrenAges.size
        guestsTextObservable.onNext(
                if (lob == LineOfBusiness.PACKAGES || lob == LineOfBusiness.FLIGHTS_V2) {
                    StrUtils.formatTravelerString(context, total)
                } else {
                    StrUtils.formatGuestString(context, total)
                }
        )
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        when (lob) {
            LineOfBusiness.PACKAGES -> {
                PackagesTracking().trackSearchTravelerPickerChooserClick(actionLabel)
            }

            LineOfBusiness.HOTELS -> {
                HotelTracking().trackTravelerPickerClick(actionLabel)
            }

            LineOfBusiness.FLIGHTS_V2 -> {
                FlightsV2Tracking.trackTravelerPickerClick(actionLabel)
            }

            else -> { // required to satisfy kotlin codestyle check
                // do nothing
            }
        }
    }
}
