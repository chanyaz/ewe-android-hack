package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import rx.subjects.BehaviorSubject

class HotelTravelersViewModel(context: Context, lob: LineOfBusiness, showMainTravelerMinAgeMessaging: Boolean) : TravelersViewModel(context, lob, showMainTravelerMinAgeMessaging) {
    override fun createNewTravelerEntryWidgetModel(context: Context, index: Int, passportRequired: BehaviorSubject<Boolean>, currentStatus: TravelerCheckoutStatus): AbstractUniversalCKOTravelerEntryWidgetViewModel {
        return HotelTravelerEntryWidgetViewModel(context, currentStatus)
    }

    override fun getTravelers() : List<Traveler> {
        return Db.getTravelers()
    }

    override fun requiresMultipleTravelers() = false

    override fun isValidForBooking(traveler: Traveler, index: Int): Boolean {
//        TODO: implement traveler validation on hotels see s/6776
        return true
    }
}