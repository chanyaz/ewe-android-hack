package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.KrazyglueServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.RewardsUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.isKrazyglueOnFlightsConfirmationEnabled
import com.expedia.bookings.utils.HMACUtil
import com.expedia.util.Optional
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightConfirmationViewModel(val context: Context, isWebCheckout: Boolean = false) {

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
    val krazyglueDestinationObservable = PublishSubject.create<String>()
    val krazyglueHotelsObservable = PublishSubject.create<List<KrazyglueResponse.KrazyglueHotel>>()
    val flightCheckoutResponseObservable = PublishSubject.create<FlightCheckoutResponse>()
    val itinDetailsResponseObservable = PublishSubject.create<FlightItinDetailsResponse>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    private val krazyglueService: KrazyglueServices by lazy {
        Ui.getApplication(context).flightComponent().krazyglueService()
    }
    private val isKrazyglueEnabled = isKrazyglueOnFlightsConfirmationEnabled(context)

    init {
        if (isWebCheckout) {
            setupItinResponseSubscription()
        } else {
            setupCheckoutResponseSubscription()
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

    private fun setupCheckoutResponseSubscription() {
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
            tripTotalPriceSubject.onNext(response.totalChargesPrice?.formattedMoneyFromAmountAndCurrencyCode ?: "")
            val hasInsurance = response.flightAggregatedResponse?.flightsDetailResponse?.first()?.
                    offer?.selectedInsuranceProduct != null

            showTripProtectionMessage.onNext(hasInsurance)

            crossSellWidgetVisibility.onNext(isQualified)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

            if (isKrazyglueEnabled) {
                val destinationCode = response.getFirstFlightLastSegment().arrivalAirportCode
                val destinationArrivalDateTime = response.getFirstFlightLastSegment().arrivalTimeRaw
                val apiKey = context.getString(R.string.exp_krazy_glue_prod_key)
                val baseUrl = context.getString(R.string.exp_krazy_glue_base_url)
                val signedUrl = HMACUtil.getSignedKrazyglueUrl(baseUrl, apiKey, destinationCode, destinationArrivalDateTime)
                krazyglueService.getKrazyglueHotels(signedUrl, makeNewKrazyglueObserver())
            }
        }

        flightCheckoutResponseObservable.subscribe { response ->
            val destinationCity = response.getFirstFlightLeg().segments?.last()?.arrivalAirportAddress?.city ?: ""
            val numberOfGuests = response.passengerDetails.size

            destinationObservable.onNext(destinationCity)
            numberOfTravelersSubject.onNext(numberOfGuests)
        }
    }

    private fun setupItinResponseSubscription() {
        itinDetailsResponseObservable.subscribe { response ->
            var email = response.getMainTravelerEmail()
            val itinNumber = response.getItinNumber()
            val isQualified = response.getIsAirAttachQualified()
            val destinationCity = response.getOutboundDestinationCity() ?: ""
            val numberOfGuests = response.getNumberOfPassengers()
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", if (email.isEmpty()) context.getString(R.string.itinerary_default_email_message) else email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            val itinContentDescription = Phrase.from(context, R.string.confirmation_number_TEMPLATE)
                    .put("number", itinNumber)
                    .format().toString()
            itinNumContentDescriptionObservable.onNext(itinContentDescription)
            if (!userStateManager.isUserAuthenticated() && !ExpediaBookingApp.isRobolectric()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            tripTotalPriceSubject.onNext(response.getTripTotalPrice())

            val hasInsurance = response.responseData.insurance != null
            showTripProtectionMessage.onNext(hasInsurance)

            crossSellWidgetVisibility.onNext(isQualified)
            destinationObservable.onNext(destinationCity)
            numberOfTravelersSubject.onNext(numberOfGuests)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
        }
    }

    private fun makeNewKrazyglueObserver(): Observer<KrazyglueResponse> {
        return object : Observer<KrazyglueResponse> {
            override fun onNext(response: KrazyglueResponse) {
                if (response.success) {
                    krazyglueDestinationObservable.onNext(response.destinationName)
                    krazyglueHotelsObservable.onNext(response.krazyglueHotels)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable) {
                Log.d("Error fetching krazy glue hotels:" + e.stackTrace)
//                TODO: handle failed krazy glue request
            }
        }
    }
}