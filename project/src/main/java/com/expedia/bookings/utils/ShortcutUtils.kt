package com.expedia.bookings.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.ItinShareTargetBroadcastReceiver
import com.expedia.bookings.utils.navigation.NavUtils
import com.mobiata.android.util.SettingUtils

@RequiresApi(25)
object ShortcutUtils : ItineraryManager.ItinerarySyncAdapter() {

    private lateinit var mainContext: Context
    private var shortcutManager: ShortcutManager? = null
    private var loadedTrips: MutableCollection<Trip> = arrayListOf()
    private var allShortcuts: List<ShortcutInfo> = listOf()
    private var noFlightShortcuts: List<ShortcutInfo> = listOf()

    fun initialize(context: Context) {
        ItineraryManager.getInstance().addSyncListener(this)
        mainContext = context
        shortcutManager = mainContext.getSystemService<ShortcutManager>(ShortcutManager::class.java)
        setupShortcuts()
    }

    private fun setupShortcuts() {
        val tripsShortcut = ShortcutInfo.Builder(mainContext, "trips")
                .setShortLabel(mainContext.resources.getString(R.string.trips))
                .setLongLabel(mainContext.resources.getString(R.string.my_trips))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_itin_ready))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(mainContext.getString(R.string.deeplink_trips))))
                .build()

        val hotelShortcut = ShortcutInfo.Builder(mainContext, "hotel")
                .setShortLabel(mainContext.resources.getString(R.string.get_a_room))
                .setLongLabel(mainContext.resources.getString(R.string.find_a_hotel_tonight))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_stat_hotel))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(mainContext.getString(R.string.deeplink_hotel_search))))
                .build()

        val shareFlightShortcut = ShortcutInfo.Builder(mainContext, "flight")
                .setShortLabel(mainContext.resources.getString(R.string.share_flight))
                .setLongLabel(mainContext.resources.getString(R.string.share_flight_status))
                .setIcon(Icon.createWithResource(mainContext, R.drawable.ic_stat_flight))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(mainContext.getString(R.string.deeplink_flight_share))))
                .build()

        allShortcuts = listOf(tripsShortcut, hotelShortcut, shareFlightShortcut)
        noFlightShortcuts = listOf(tripsShortcut, hotelShortcut)
        shortcutManager?.dynamicShortcuts = noFlightShortcuts
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
                val closestFlightTrip = flightTrips.sortedBy { flight -> flight.startDate }.first()
                val tripFlightComponent = closestFlightTrip.tripComponents.first { tripComponent -> tripComponent.type == TripComponent.Type.FLIGHT && tripComponent.startDate.isAfterNow } as TripFlight
                val shareString = shareUtils.getFlightShareTextShort(tripFlightComponent.flightTrip.getLeg(0), closestFlightTrip.shareInfo.sharableDetailsUrl, false, tripFlightComponent.travelers.first().firstName)

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString)
                shareIntent.type = "text/plain"

                SettingUtils.save(context, "TripType", "Flight")

                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
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
            var hasFlights = false
            loadedTrips = trips
            for (trip in loadedTrips) {
                if (trip.tripComponents.any({ c -> c.type == TripComponent.Type.FLIGHT })) {
                    hasFlights = true
                }
            }

            when (hasFlights && ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
                true -> shortcutManager?.dynamicShortcuts = allShortcuts
                false -> shortcutManager?.dynamicShortcuts = noFlightShortcuts
            }
        } else {
            shortcutManager?.dynamicShortcuts = noFlightShortcuts
        }
    }
}
