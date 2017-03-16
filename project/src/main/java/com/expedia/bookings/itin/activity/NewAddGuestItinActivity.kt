package com.expedia.bookings.itin.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.presenter.trips.ItinSignInPresenter
import com.expedia.bookings.tracking.OmnitureTracking

class NewAddGuestItinActivity : AppCompatActivity() {

    val mSignInPresenter: ItinSignInPresenter by lazy {
        findViewById(R.id.itin_sign_in_presenter) as ItinSignInPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_guest_itin_activity)

        mSignInPresenter.showAddGuestItinScreen()

        // Finish activity when sync is successful without any errors
        mSignInPresenter.syncFinishedWithoutErrorsSubject.subscribe {
            finish()
        }

        OmnitureTracking.trackFindGuestItin()
    }

    override fun onResume() {
        super.onResume()
        ItineraryManager.getInstance().addSyncListener(mSignInPresenter.syncListenerAdapter)
    }

    override fun onPause() {
        super.onPause()
        ItineraryManager.getInstance().removeSyncListener(mSignInPresenter.syncListenerAdapter)
    }
}