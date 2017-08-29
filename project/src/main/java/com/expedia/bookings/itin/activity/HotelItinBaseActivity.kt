package com.expedia.bookings.itin.activity

import android.content.Intent
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.utils.Constants

abstract class HotelItinBaseActivity : AppCompatActivity() {
    val syncListener: ItinSyncListener = ItinSyncListener()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.ITIN_HOTEL_WEBPAGE_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val tripId = data.getStringExtra(Constants.ITIN_HOTEL_WEBPAGE_TRIP_NUMBER)
                if (tripId.isNotEmpty()) {
                    getItineraryManager().deepRefreshTrip(tripId, true)
                }
            }
        }
    }

    inner class ItinSyncListener : ItineraryManager.ItinerarySyncAdapter() {
        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            super.onSyncFinished(trips)
            updateItinCardDataHotel()
        }
    }

    abstract fun updateItinCardDataHotel()

    override fun onResume() {
        super.onResume()
        getItineraryManager().addSyncListener(syncListener)
    }

    override fun onPause() {
        super.onPause()
        getItineraryManager().removeSyncListener(syncListener)
    }

    override fun onBackPressed() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    @VisibleForTesting
    open fun getItineraryManager(): ItineraryManager {
        return ItineraryManager.getInstance()
    }
}