package com.expedia.vm.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import io.reactivex.subjects.BehaviorSubject

abstract class AbstractTravelersViewModel {

    val allTravelersCompleteSubject = BehaviorSubject.create<List<Traveler>>()
    val invalidTravelersSubject = BehaviorSubject.create<Unit>()
    val travelersCompletenessStatus = BehaviorSubject.createDefault<TravelerCheckoutStatus>(TravelerCheckoutStatus.CLEAN)
    val refreshSelectedTravelerStatus = BehaviorSubject.create<Unit>()
    val isDirtyObservable = BehaviorSubject.createDefault<Boolean>(false)

    abstract fun isValidForBooking(traveler: Traveler, index: Int) : Boolean
    abstract fun isTravelerEmpty(traveler: Traveler) : Boolean


    fun updateCompletionStatus() {
        if (areTravelersEmpty()) {
            invalidTravelersSubject.onNext(Unit)
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)
        } else if (allTravelersValid()) {
            allTravelersCompleteSubject.onNext(getTravelers())
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.COMPLETE)
        } else if (Db.sharedInstance.travelers.size > 1 && !isDirtyObservable.value) {
            invalidTravelersSubject.onNext(Unit)
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)
        } else {
            invalidTravelersSubject.onNext(Unit)
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.DIRTY)
        }
    }

    open fun allTravelersValid(): Boolean {
        val travelerList = getTravelers()

        if (travelerList.isEmpty()) return false

        travelerList.forEachIndexed { index, traveler ->
            if (!isValidForBooking(traveler, index)) {
                return false
            }
        }
        return true
    }

    abstract fun getTravelers() : List<Traveler>

    abstract fun requiresMultipleTravelers() : Boolean

    open fun getTraveler(index: Int) : Traveler {
        val travelerList = Db.sharedInstance.travelers
        return travelerList[index]
    }

    open fun areTravelersEmpty() : Boolean {
        val travelerList = getTravelers()
        for (traveler in travelerList) {
            if (!isTravelerEmpty(traveler)) {
                return false
            }
        }
        return true
    }
}
