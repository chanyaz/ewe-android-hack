package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus

class LXTravelerEntryWidgetViewModel(context: Context, travelerCheckoutStatus: TravelerCheckoutStatus) : AbstractUniversalCKOTravelerEntryWidgetViewModel(context, 0) {

    init {
        updateTraveler(getTraveler())
        if (travelerCheckoutStatus != TravelerCheckoutStatus.CLEAN) {
            validate()
        }
        showPhoneNumberObservable.onNext(true)
    }

    override fun getTraveler(): Traveler {
        //TO-DO get real traveler
        val traveler = Traveler()
        traveler.email = ""
        return traveler
    }
}
