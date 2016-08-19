package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import rx.subjects.BehaviorSubject
import javax.inject.Inject

open class CheckoutTravelerViewModel(context: Context, val lob: LineOfBusiness, showMainTravelerMinAgeMessaging: Boolean) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val allTravelersCompleteSubject = BehaviorSubject.create<List<Traveler>>()
    val invalidTravelersSubject = BehaviorSubject.create<Unit>()
    val emptyTravelersSubject = BehaviorSubject.create<Unit>()
    val travelerCompletenessStatus = BehaviorSubject.create<TravelerCheckoutStatus>(TravelerCheckoutStatus.CLEAN)
    val passportRequired = BehaviorSubject.create<Boolean>(false)
    val showMainTravelerMinAgeMessaging = BehaviorSubject.create<Boolean>(false)

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        this.showMainTravelerMinAgeMessaging.onNext(showMainTravelerMinAgeMessaging)
    }

    fun refresh() {
        if (areTravelersEmpty()) {
            emptyTravelersSubject.onNext(Unit)
            travelerCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)
        } else {
            updateCompletionStatus()
        }
    }

    fun updateCompletionStatus() {
        if (validateTravelersComplete()){
            allTravelersCompleteSubject.onNext(getTravelers())
            travelerCompletenessStatus.onNext(TravelerCheckoutStatus.COMPLETE)
        } else {
            invalidTravelersSubject.onNext(Unit)
            travelerCompletenessStatus.onNext(TravelerCheckoutStatus.DIRTY)
        }
    }

    open fun validateTravelersComplete(): Boolean {
        val travelerList = getTravelers()

        if (travelerList.isEmpty()) return false

        travelerList.forEachIndexed { index, traveler ->
            if (!travelerValidator.isValidForBooking(traveler, index, passportRequired.value)) {
                return false
            }
        }
        return true
    }

    open fun areTravelersEmpty() : Boolean {
        val travelerList = getTravelers()
        for (traveler in travelerList) {
            if (!travelerValidator.isTravelerEmpty(traveler)) {
                return false
            }
        }
        return true
    }

    open fun getTravelers() : List<Traveler> {
        return Db.getTravelers();
    }

    open fun getTraveler(index: Int) : Traveler {
        val travelerList = Db.getTravelers()
        return travelerList[index]
    }
}
