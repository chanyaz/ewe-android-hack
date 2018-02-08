package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.notification.INotificationManager
import com.expedia.bookings.notification.PushNotificationUtils
import com.expedia.bookings.server.ExpediaServices
import com.expedia.bookings.server.ExpediaServicesPushInterface
import com.expedia.bookings.server.PushRegistrationResponseHandler
import com.expedia.bookings.services.ITNSServices
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.UniqueIdentifierHelper
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.mobiata.flightlib.data.Flight
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

open class NotificationScheduler @JvmOverloads constructor(val context: Context,
                                                           val db: Db = Db.sharedInstance,
                                                           val services: ExpediaServicesPushInterface = ExpediaServices(context),
                                                           var notificationManager: INotificationManager,
                                                           val userStateManager: UserStateManager,
                                                           val tnsServices: ITNSServices,
                                                           val gcmRegistrationKeeper: GCMRegistrationKeeper = GCMRegistrationKeeper.getInstance(context),
                                                           val pos: PointOfSale = PointOfSale.getPointOfSale()) {

    private val LOGGING_TAG = "NotificationScheduler"

    fun subscribeToListener(finishObserver: PublishSubject<List<ItinCardData>>) {
        finishObserver.observeOn(Schedulers.io()).subscribe(makeNewFinishedObserver())
    }

    fun scheduleLocalNotifications(dataCards: List<ItinCardData>) {
        for (data in dataCards) {
            if (data.isSharedItin) {
                continue
            }

            val generator = getGenerator(data)

            val notifications = generator.generateNotifications() ?: continue
            for (notification in notifications) {
                if (SettingUtils.get(context, context.getString(R.string.preference_launch_all_trip_notifications), false)) {
                    notification.triggerTimeMillis = System.currentTimeMillis() + 5000
                }

                notificationManager.searchForExistingAndUpdate(notification)
            }
        }

        notificationManager.scheduleAll()
        notificationManager.cancelAllExpired()
    }

    open fun getGenerator(data: ItinCardData): ItinContentGenerator<out ItinCardData> =
            ItinContentGenerator.createGenerator(context, data)

    fun registerForPushNotifications(itinCardDatas: List<ItinCardData>) {

        Log.d(LOGGING_TAG, "registerForPushNotifications")

        //NOTE: If this is the first time we are registering for push notifications, regId will likely be empty
        //we need to wait for a gcm callback before we will get a regid, so we just skip for now and wait for the next sync
        //at which time we should have a valid id (assuming network is up and running)
        val regId = gcmRegistrationKeeper.getRegistrationId(context)
        Log.d(LOGGING_TAG, "registerForPushNotifications regId:" + regId)
        if (!regId.isNullOrEmpty()) {
            Log.d(LOGGING_TAG, "registerForPushNotifications regId:$regId is not empty!")

            val langId = pos.dualLanguageId
            val siteId = pos.siteId
            val userTuid: Long = 0
            val tnsUser = getTNSUser(siteId)

            val courier = Courier("gcm", Integer.toString(langId), BuildConfig.APPLICATION_ID, regId, UniqueIdentifierHelper.getID(context))
            //use old Flight Alert system
            if (!AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.TripsNewFlightAlerts)) {
                val payload = PushNotificationUtils
                        .buildPushRegistrationPayload(context, regId, siteId, userTuid,
                                getItinFlights(itinCardDatas))

                Log.d(LOGGING_TAG, "registerForPushNotifications payload:" + payload.toString())

                Log.d(LOGGING_TAG, "registering with old alert system")
                val resp = services.registerForPushNotifications(
                        PushRegistrationResponseHandler(context), payload, regId)
                Log.d(LOGGING_TAG,
                        "registerForPushNotifications response:" + resp?.success)

                tnsServices.deregisterForFlights(tnsUser, courier)
            } else {
                //use new TNS system
                val payload = PushNotificationUtils
                        .buildPushRegistrationPayload(context, regId, siteId, userTuid,
                                ArrayList())

                Log.d(LOGGING_TAG, "registerForPushNotifications payload:" + payload.toString())

                val resp = services.registerForPushNotifications(
                        PushRegistrationResponseHandler(context), payload, regId)
                Log.d(LOGGING_TAG,
                        "registerForPushNotifications response:" + resp?.success)

                tnsServices.registerForFlights(tnsUser, courier, getFlightsForNewSystem(itinCardDatas))
            }
        }
    }

    open fun getTNSUser(siteId: Int): TNSUser {
        val siteIdString = siteId.toString()

        val guid = db.abacusGuid

        return if (userStateManager.isUserAuthenticated()) {
            val userDetail = userStateManager.userSource
            TNSUser(siteIdString, userDetail.tuid!!.toString(), userDetail.expUserId!!.toString(), guid)
        } else {
            TNSUser(siteIdString, null, null, guid)
        }
    }

    fun getItinFlights(itinCardDatas: List<ItinCardData>): List<Flight> {
        val retFlights = ArrayList<Flight>()
        for (data in itinCardDatas) {
            if (data.tripComponentType != null && data.tripComponentType == TripComponent.Type.FLIGHT
                    && data.tripComponent != null && data is ItinCardDataFlight) {
                val leg = data.flightLeg
                if (leg != null && leg.segments != null) {
                    retFlights.addAll(leg.segments)
                }
            }
        }
        return retFlights
    }

    fun getFlightsForNewSystem(itinCardDatas: List<ItinCardData>): List<TNSFlight> {
        val flights = ArrayList<TNSFlight>()
        val itinFlights = getItinFlights(itinCardDatas)
        val dateTimeTimeZonePattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"
        for (flight in itinFlights) {
            if (isFlightDataAvailable(flight)) {
                val primaryFlightCode = flight.primaryFlightCode
                val flightToAdd = TNSFlight(primaryFlightCode!!.mAirlineCode,
                        JodaUtils.format(flight.segmentArrivalTime, dateTimeTimeZonePattern),
                        JodaUtils.format(flight.segmentDepartureTime, dateTimeTimeZonePattern),
                        flight.destinationWaypoint.mAirportCode,
                        primaryFlightCode.mNumber,
                        flight.originWaypoint.mAirportCode
                )
                flights.add(flightToAdd)
            }
        }
        return flights
    }

    fun isFlightDataAvailable(flight: Flight): Boolean {
        val primaryFlightCode = flight.primaryFlightCode
        val isAirlineCodeAvailable = primaryFlightCode != null && !primaryFlightCode.mAirlineCode.isNullOrEmpty()
        val isFlightNumberAvailable = primaryFlightCode != null && !primaryFlightCode.mNumber.isNullOrEmpty()
        val isSegmentArrivalTimeAvailable = flight.segmentArrivalTime != null
        val isSegmentDepartureTimeAvailable = flight.segmentDepartureTime != null
        val isDestinationAirportCodeAvailable = flight.destinationWaypoint != null && !flight.destinationWaypoint.mAirportCode.isNullOrEmpty()
        val isOriginAirportCodeAvailable = flight.originWaypoint != null && !flight.originWaypoint.mAirportCode.isNullOrEmpty()
        return (isAirlineCodeAvailable && isFlightNumberAvailable && isSegmentArrivalTimeAvailable
                && isSegmentDepartureTimeAvailable && isDestinationAirportCodeAvailable && isOriginAirportCodeAvailable)
    }

    private fun makeNewFinishedObserver() = endlessObserver<List<ItinCardData>> {
        scheduleLocalNotifications(it)
        registerForPushNotifications(it)
    }
}
