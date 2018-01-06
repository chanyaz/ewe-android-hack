package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.enums.TravelerCheckoutStatus
import rx.subjects.BehaviorSubject

class HotelTravelersViewModel(context: Context, lob: LineOfBusiness, showMainTravelerMinAgeMessaging: Boolean) : TravelersViewModel(context, lob, showMainTravelerMinAgeMessaging) {
    val createTripOptInStatus = BehaviorSubject.create<MerchandiseSpam>()

    override fun createNewTravelerEntryWidgetModel(context: Context, index: Int, passportRequired: BehaviorSubject<Boolean>, currentStatus: TravelerCheckoutStatus): AbstractUniversalCKOTravelerEntryWidgetViewModel {
        return HotelTravelerEntryWidgetViewModel(context, currentStatus, createTripOptInStatus)
    }

    override fun getTravelers() : List<Traveler> {
        val traveler = if (Db.sharedInstance.travelers.isNotEmpty()) {
            (Db.sharedInstance.travelers[0])
        } else {
            Traveler()
        }
        return listOf(traveler)
    }

    override fun requiresMultipleTravelers() = false

    override fun isValidForBooking(traveler: Traveler, index: Int): Boolean {
        return travelerValidator.isValidForHotelBooking(traveler)
    }
}
