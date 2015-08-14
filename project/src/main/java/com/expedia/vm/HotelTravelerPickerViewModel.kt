package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import java.util.ArrayList

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
    val updateObservable = BehaviorSubject.create(HotelTravelerParams(numberOfAdults, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<String>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()

    init {
        updateObservable.subscribe { update ->
            guestsTextObservable.onNext(
                    StrUtils.formatGuests(context, update.numberOfAdults, update.children.size())
            )

            adultTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_adults, update.numberOfAdults, update.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_children, update.children.size(), update.children.size())
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
            numberOfChildren ++
            update()
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (allowed(adultChange = 0, childChange = -1)) {
            numberOfChildren --
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
        for (i in 0..numberOfChildren-1) {
            ages.add(childAges[i])
        }
        updateObservable.onNext(HotelTravelerParams(numberOfAdults, ages.toList()))
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
