package com.expedia.bookings.flights.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import io.reactivex.subjects.BehaviorSubject

class HotelCrossSellViewModel(context: Context) {

    val searchParamsObservable = BehaviorSubject.create<com.expedia.bookings.data.flights.FlightSearchParams>()
    val confirmationObservable = BehaviorSubject.create<FlightCheckoutResponse>()
    val itinDetailsResponseObservable = BehaviorSubject.create<FlightItinDetailsResponse>()
    val expiresTodayVisibility = BehaviorSubject.create<Boolean>()
    val daysRemainingVisibility = BehaviorSubject.create<Boolean>()
    val daysRemainingText = BehaviorSubject.create<String>()

    init {
        confirmationObservable.subscribe { response ->
            val isQualified = response.airAttachInfo?.hasAirAttach ?: false
            if (isQualified) {
                val expirationDate = response.airAttachInfo?.offerExpirationTimes?.airAttachExpirationTime()
                setDaysRemainingText(context, expirationDate!!)
            }
        }

        itinDetailsResponseObservable.subscribe { response ->
            val isQualifed = response.responseData.airAttachQualificationInfo.airAttachQualified
            if (isQualifed) {
                val expirationDate = response.responseData.airAttachQualificationInfo.offerExpiresTime.airAttachExpirationTime()
                setDaysRemainingText(context, expirationDate)
            }
        }
    }

    private fun setDaysRemainingText(context: Context, expirationDate: DateTime) {
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
