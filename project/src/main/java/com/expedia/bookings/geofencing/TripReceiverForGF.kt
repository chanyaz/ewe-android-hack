package com.expedia.bookings.geofencing

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expedia.bookings.data.trips.Trip

/**
 * Created by nbirla on 14/02/18.
 */

class TripReceiverForGF : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geoIntent = Intent(context, GeoFencingIntentService::class.java)
        geoIntent.putExtras(intent)

        context!!.startService(geoIntent)
    }

    companion object {
        val EXTRA_DATA = "EXTRA_DATA"

        @JvmStatic
        fun generateSchedulePendingIntent(context: Context, trip: Trip): PendingIntent {
            val intent = Intent(context, TripReceiverForGF::class.java)
            intent.putExtra(EXTRA_DATA, trip.toJson().toString())
            return PendingIntent.getBroadcast(context, trip.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

}
