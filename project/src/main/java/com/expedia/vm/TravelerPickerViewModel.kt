package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.TravelerParams
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.Observable

class TravelerPickerViewModel(context: Context) : BaseTravelerPickerViewModel(context) {


    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val infantPreferenceSeatingObservable = BehaviorSubject.create<Boolean>(false)
    val tooManyInfantsInLap = PublishSubject.create<Boolean>()
    val tooManyInfantsInSeat = PublishSubject.create<Boolean>()
    val showInfantErrorMessage = Observable.zip(tooManyInfantsInLap, tooManyInfantsInSeat, { inLap, inSeat ->
        when {
            inLap -> context.getString(R.string.max_one_infant_per_lap)
            inSeat -> context.getString(R.string.max_two_infants_seated_per_adult)
            else -> ""
        }
    })

    init {
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.childrenAges.size
            makeTravelerText(travelers)

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
            val travelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(travelerParams.numberOfAdults + 1, travelerParams.childrenAges, emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Adult")
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.value) {
            val travelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(travelerParams.numberOfAdults - 1, travelerParams.childrenAges, emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Adult")
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.value) {
            val travelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(travelerParams.numberOfAdults, travelerParams.childrenAges.plus(childAges[travelerParams.childrenAges.size]), emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Child")
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.value) {
            val travelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(travelerParams.numberOfAdults, travelerParams.childrenAges.subList(0, travelerParams.childrenAges.size - 1), emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Child")
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age
        val travelerParams = travelerParamsObservable.value
        val children = travelerParams.childrenAges.toIntArray()
        if (children.size > which) {
            children[which] = childAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(travelerParams.numberOfAdults, children.toList(), emptyList(), emptyList()))
    }

    private fun validateInfants() {
        val travelerParams = travelerParamsObservable.value
        val numberOfInfants = travelerParams.childrenAges.count { childAge -> childAge < 2 }
        val numChildrenOver12 = travelerParams.childrenAges.count { childAge -> childAge >= 12 }
        infantPreferenceSeatingObservable.onNext(numberOfInfants > 0)
        tooManyInfantsInLap.onNext(isInfantInLapObservable.value && (numberOfInfants > (travelerParams.numberOfAdults + numChildrenOver12)))
        tooManyInfantsInSeat.onNext(showSeatingPreference && !isInfantInLapObservable.value && (numberOfInfants > 2 && (travelerParams.numberOfAdults + numChildrenOver12) == 1))
    }
}
