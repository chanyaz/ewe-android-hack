package com.expedia.bookings.itin.activity

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.presenter.trips.AddGuestItinWidget
import com.expedia.bookings.presenter.trips.ItinFetchProgressWidget
import com.expedia.bookings.tracking.AdTracker
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClearPrivateDataUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import com.squareup.otto.Produce
import javax.inject.Inject

class NewAddGuestItinActivity : AppCompatActivity(), AboutUtils.CountrySelectDialogListener {

    lateinit var pointOfSaleStateModel: PointOfSaleStateModel
        @Inject set

    private val presenter: Presenter by lazy {
        findViewById(R.id.guest_itin_presenter) as Presenter
    }

    @VisibleForTesting
    val addGuestItinWidget: AddGuestItinWidget by lazy {
        findViewById(R.id.add_guest_itin_widget) as AddGuestItinWidget
    }

    private val guestItinProgressWidget: ItinFetchProgressWidget by lazy {
        findViewById(R.id.guest_itin_progress_widget) as ItinFetchProgressWidget
    }

    private var hasAddGuestItinErrors = false
    @VisibleForTesting
    var isSyncCalledFromHere = false
    @VisibleForTesting
    val syncListenerAdapter = createSyncAdapter()

    private val guestItinToProgressTransition by lazy {
        object : VisibilityTransition(presenter, AddGuestItinWidget::class.java, ItinFetchProgressWidget::class.java) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.add_guest_itin_activity)

        OmnitureTracking.trackFindGuestItin()
        Ui.getApplication(this).tripComponent().inject(this)

        presenter.addTransition(guestItinToProgressTransition)
        presenter.show(addGuestItinWidget)

        addGuestItinWidget.viewModel.showItinFetchProgressObservable.subscribe {
            hasAddGuestItinErrors = false
            isSyncCalledFromHere = true
            presenter.show(guestItinProgressWidget)
        }
    }

    override fun onResume() {
        super.onResume()
        ItineraryManager.getInstance().addSyncListener(syncListenerAdapter)
        Events.register(this)
        AccessibilityUtil.delayFocusToToolbarNavigationIcon(addGuestItinWidget.toolbar, 300)
    }

    override fun onPause() {
        super.onPause()
        Events.unregister(this)
        ItineraryManager.getInstance().removeSyncListener(syncListenerAdapter)
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
        OmnitureTracking.trackItinChangePOS()
    }

    override fun showDialogFragment(dialog: DialogFragment) {
        dialog.show(supportFragmentManager, "dialog_from_about_utils")
    }

    @VisibleForTesting
    inner class createSyncAdapter : ItineraryManager.ItinerarySyncAdapter() {
        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            if (!hasAddGuestItinErrors && isSyncCalledFromHere) {
                finish()
            }
        }

        override fun onTripFailedFetchingGuestItinerary() {
            hasAddGuestItinErrors = true
            presenter.show(addGuestItinWidget)
        }

        override fun onTripFailedFetchingRegisteredUserItinerary() {
            hasAddGuestItinErrors = true
            presenter.show(addGuestItinWidget)
        }
    }
}