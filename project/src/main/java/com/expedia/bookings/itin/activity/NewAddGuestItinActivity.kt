package com.expedia.bookings.itin.activity

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.trips.ItinSignInPresenter
import com.expedia.bookings.tracking.AdTracker
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.ClearPrivateDataUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import com.squareup.otto.Produce
import javax.inject.Inject

class NewAddGuestItinActivity : AppCompatActivity(), AboutUtils.CountrySelectDialogListener {

    lateinit var pointOfSaleStateModel: PointOfSaleStateModel
        @Inject set

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
        Ui.getApplication(this).tripComponent().inject(this)
    }

    override fun onResume() {
        super.onResume()
        ItineraryManager.getInstance().addSyncListener(mSignInPresenter.syncListenerAdapter)
        Events.register(this)
    }

    override fun onPause() {
        super.onPause()
        ItineraryManager.getInstance().removeSyncListener(mSignInPresenter.syncListenerAdapter)
    }

    override fun onStop() {
        super.onStop()
        Events.unregister(this)
    }

    @Produce
    fun postPOSChangeEvent(): Events.PhoneLaunchOnPOSChange {
        return Events.PhoneLaunchOnPOSChange()
    }

    override fun onNewCountrySelected(pointOfSaleId: Int) {
        SettingUtils.save(this, R.string.PointOfSaleKey, Integer.toString(pointOfSaleId))

        ClearPrivateDataUtil.clear(this)
        PointOfSale.onPointOfSaleChanged(this)
        AdTracker.updatePOS()

        setResult(Constants.RESULT_CHANGED_PREFS)
        postPOSChangeEvent()

        Toast.makeText(this, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show()

        pointOfSaleStateModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
    }

    override fun showDialogFragment(dialog: DialogFragment) {
        dialog.show(supportFragmentManager, "dialog_from_about_utils")
    }
}