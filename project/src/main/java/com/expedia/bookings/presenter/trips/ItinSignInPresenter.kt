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
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInWidget: ItinSignInWidget by bindView(R.id.sign_in_widget)
    val addGuestItinWidget: AddGuestItinWidget by bindView(R.id.add_guest_itin_widget)
    val itinFetchProgressWidget: ItinFetchProgressWidget by bindView(R.id.itin_fetch_progress_widget)
    val syncListenerAdapter = createSyncAdapter()

    private var hasAddGuestItinErrors = false
    var syncFinishedWithoutErrorsSubject = PublishSubject.create<Unit>()

    private val defaultTransition = object : Presenter.DefaultTransition(ItinSignInWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = View.VISIBLE
            signInWidget.itinPOSHeader.setCurrentPOS()
        }
    }

    private val signInToAddGuestTransition = object : VisibilityTransition(this, ItinSignInWidget::class.java, AddGuestItinWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(addGuestItinWidget.toolbar, 300)
                addGuestItinWidget.itinPOSHeader.setCurrentPOS()
            }
            else {
                signInWidget.itinPOSHeader.setCurrentPOS()
            }
        }
    }
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
            showAddGuestItinScreen(false)
            OmnitureTracking.trackFindGuestItin()
        }

        addGuestItinWidget.viewModel.showItinFetchProgressObservable.subscribe {
            showItinFetchProgress()
        }
        signInWidget.viewModel.syncItinManagerSubject.subscribe {
            OmnitureTracking.trackItinRefresh()
            showItinFetchProgress()
        }
    }

    private fun showItinFetchProgress() {
        hasAddGuestItinErrors = false
        show(itinFetchProgressWidget)
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(true)
    }

    fun showAddGuestItinScreen(hasError: Boolean = false) {
        if (currentState == null) {
            showSignInWidget()
        }

        show(addGuestItinWidget, Presenter.FLAG_CLEAR_TOP)
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(false)
        if (!hasError) {
            addGuestItinWidget.resetFields()
            addGuestItinWidget.viewModel.emailFieldFocusObservable.onNext(Unit)
        }
    }

    fun showSignInWidget() {
        show(signInWidget, Presenter.FLAG_CLEAR_TOP)
        addGuestItinWidget.viewModel.toolBarVisibilityObservable.onNext(true)
    }

    inner class createSyncAdapter() : ItineraryManager.ItinerarySyncAdapter() {

        override fun onSyncFailure(error: ItineraryManager.SyncError?) {
            signInWidget.viewModel.syncFailure(error)
        }

        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            signInWidget.viewModel.syncError(trips)
            if (currentState != addGuestItinWidget.javaClass.name){
                showSignInWidget()
            }
            if (!hasAddGuestItinErrors) {
                syncFinishedWithoutErrorsSubject.onNext(Unit)
            }
        }

        override fun onTripFailedFetchingGuestItinerary() {
            hasAddGuestItinErrors = true
            showAddGuestItinScreen(true)
        }

        override fun onTripFailedFetchingRegisteredUserItinerary() {
            hasAddGuestItinErrors = true
            showAddGuestItinScreen(true)
        }
    }
}
