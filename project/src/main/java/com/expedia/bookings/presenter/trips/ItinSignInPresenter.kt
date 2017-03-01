package com.expedia.bookings.presenter.trips

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInWidget: ItinSignInWidget by bindView(R.id.sign_in_widget)
    val addGuestItinWidget: AddGuestItinWidget by bindView(R.id.add_guest_itin_widget)
    val itinFetchProgressWidget: ItinFetchProgressWidget by bindView(R.id.itin_fetch_progress_widget)
    val syncListenerAdapter = createSyncAdapter()

    private val defaultTransition = object : Presenter.DefaultTransition(ItinSignInWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = View.VISIBLE
        }
    }

    private val signInToAddGuestTransition = object : VisibilityTransition(this, ItinSignInWidget::class.java, AddGuestItinWidget::class.java) {}
    private val addGuestToProgressTransition = object : VisibilityTransition(this, AddGuestItinWidget::class.java, ItinFetchProgressWidget::class.java) {}
    private val signInToProgressTransition = object : VisibilityTransition(this, ItinSignInWidget::class.java, ItinFetchProgressWidget::class.java) {}

    init {
        Ui.getApplication(context).defaultTripComponents()
        View.inflate(context, R.layout.itin_sign_in_presenter, this)
        addDefaultTransition(defaultTransition)
        addTransition(signInToAddGuestTransition)
        addTransition(addGuestToProgressTransition)
        addTransition(signInToProgressTransition)
        showSignInWidget()
        signInWidget.viewModel.addGuestItinClickSubject.subscribe {
            println("Supreeth addGuestItinClickSubject")
            showAddGuestItinScreen()
            addGuestItinWidget.viewModel.emailFieldFocusObservable.onNext(Unit)
        }

        addGuestItinWidget.viewModel.showItinFetchProgressObservable.subscribe {
            println("Supreeth showItinFetchProgressObservable")
            showItinFetchProgress()
        }
        signInWidget.viewModel.syncItinManagerSubject.subscribe {
            println("Supreeth syncItinManagerSubject")
            showItinFetchProgress()
        }
    }

    private fun showItinFetchProgress() {
        show(itinFetchProgressWidget)
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(true)
    }

    fun showAddGuestItinScreen() {
        if (currentState == null) {
            showSignInWidget()
        }
        show(addGuestItinWidget, Presenter.FLAG_CLEAR_TOP)
        OmnitureTracking.trackFindGuestItin()
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(false)
    }

    fun showSignInWidget() {
        show(signInWidget, Presenter.FLAG_CLEAR_TOP)
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(true)
    }

    inner class createSyncAdapter() : ItineraryManager.ItinerarySyncAdapter() {

        override fun onSyncFailure(error: ItineraryManager.SyncError?) {
            println("Supreeth onSyncFailure")
            signInWidget.viewModel.syncFailure(error)
        }

        override fun onTripAdded(trip: Trip?) {
            super.onTripAdded(trip)
            println("Supreeth onTripAdded")
            show(signInWidget, Presenter.FLAG_CLEAR_TOP)
        }

        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            println("Supreeth onSyncFinished")
            signInWidget.viewModel.syncError(trips)
            if (trips?.size == 0 && currentState != addGuestItinWidget.javaClass.name){
                println("Supreeth onSyncFinished showing signInWidget")
                show(signInWidget, Presenter.FLAG_CLEAR_TOP)
            }
        }

        override fun onTripFailedFetchingGuestItinerary() {
            println("Supreeth onTripFailedFetchingGuestItinerary")
            showAddGuestItinScreen()
        }

        override fun onTripFailedFetchingRegisteredUserItinerary() {
            println("Supreeth onTripFailedFetchingRegisteredUserItinerary")
            showAddGuestItinScreen()
        }
    }
}
