package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.Strings
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

// Open for Testing Purpose Only
 open class FlightConfirmationViewModel(val context: Context){
    val confirmationObservable = PublishSubject.create<Pair<FlightCheckoutResponse, String>>()
    val rewardPointsObservable = PublishSubject.create<String>()
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val rewardsPointsObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val inboundCardVisibility = BehaviorSubject.create<Boolean>()
    val crossSellWidgetVisibility = BehaviorSubject.create<Boolean>()
    val crossSellTodayVisibility = BehaviorSubject.create<Boolean>()
    val crossSellCountDownVisibility = BehaviorSubject.create<Boolean>()
    val crossSellText = BehaviorSubject.create<String>()


    init {
        confirmationObservable.subscribe { pair ->
            val email = pair.second
            val response = pair.first
            val itinNumber = response.newTrip.itineraryNumber
            val isQualified = response.airAttachInfo?.hasAirAttach as Boolean
            val expirationDate = response.airAttachInfo?.offerExpirationTimes?.airAttachExpirationTime()

            val itinNumberMessage = Phrase.from(context, R.string.package_itinerary_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            if (!isUserLoggedIn()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            if (isQualified) { // can be null (see #5771)
                val currentDate = DateTime()
                val daysRemaining = JodaUtils.daysBetween(currentDate, expirationDate)
                crossSellWidgetVisibility.onNext(true)
                if (daysRemaining > 0) {
                    crossSellCountDownVisibility.onNext(true)
                    crossSellText.onNext(Phrase.from(context.resources.getQuantityString(R.plurals.days_from_now, daysRemaining, daysRemaining))
                            .put("days", daysRemaining).format().toString())
                } else {
                    crossSellTodayVisibility.onNext(true)
                }
            } else {
                crossSellWidgetVisibility.onNext(false)
            }
        }

        if (isUserLoggedIn()) {
            rewardPointsObservable.subscribe { rewardPoints ->
                if (Strings.isNotEmpty(rewardPoints)) {
                    val rewardsPointsText = Phrase.from(context, R.string.package_confirmation_reward_points)
                            .put("rewardpoints", rewardPoints)
                            .put("brand", BuildConfig.brand)
                            .format().toString()
                    rewardsPointsObservable.onNext(rewardsPointsText)
                }
            }
        }
    }
     open protected fun isUserLoggedIn() = User.isLoggedIn(context)

 }