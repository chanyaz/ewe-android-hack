package com.expedia.bookings.presenter.trips

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.itin.ItinPageUsableTracking
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import javax.inject.Inject

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInWidget: ItinSignInWidget by bindView(R.id.sign_in_widget)
    val itinFetchProgressWidget: ItinFetchProgressWidget by bindView(R.id.itin_fetch_progress_widget)
    val syncListenerAdapter = createSyncAdapter()

    lateinit var itinPageUsablePerformanceModel: ItinPageUsableTracking
        @Inject set

    private val defaultTransition = object : Presenter.DefaultTransition(ItinSignInWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = View.VISIBLE
        }
    }

    private val signInToProgressTransition = object : VisibilityTransition(this, ItinSignInWidget::class.java, ItinFetchProgressWidget::class.java) {}

    init {
        Ui.getApplication(context).defaultTripComponents()
        Ui.getApplication(context).tripComponent().inject(this)
        View.inflate(context, R.layout.itin_sign_in_presenter, this)
        addDefaultTransition(defaultTransition)
        addTransition(signInToProgressTransition)
        showSignInWidget()
        signInWidget.viewModel.addGuestItinClickSubject.subscribe {
            showAddGuestItinScreen()
            OmnitureTracking.trackFindGuestItin()
        }

        signInWidget.viewModel.syncItinManagerSubject.subscribe {
            OmnitureTracking.trackItinRefresh()
            itinPageUsablePerformanceModel.markSuccessfulStartTime(System.currentTimeMillis())
            showItinFetchProgress()
        }
    }

    fun showAddGuestItinScreen() {
        if (currentState == null) {
            showSignInWidget()
        }

        val intent = Intent(context, NewAddGuestItinActivity::class.java)
        context.startActivity(intent)
    }

    fun showSignInWidget() {
        show(signInWidget, Presenter.FLAG_CLEAR_TOP)
    }

    fun showItinFetchProgress() {
        show(itinFetchProgressWidget)
    }

    inner class createSyncAdapter : ItineraryManager.ItinerarySyncAdapter() {
        override fun onSyncFailure(error: ItineraryManager.SyncError?) {
            signInWidget.viewModel.syncFailure()
        }

        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            signInWidget.viewModel.newTripsUpdateState(trips)
            showSignInWidget()
        }
    }
}
