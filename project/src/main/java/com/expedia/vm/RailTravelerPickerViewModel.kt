package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

class RailTravelerPickerViewModel(context: Context) : BaseTravelerPickerViewModel(context){

    val MAX_RAIL_TRAVELER = 8
    val MIN_RAIL_TRAVELER = 0
    val MIN_ADULTS = 0
    val MIN_CHILDREN = 0
    val MAX_CHILDREN = 7
    val MIN_YOUTH = 0
    val MAX_YOUTH = 8
    val MIN_SENIOR = 0
    val MAX_SENIOR = 8
    private val DEFAULT_CHILD_AGE = 10
    private val DEFAULT_YOUTH_AGE = 16
    private val DEFAULT_SENIOR_AGE = 60

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)
    private var youthAges = arrayListOf(DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE, DEFAULT_YOUTH_AGE)
    private var seniorAges = arrayListOf(DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE, DEFAULT_SENIOR_AGE)

    // Outputs
    val youthTextObservable = BehaviorSubject.create<String>()
    val seniorTextObservable = BehaviorSubject.create<String>()
    val youthPlusObservable = BehaviorSubject.create<Boolean>()
    val youthMinusObservable = BehaviorSubject.create<Boolean>()
    val seniorPlusObservable = BehaviorSubject.create<Boolean>()
    val seniorMinusObservable = BehaviorSubject.create<Boolean>()

    init {
        lob = LineOfBusiness.RAILS
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.childrenAges.size + travelers.youthAges.size + travelers.seniorAges.size
            makeTravelerText(travelers)

            adultTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_children, travelers.childrenAges.size, travelers.childrenAges.size)
            )

            youthTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_youth, travelers.youthAges.size, travelers.youthAges.size)
            )

            seniorTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_senior, travelers.seniorAges.size, travelers.seniorAges.size)
            )

            val totalNumberOfTravelersExcludeChildren = travelers.numberOfAdults + travelers.youthAges.size + travelers.seniorAges.size
            val isTravelerChildOnly = totalNumberOfTravelersExcludeChildren == MIN_RAIL_TRAVELER
            val isNonChildTravelerGreaterThanOne = totalNumberOfTravelersExcludeChildren > 1

            adultPlusObservable.onNext(total < MAX_RAIL_TRAVELER)
            adultMinusObservable.onNext(isNonChildTravelerGreaterThanOne && travelers.numberOfAdults > MIN_ADULTS)
            childPlusObservable.onNext(total < MAX_RAIL_TRAVELER && travelers.childrenAges.size < MAX_CHILDREN && !isTravelerChildOnly)
            childMinusObservable.onNext(travelers.childrenAges.size > MIN_CHILDREN)
            youthPlusObservable.onNext(total < MAX_RAIL_TRAVELER && travelers.youthAges.size < MAX_YOUTH)
            youthMinusObservable.onNext(isNonChildTravelerGreaterThanOne && travelers.youthAges.size > MIN_YOUTH)
            seniorPlusObservable.onNext(total < MAX_RAIL_TRAVELER && travelers.seniorAges.size < MAX_SENIOR)
            seniorMinusObservable.onNext(isNonChildTravelerGreaterThanOne && travelers.seniorAges.size > MIN_SENIOR)
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultPlusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults + 1,
                    railTravelerParams.childrenAges, railTravelerParams.youthAges, railTravelerParams.seniorAges))
            trackTravelerPickerClick("Add.Adult")
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults - 1,
                    railTravelerParams.childrenAges, railTravelerParams.youthAges, railTravelerParams.seniorAges))
            trackTravelerPickerClick("Remove.Adult")
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges.plus(childAges[railTravelerParams.childrenAges.size]),
                    railTravelerParams.youthAges,
                    railTravelerParams.seniorAges))
            trackTravelerPickerClick("Add.Child")
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges.subList(0, railTravelerParams.childrenAges.size - 1),
                    railTravelerParams.youthAges,
                    railTravelerParams.seniorAges))
            trackTravelerPickerClick("Remove.Child")
        }
    }

    val incrementYouthObserver: Observer<Unit> = endlessObserver {
        if (youthPlusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges,
                    railTravelerParams.youthAges.plus(youthAges[railTravelerParams.youthAges.size]),
                    railTravelerParams.seniorAges))
            trackTravelerPickerClick("Add.Youth")
        }
    }

    val decrementYouthObserver: Observer<Unit> = endlessObserver {
        if (youthMinusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges,
                    railTravelerParams.youthAges.subList(0, railTravelerParams.youthAges.size - 1),
                    railTravelerParams.seniorAges))
            trackTravelerPickerClick("Remove.Youth")
        }
    }
    val incrementSeniorObserver: Observer<Unit> = endlessObserver {
        if (seniorPlusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges,
                    railTravelerParams.youthAges,
                    railTravelerParams.seniorAges.plus(seniorAges[railTravelerParams.seniorAges.size])))
            trackTravelerPickerClick("Add.Senior")
        }
    }

    val decrementSeniorObserver: Observer<Unit> = endlessObserver {
        if (seniorMinusObservable.value) {
            val railTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults,
                    railTravelerParams.childrenAges,
                    railTravelerParams.youthAges,
                    railTravelerParams.seniorAges.subList(0, railTravelerParams.seniorAges.size - 1)))
            trackTravelerPickerClick("Remove.Senior")
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age
        val railTravelerParams = travelerParamsObservable.value
        val children = railTravelerParams.childrenAges.toIntArray()
        if (children.size > which) {
            children[which] = childAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults, children.toList(), railTravelerParams.youthAges, railTravelerParams.seniorAges))
    }

    val youthAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        youthAges[which] = age
        val railTravelerParams = travelerParamsObservable.value
        val youth = railTravelerParams.youthAges.toIntArray()
        if (youth.size > which) {
            youth[which] = youthAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults, railTravelerParams.childrenAges, youth.toList(), railTravelerParams.seniorAges))
    }

    val seniorAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        seniorAges[which] = age
        val railTravelerParams = travelerParamsObservable.value
        val senior = railTravelerParams.seniorAges.toIntArray()
        if (senior.size > which) {
            senior[which] = seniorAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(railTravelerParams.numberOfAdults, railTravelerParams.childrenAges, railTravelerParams.youthAges, senior.toList()))
    }

    override fun makeTravelerText(travelers: TravelerParams) {
        val total = travelers.numberOfAdults + travelers.childrenAges.size + travelers.youthAges.size + travelers.seniorAges.size
        guestsTextObservable.onNext(
            StrUtils.formatTravelerString(context, total)
        )
    }
}
