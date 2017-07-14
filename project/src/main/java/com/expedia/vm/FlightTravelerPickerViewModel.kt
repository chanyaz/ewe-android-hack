package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.Observable

class FlightTravelerPickerViewModel(context: Context) : BaseTravelerPickerViewModel(context) {

    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_YOUTHS = 0
    private val MIN_CHILDREN = 0
    private val MIN_INFANTS = 0

    private val MAX_YOUTHS = 4
    private val MAX_CHILDREN = 4
    private val MAX_INFANT = 4

    private val DEFAULT_YOUTH_AGE = 16
    private val DEFAULT_CHILD_AGE = 10
    private val DEFAULT_INFANT_AGE = 1

    private val infantAgeUpperLimit = 2
    private val childAgeUpperLimit = 12
    private val YouthAgeUpperLimit = 17


    val youthTravelerCountChangeObservable = BehaviorSubject.create<Unit>()
    val infantTravelerCountChangeObservable = BehaviorSubject.create<Unit>()

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

    // Outputs
    val youthTextObservable = BehaviorSubject.create<String>()
    val infantTextObservable = BehaviorSubject.create<String>()

    val youthPlusObservable = BehaviorSubject.create<Boolean>()
    val youthMinusObservable = BehaviorSubject.create<Boolean>()
    val infantPlusObservable = BehaviorSubject.create<Boolean>()
    val infantMinusObservable = BehaviorSubject.create<Boolean>()

    init {
        lob = LineOfBusiness.FLIGHTS_V2


        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.childrenAges.size
            makeTravelerText(travelers)

            val infant = travelers.childrenAges.count { infantAge -> infantAge < infantAgeUpperLimit }
            val child = travelers.childrenAges.count { childAge -> childAge in infantAgeUpperLimit..childAgeUpperLimit }
            val youth = travelers.childrenAges.count { youthAge -> youthAge in (childAgeUpperLimit+1)..YouthAgeUpperLimit }

            adultTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_children, child, child)
            )

            youthTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_youth, youth, youth)
            )

            infantTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_infant, infant, infant)
            )
            adultPlusObservable.onNext(total < MAX_GUESTS)
            youthPlusObservable.onNext(total < MAX_GUESTS && youth < MAX_YOUTHS)
            childPlusObservable.onNext(total < MAX_GUESTS && child < MAX_CHILDREN)
            infantPlusObservable.onNext(total < MAX_GUESTS && infant < MAX_INFANT)
            validateInfants()

            adultMinusObservable.onNext(travelers.numberOfAdults > MIN_ADULTS)
            youthMinusObservable.onNext(youth > MIN_YOUTHS)
            childMinusObservable.onNext(child > MIN_CHILDREN)
            infantMinusObservable.onNext(infant > MIN_INFANTS)

        }
        isInfantInLapObservable.subscribe { inLap ->
            validateInfants()
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults + 1, flightTravelerParams.childrenAges, emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Adult")
        adultTravelerCountChangeObservable.onNext(Unit)

    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults - 1, flightTravelerParams.childrenAges, emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Adult")

        adultTravelerCountChangeObservable.onNext(Unit)

    }

    val incrementYouthObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.plusElement(DEFAULT_YOUTH_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Youth")
        youthTravelerCountChangeObservable.onNext(Unit)

    }

    val decrementYouthObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.minusElement(DEFAULT_YOUTH_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Youth")

        youthTravelerCountChangeObservable.onNext(Unit)

    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.plusElement(DEFAULT_CHILD_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Child")

        childTravelerCountChangeObservable.onNext(Unit)

    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.minusElement(DEFAULT_CHILD_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Child")

        childTravelerCountChangeObservable.onNext(Unit)

    }

    val incrementInfantObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            val infant = flightTravelerParams.childrenAges.count { infantAge -> infantAge < infantAgeUpperLimit }
            if (showSeatingPreference && infant == 0) {
                infantInSeatObservable.onNext(false)
            }
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.plusElement(DEFAULT_INFANT_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Add.Infant")

        infantTravelerCountChangeObservable.onNext(Unit)

    }

    val decrementInfantObserver: Observer<Unit> = endlessObserver {

            val flightTravelerParams = travelerParamsObservable.value
            val infant = flightTravelerParams.childrenAges.count { infantAge -> infantAge < infantAgeUpperLimit }
            if (showSeatingPreference && infant == 1) {
                infantInSeatObservable.onNext(true)
            }
            travelerParamsObservable.onNext(TravelerParams(flightTravelerParams.numberOfAdults, flightTravelerParams.childrenAges.minusElement(DEFAULT_INFANT_AGE), emptyList(), emptyList()))
            trackTravelerPickerClick("Remove.Infant")

        infantTravelerCountChangeObservable.onNext(Unit)

    }

    private fun validateInfants() {
        val travelerParams = travelerParamsObservable.value
        val numberOfInfants = travelerParams.childrenAges.count { childAge -> childAge < infantAgeUpperLimit }
        val numberOfChild = travelerParams.childrenAges.count { childAge -> childAge in infantAgeUpperLimit..childAgeUpperLimit }
        val numberOfYouth = travelerParams.childrenAges.count { youthAge -> youthAge in (childAgeUpperLimit+1)..youthAge }
        infantPreferenceSeatingObservable.onNext(numberOfInfants > 0)
        tooManyInfantsInLap.onNext(showSeatingPreference && isInfantInLapObservable.value && (numberOfInfants > (travelerParams.numberOfAdults + numberOfChild + numberOfYouth)))
        tooManyInfantsInSeat.onNext(showSeatingPreference && !isInfantInLapObservable.value && (numberOfInfants > 2 && (travelerParams.numberOfAdults + numberOfChild + numberOfYouth) == 1))
    }

}
