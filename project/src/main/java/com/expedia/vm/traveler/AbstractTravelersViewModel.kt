package com.expedia.vm.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import rx.subjects.BehaviorSubject

abstract class AbstractTravelersViewModel() {

    val allTravelersCompleteSubject = BehaviorSubject.create<List<Traveler>>()
    val invalidTravelersSubject = BehaviorSubject.create<Unit>()
    val travelersCompletenessStatus = BehaviorSubject.create<TravelerCheckoutStatus>(TravelerCheckoutStatus.CLEAN)
    val refreshSelectedTravelerStatus = BehaviorSubject.create<Unit>()
    val isDirtyObservable = BehaviorSubject.create<Boolean>(false)

    abstract fun isValidForBooking(traveler: Traveler, index: Int) : Boolean
    abstract fun isTravelerEmpty(traveler: Traveler) : Boolean

    fun updateCompletionStatus() {
        if (allTravelersValid()){
            allTravelersCompleteSubject.onNext(getTravelers())
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.COMPLETE)
        } else if (Db.getTravelers().size > 1 && !isDirtyObservable.value) {
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

    open fun getTravelers() : List<Traveler> {
        return Db.getTravelers()
    }

    open fun getTraveler(index: Int) : Traveler {
        val travelerList = Db.getTravelers()
        return travelerList[index]
    }
}