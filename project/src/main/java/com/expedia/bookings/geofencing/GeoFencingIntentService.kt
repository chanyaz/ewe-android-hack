package com.expedia.bookings.geofencing

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.data.foursquare.FourSquareResponse
import com.expedia.bookings.data.foursquare.Item_
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripCar
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.TripPackage
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.notification.GeofenceTransitionsIntentService
import com.expedia.bookings.services.FourSquareServices
import com.expedia.bookings.utils.FoursquareResponseUtil
import com.expedia.bookings.utils.Ui
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import org.json.JSONObject
import javax.inject.Inject

class GeoFencingIntentService : IntentService("GeoFencingIntentService"),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    var mGoogleApiClient: GoogleApiClient? = null
    var searchSubscriber: Disposable? = null

    lateinit var fourSquareService: FourSquareServices
        @Inject set

    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        Ui.getApplication(this).defaultFourSquareComponents()
        fourSquareService = Ui.getApplication(this).fourSquareComponent().fourSquareServices()
        buildGoogleApiClient()
    }

    override fun onHandleIntent(intent: Intent?) {

        if (!mGoogleApiClient!!.isConnecting() || !mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.connect()
        }

        val extra = intent?.getStringExtra(TripReceiverForGF.EXTRA_DATA)
        if (extra != null) {
            val trip = Trip()
            trip.fromJson(JSONObject(extra))

            //get location for API Call
            var loc = getLocFromTrip(trip)
            var place = loc
            if (areCoordinatesAvailable(trip)) {
                place = null
            } else {
                loc = null
            }

            if (loc!!.isNotEmpty()) {
                //make API call
                searchSubscriber = fourSquareService?.searchTrendingPlacesAround(loc, place)?.subscribeObserver(makeResultsObserver())
            }

        }
    }

    private fun makeResultsObserver(): Observer<FourSquareResponse> {
        return object : DisposableObserver<FourSquareResponse>() {
            override fun onError(e: Throwable) {
                //reset alarm
            }

            override fun onComplete() {
                //do nothing
            }

            override fun onNext(t: FourSquareResponse) {
                //delete all existing geofences based on previous response
                removeOldGeofences()

                //save new response
                FoursquareResponseUtil.saveResponse(this@GeoFencingIntentService, t)

                //set geo fences here
                t.response.groups.get(0).items.forEach { item ->
                    try {
                        val builder = GeofencingRequest.Builder()

                        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        builder.addGeofence(Geofence.Builder()
                                .setRequestId(item.venue.id)
                                .setCircularRegion(
                                        item.venue.location.lat,
                                        item.venue.location.lng,
                                        5000f
                                )
                                .setExpirationDuration(172800000L)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build())

                        val geoReq = builder.build()

                        if (!mGoogleApiClient!!.isConnected()) {
                            return
                        }

                        val result = LocationServices.getGeofencingClient(this@GeoFencingIntentService)
                                .addGeofences(geoReq, getPIforGeoFence(item, t.response.groups.get(0).items.indexOf(item)))

                        result.addOnSuccessListener {
                            Log.d("GeoFencingIntentService", "Success:" + item.venue.name)
                        }

                        result.addOnCompleteListener {
                            Log.d("GeoFencingIntentService", "Complete:" + item.venue.name)
                        }

                        result.addOnFailureListener {
                            Log.d("GeoFencingIntentService", "Failed:" + item.venue.name)
                        }
                    } catch (ex: Exception){
                        Log.d("GeoFencingIntentService", ex.toString() + " : exception in adding new geofence")
                    }

                }
            }

        }
    }

    private fun removeOldGeofences() {
        val oldResponse = FoursquareResponseUtil.loadResponse(this@GeoFencingIntentService)
        oldResponse?.response?.groups?.get(0)?.items?.forEach {
            try {
                val res = LocationServices.getGeofencingClient(this@GeoFencingIntentService).removeGeofences(getPIforGeoFence(it, oldResponse.response.groups.get(0).items.indexOf(it)))

                res.addOnSuccessListener {
                    Log.d("GeoFencingIntentService", "Successfully removed")
                }

                res.addOnCompleteListener {
                    Log.d("GeoFencingIntentService", "Completed removing")
                }

                res.addOnFailureListener {
                    Log.d("GeoFencingIntentService", "Failed removng old geofence")
                }
            } catch (ex: Exception){
                Log.d("GeoFencingIntentService", ex.toString() + " : exception in removing old geofence")
            }
        }
    }

    private fun areCoordinatesAvailable(trip: Trip): Boolean {
        when (trip.tripComponents.get(0).type) {
            TripComponent.Type.HOTEL, TripComponent.Type.PACKAGE, TripComponent.Type.CAR -> return true
            else -> return false
        }
    }

    private fun getPIforGeoFence(item: Item_, index: Int): PendingIntent {
        return GeofenceTransitionsIntentService.generateSchedulePendingIntent(this)
    }

    private fun getLocFromTrip(trip: Trip): String? {
        when (trip.tripComponents.get(0).type) {
            TripComponent.Type.HOTEL -> {
                val loc = (trip.tripComponents.get(0) as TripHotel).property.location
                return loc.latitude.toString() + "," + loc.longitude.toString()
            }
            TripComponent.Type.CRUISE -> {
                return ""
            }
            TripComponent.Type.RAILS -> {
                return trip.title
            }
            TripComponent.Type.FLIGHT -> {
                return trip.title
            }
            TripComponent.Type.PACKAGE -> {
                val loc = ((trip.tripComponents.get(0) as TripPackage).tripComponents.filter { it is TripHotel }.get(1) as TripHotel).property.location
                return loc.latitude.toString() + "," + loc.longitude.toString()
            }
            TripComponent.Type.CAR -> {
                val loc = (trip.tripComponents.get(0) as TripCar).car.pickUpLocation
                return loc.latitude.toString() + "," + loc.longitude.toString()
            }
            TripComponent.Type.ACTIVITY -> {
                return trip.title
            }
        }
        return ""
    }

    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mGoogleApiClient!!.isConnecting() || mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }
}
