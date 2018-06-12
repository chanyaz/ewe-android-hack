package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.features.Features
import com.expedia.bookings.itin.tripstore.data.Flight
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.getDateTime
import com.expedia.bookings.itin.tripstore.extensions.getLegs
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.notification.INotificationManager
import com.expedia.bookings.services.ITNSServices
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.UniqueIdentifierHelper
import com.expedia.bookings.utils.UniqueIdentifierPersistenceProvider
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class NotificationScheduler @JvmOverloads constructor(val context: Context,
                                                      val db: Db = Db.sharedInstance,
                                                      var notificationManager: INotificationManager,
                                                      val userStateManager: UserStateManager,
                                                      val tnsServices: ITNSServices,
                                                      val gcmRegistrationKeeper: GCMRegistrationKeeper = GCMRegistrationKeeper.getInstance(context),
                                                      val pos: PointOfSale = PointOfSale.getPointOfSale(),
                                                      val jsonUtil: IJsonToItinUtil) {

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
                if (Features.all.launchAllTripNotifications.enabled()) {
                    notification.triggerTimeMillis = System.currentTimeMillis() + 5000
                }

                notificationManager.searchForExistingAndUpdate(notification)
            }
        }

        notificationManager.scheduleAll()
        notificationManager.cancelAllExpired()
    }

    private fun getGenerator(data: ItinCardData): ItinContentGenerator<out ItinCardData> =
            ItinContentGenerator.createGenerator(context, data)

    fun registerForPushNotifications() {

        Log.d(LOGGING_TAG, "registerForPushNotifications")

        //NOTE: If this is the first time we are registering for push notifications, regId will likely be empty
        //we need to wait for a gcm callback before we will get a regid, so we just skip for now and wait for the next sync
        //at which time we should have a valid id (assuming network is up and running)
        val regId = gcmRegistrationKeeper.getRegistrationId(context)
        Log.d(LOGGING_TAG, "registerForPushNotifications regId:$regId")
        if (!regId.isNullOrEmpty()) {
            Log.d(LOGGING_TAG, "registerForPushNotifications regId:$regId is not empty!")

            val langId = pos.dualLanguageId
            val siteId = pos.siteId
            val tnsUser = getTNSUser(siteId)

            val courier = Courier("gcm", Integer.toString(langId), BuildConfig.APPLICATION_ID, regId,
                    UniqueIdentifierHelper.getID(UniqueIdentifierPersistenceProvider(context)))
            val itinList = jsonUtil.getItinList()
            tnsServices.registerForFlights(tnsUser, courier, getTNSFlights(itinList))
        }
    }

    fun getTNSUser(siteId: Int): TNSUser {
        val siteIdString = siteId.toString()

        val guid = db.abacusGuid

        return if (userStateManager.isUserAuthenticated()) {
            val userDetail = userStateManager.userSource
            TNSUser(siteIdString, userDetail.tuid!!.toString(), userDetail.expUserId!!.toString(), guid)
        } else {
            TNSUser(siteIdString, null, null, guid)
        }
    }

    private fun makeNewFinishedObserver() = endlessObserver<List<ItinCardData>> {
        scheduleLocalNotifications(it)
        registerForPushNotifications()
    }

    fun getTNSFlights(itins: List<Itin>): List<TNSFlight> {
        val retList = mutableListOf<TNSFlight>()
        val dateTimeTimeZonePattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"
        itins.forEach { itin ->
            val legs = itin.getLegs()
            legs.forEach { leg ->
                leg.segments.forEach { flight ->
                    if (isFlightDataAvailable(flight))
                        retList.add(TNSFlight(flight.airlineCode!!,
                                JodaUtils.format(flight.arrivalTime!!.getDateTime(), dateTimeTimeZonePattern),
                                JodaUtils.format(flight.departureTime!!.getDateTime(), dateTimeTimeZonePattern),
                                flight.arrivalLocation?.airportCode!!, flight.flightNumber!!, flight.departureLocation?.airportCode!!,
                                flight == leg.segments.last()))
                }
            }
        }
        return retList
    }

    fun isFlightDataAvailable(flight: Flight): Boolean {
        val isAirlineCodeAvailable = !flight.airlineCode.isNullOrEmpty()
        val isFlightNumberAvailable = !flight.flightNumber.isNullOrEmpty()
        val isSegmentArrivalTimeAvailable = !flight.arrivalTime?.raw.isNullOrEmpty()
        val isSegmentDepartureTimeAvailable = !flight.departureTime?.raw.isNullOrEmpty()
        val isDestinationAirportCodeAvailable = !flight.arrivalLocation?.airportCode.isNullOrEmpty()
        val isOriginAirportCodeAvailable = !flight.departureLocation?.airportCode.isNullOrEmpty()
        return (isAirlineCodeAvailable && isFlightNumberAvailable && isSegmentArrivalTimeAvailable
                && isSegmentDepartureTimeAvailable && isDestinationAirportCodeAvailable && isOriginAirportCodeAvailable)
    }
}
