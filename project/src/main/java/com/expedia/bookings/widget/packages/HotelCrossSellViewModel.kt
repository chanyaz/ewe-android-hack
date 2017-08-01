package com.expedia.bookings.widget.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import io.reactivex.subjects.BehaviorSubject

class HotelCrossSellViewModel(context: Context) {

    val searchParamsObservable = BehaviorSubject.create<com.expedia.bookings.data.flights.FlightSearchParams>()
    val confirmationObservable = BehaviorSubject.create<FlightCheckoutResponse>()
    val expiresTodayVisibility = BehaviorSubject.create<Boolean>()
    val daysRemainingVisibility = BehaviorSubject.create<Boolean>()
    val daysRemainingText = BehaviorSubject.create<String>()

    init {
        confirmationObservable.subscribe { response ->
            val isQualified = response.airAttachInfo?.hasAirAttach ?: false
            if (isQualified) {
                val expirationDate = response.airAttachInfo?.offerExpirationTimes?.airAttachExpirationTime()
                val currentDate = DateTime()
                val daysRemaining = JodaUtils.daysBetween(currentDate, expirationDate)
                if (daysRemaining > 0) { // can be null (see #5771)
                    daysRemainingVisibility.onNext(true)
                    daysRemainingText.onNext(Phrase.from(context.resources.getQuantityString(R.plurals.days_from_now, daysRemaining, daysRemaining))
                            .put("days", daysRemaining).format().toString())
                } else {
                    expiresTodayVisibility.onNext(true)
                }
            }

        }
    }
}