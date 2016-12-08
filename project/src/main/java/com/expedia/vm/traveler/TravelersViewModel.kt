package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import rx.subjects.BehaviorSubject
import javax.inject.Inject

open class TravelersViewModel(val context: Context, val lob: LineOfBusiness,
                              showMainTravelerMinAgeMessaging: Boolean) : AbstractTravelersViewModel() {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val emptyTravelersSubject = BehaviorSubject.create<Unit>()
    val passportRequired = BehaviorSubject.create<Boolean>(false)
    val showMainTravelerMinAgeMessaging = BehaviorSubject.create<Boolean>(false)

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        this.showMainTravelerMinAgeMessaging.onNext(showMainTravelerMinAgeMessaging)
    }

    override fun isValidForBooking(traveler: Traveler, index: Int): Boolean {
        return travelerValidator.isValidForFlightBooking(traveler, index, passportRequired.value, User.isLoggedIn(context))
    }

    override fun isTravelerEmpty(traveler: Traveler): Boolean {
        return travelerValidator.isTravelerEmpty(traveler)
    }

    fun refresh() {
        if (areTravelersEmpty()) {
            emptyTravelersSubject.onNext(Unit)
            travelersCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)
        } else {
            updateCompletionStatus()
        }
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
