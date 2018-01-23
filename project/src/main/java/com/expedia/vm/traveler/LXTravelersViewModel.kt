package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import io.reactivex.subjects.BehaviorSubject

class LXTravelersViewModel(context: Context, lob: LineOfBusiness, showMainTravelerMinAgeMessaging: Boolean) : TravelersViewModel(context, lob, showMainTravelerMinAgeMessaging) {
    override fun createNewTravelerEntryWidgetModel(context: Context, index: Int, passportRequired: BehaviorSubject<Boolean>, currentStatus: TravelerCheckoutStatus): AbstractUniversalCKOTravelerEntryWidgetViewModel {
        return LXTravelerEntryWidgetViewModel(context, currentStatus)
    }

    val traveler = Traveler()

    override fun getTravelers(): List<Traveler> {
        return arrayListOf(traveler)
    }

    override fun requiresMultipleTravelers() = false
}
