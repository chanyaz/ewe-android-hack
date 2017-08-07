package com.expedia.bookings.utils

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.ItinShareTargetBroadcastReceiver
import com.expedia.bookings.utils.navigation.NavUtils
import com.mobiata.android.util.SettingUtils
import java.util.*

@TargetApi(25)
object ShortcutUtils : ItineraryManager.ItinerarySyncAdapter() {

    private lateinit var mainContext: Context
    private var shortcutManager: ShortcutManager? = null
    private var loadedTrips: MutableCollection<Trip> = arrayListOf()

    fun initialize(context: Context, manager: ShortcutManager?) {
        ItineraryManager.getInstance().addSyncListener(this)
        mainContext = context
        shortcutManager = manager
        setupShortcuts()
    }

    private fun setupShortcuts() {
        val tripsShortcut = ShortcutInfo.Builder(mainContext, "trips")
                .setShortLabel(mainContext.resources.getString(R.string.trips))
                .setLongLabel(mainContext.resources.getString(R.string.my_trips))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_itin_ready))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("expda://trips")))
                .build()

        val hotelShortcut = ShortcutInfo.Builder(mainContext, "hotel")
                .setShortLabel(mainContext.resources.getString(R.string.get_a_room))
                .setLongLabel(mainContext.resources.getString(R.string.find_a_hotel_tonight))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_stat_hotel))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("expda://hotelSearch")))
                .build()

        val shareFlightShortcut = ShortcutInfo.Builder(mainContext, "flight")
                .setShortLabel(mainContext.resources.getString(R.string.share_flight))
                .setLongLabel(mainContext.resources.getString(R.string.share_flight_status))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_stat_flight))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("expda://flightShare")))
                .build()

        shortcutManager?.dynamicShortcuts = Arrays.asList(tripsShortcut, hotelShortcut, shareFlightShortcut)
    }

    fun shareFlightStatus(context: Context) {
        val shareUtils = ShareUtils(context)
        val flightTrips = arrayListOf<Trip>()

        NavUtils.goToLaunchScreen(context)

        if (loadedTrips.isNotEmpty()) {
            for (trip in loadedTrips) {
                if (trip.tripComponents.any({ c -> c.type == TripComponent.Type.FLIGHT })) {
                    flightTrips.add(trip)
                }
            }

            if (flightTrips.isNotEmpty()) {
                val closestFlightTrip = flightTrips.last()
                val tripFlightComponent = closestFlightTrip.tripComponents.findLast { tripComponent -> tripComponent.type == TripComponent.Type.FLIGHT } as TripFlight
                val shareString = shareUtils.getFlightShareTextShort(tripFlightComponent.flightTrip.getLeg(0), closestFlightTrip.shareInfo.sharableDetailsUrl, false, tripFlightComponent.travelers.first().firstName)

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString)
                shareIntent.type = "text/plain"

                SettingUtils.save(context, "TripType", "Flight")

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    context.startActivity(shareIntent)
                } else {
                    val receiver = Intent(context, ItinShareTargetBroadcastReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
                    val chooserIntent = Intent.createChooser(shareIntent, "", pendingIntent.intentSender)
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
                    context.startActivity(chooserIntent)
                }
            }
        }
    }

    override fun onSyncFinished(trips: MutableCollection<Trip>?) {
        if (trips != null && trips.any()) {
            shortcutManager?.enableShortcuts(listOf("flight"))
            loadedTrips = trips
        } else {
            shortcutManager?.disableShortcuts(listOf("flight"))
        }
    }
}