package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

abstract class TravelersViewModel(
    val context: Context,
    val lob: LineOfBusiness,
    showMainTravelerMinAgeMessaging: Boolean
) : AbstractTravelersViewModel() {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val emptyTravelersSubject = BehaviorSubject.create<Unit>()
    val passportRequired = BehaviorSubject.createDefault<Boolean>(false)
    val showMainTravelerMinAgeMessaging = BehaviorSubject.createDefault<Boolean>(false)
    val doneClickedMethod = PublishSubject.create<() -> Unit>()

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        this.showMainTravelerMinAgeMessaging.onNext(showMainTravelerMinAgeMessaging)
    }

    override fun isValidForBooking(traveler: Traveler, index: Int): Boolean {
        return travelerValidator.isValidForFlightBooking(traveler, index, passportRequired.value)
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

    abstract fun createNewTravelerEntryWidgetModel(context: Context, index: Int, passportRequired: BehaviorSubject<Boolean>, currentStatus: TravelerCheckoutStatus): AbstractUniversalCKOTravelerEntryWidgetViewModel
}
