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
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.json.JSONObject
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightConfirmationViewModel(val context: Context) {

    val confirmationObservable = PublishSubject.create<Pair<FlightCheckoutResponse, String>>()
    val setRewardsPoints = PublishSubject.create<String>()
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val rewardPointsObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val inboundCardVisibility = BehaviorSubject.create<Boolean>()
    val crossSellWidgetVisibility = BehaviorSubject.create<Boolean>()
    val tripTotalPriceSubject = PublishSubject.create<String>()
    val numberOfTravelersSubject = PublishSubject.create<Int>()
    val formattedTravelersStringSubject = PublishSubject.create<String>()
    val showTripProtectionMessage = BehaviorSubject.create<Boolean>(false)
    val isNewConfirmationScreenEnabled = BehaviorSubject.create<Boolean>(false)

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        confirmationObservable.subscribe { pair ->
            val email = pair.second
            val response = pair.first
            val itinNumber = response.newTrip!!.itineraryNumber
            val tripId = response.newTrip!!.tripId
            val travelRecordLocator = response.newTrip!!.travelRecordLocator
            val isQualified = response.airAttachInfo?.hasAirAttach ?: false
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            if (isNewConfirmationScreenEnabled.value) {
                tripTotalPriceSubject.onNext(response.totalChargesPrice?.formattedMoney)
                val hasInsurance = response.flightAggregatedResponse?.flightsDetailResponse?.first()?.
                        offer?.selectedInsuranceProduct != null

                showTripProtectionMessage.onNext(hasInsurance)
            }
            crossSellWidgetVisibility.onNext(isQualified)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
            val user = com.expedia.bookings.data.Db.getUser()
                        if(user != null) {
                            val expediaUserId = user.expediaUserId;
                            val tuid = user.tuidString
                            val deviceId = com.google.firebase.iid.FirebaseInstanceId.getInstance().getToken() as String
                            sendTravelNotifications(expediaUserId,tuid,deviceId,email,tripId,travelRecordLocator,itinNumber)
                        }
        }

        numberOfTravelersSubject.subscribe { number ->
            formattedTravelersStringSubject.onNext(StrUtils.formatTravelerString(context, number))
        }

        setRewardsPoints.subscribe { points ->
            if (points != null)
                if (userStateManager.isUserAuthenticated() && PointOfSale.getPointOfSale().shouldShowRewards()) {
                    val rewardPointText = RewardsUtil.buildRewardText(context, points, ProductFlavorFeatureConfiguration.getInstance(), isNewConfirmationScreenEnabled.value)
                    if (Strings.isNotEmpty(rewardPointText)) {
                        rewardPointsObservable.onNext(rewardPointText)
                    }
                }
        }
    }

    private fun sendTravelNotifications( expediaUserId: String, tuid: String, deviceId: String, email: String, tripId: String?, travelRecordLocator: String?, itinNumber: String?){
            val thread = Thread(Runnable {
                try {
                    val httpclient = DefaultHttpClient()
                    val httppost = HttpPost("http://DELC02NG1WGG3QC.sea.corp.expecn.com:8080/notification/bookingConfirmation")
                    httppost.addHeader("Accept", "application/json")
                    httppost.addHeader("Content-Type", "application/json")
                    val json = JSONObject()
                    json.put("expUserId", expediaUserId)
                    json.put("tuId", tuid)
                    json.put("emailAddress", email)
                    json.put("deviceId", deviceId)
                    json.put("tripId", tripId)
                    json.put("travelRecordLocator", travelRecordLocator)
                    json.put("itinId", itinNumber)
                    val params = StringEntity(json.toString())
                    httppost.entity = params
                    //execute http post
                    val response = httpclient.execute(httppost)
                    Log.e(response.toString())
                    //Your code goes here
                } catch (e: Exception) {
                    Log.e("ERROR Occurred!!!")
                    e.printStackTrace()
                }
            })
            thread.start()
        }
}