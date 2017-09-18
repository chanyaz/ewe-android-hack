package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.RewardsUtil
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightConfirmationViewModel(val context: Context) {

    val confirmationObservable = PublishSubject.create<Pair<FlightCheckoutResponse, String>>()
    val setRewardsPoints = PublishSubject.create<Optional<String>>()
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val rewardPointsObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val itinNumContentDescriptionObservable = BehaviorSubject.create<String>()
    val inboundCardVisibility = BehaviorSubject.create<Boolean>()
    val crossSellWidgetVisibility = BehaviorSubject.create<Boolean>()
    val tripTotalPriceSubject = PublishSubject.create<String>()
    val numberOfTravelersSubject = PublishSubject.create<Int>()
    val formattedTravelersStringSubject = PublishSubject.create<String>()
    val showTripProtectionMessage = BehaviorSubject.create<Boolean>(false)

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        confirmationObservable.subscribe { pair ->
            val email = pair.second
            val response = pair.first
            val itinNumber = response.newTrip!!.itineraryNumber
            val isQualified = response.airAttachInfo?.hasAirAttach ?: false
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            val itinContentDescription = Phrase.from(context, R.string.confirmation_number_TEMPLATE)
                    .put("number", itinNumber)
                    .format().toString()
            itinNumContentDescriptionObservable.onNext(itinContentDescription)
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            tripTotalPriceSubject.onNext(response.totalChargesPrice?.formattedMoneyFromAmountAndCurrencyCode)
            val hasInsurance = response.flightAggregatedResponse?.flightsDetailResponse?.first()?.
                    offer?.selectedInsuranceProduct != null

            showTripProtectionMessage.onNext(hasInsurance)

            crossSellWidgetVisibility.onNext(isQualified)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
        }

        numberOfTravelersSubject.subscribe { number ->
            formattedTravelersStringSubject.onNext(StrUtils.formatTravelerString(context, number))
        }

        setRewardsPoints.subscribe { points ->
            points.value?.let {
                if (userStateManager.isUserAuthenticated() && PointOfSale.getPointOfSale().shouldShowRewards()) {
                    val rewardPointText = RewardsUtil.buildRewardText(context, it, ProductFlavorFeatureConfiguration.getInstance(), isFlights = true)
                    if (Strings.isNotEmpty(rewardPointText)) {
                        rewardPointsObservable.onNext(rewardPointText)
                    }
                }
            }
        }
    }
}