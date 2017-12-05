package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus

class HotelTravelerEntryWidgetViewModel(context: Context, travelerCheckoutStatus: TravelerCheckoutStatus) : AbstractUniversalCKOTravelerEntryWidgetViewModel(context, 0) {

    init {
        updateTraveler(getTraveler())
        if (travelerCheckoutStatus != TravelerCheckoutStatus.CLEAN) {
            validate()
        }
        showPhoneNumberObservable.onNext(true)
    }

    override fun getTraveler(): Traveler {
        if (Db.getTravelers().isNotEmpty()) {
            return Db.getTravelers()[0]
        } else {
            val traveler = Traveler()
            traveler.email = ""
            return traveler
        }
    }

    override fun updateTraveler(traveler: Traveler) {
        super.updateTraveler(traveler)
    }
}