package com.expedia.bookings.presenter

import android.support.annotation.CheckResult
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import io.reactivex.Flowable
import io.reactivex.Single

class HotelTravelersRepository : TravelersRepository {

    override val travelers: Flowable<List<Traveler>>
        get() {
            return getTravelers().toFlowable()
        }

    @CheckResult
    private fun getTravelers(): Single<List<Traveler>> {
        return Single.create { emitter ->
            emitter.onSuccess(Db.sharedInstance.travelers)
        }
    }

    override fun updateTraveler(index: Int, traveler: Traveler) {
        Db.sharedInstance.travelers[index] = traveler
        refreshTravelers()
    }

    private fun refreshTravelers() {
        travelers.repeat()
    }

}
