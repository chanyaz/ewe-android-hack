package com.expedia.bookings.presenter.trips

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInWidget: ItinSignInWidget by bindView(R.id.sign_in_widget)
    val addGuestItinWidget: AddGuestItinWidget by bindView(R.id.add_guest_itin_widget)
    val syncListenerAdapter = createSyncAdapter()

    private val defaultTransition = object : Presenter.DefaultTransition(ItinSignInWidget::class.java.name) {
        override fun endTransition(forward: Boolean) {
            signInWidget.visibility = View.VISIBLE
        }
    }

    private val signInToAddGuestTransition = object : VisibilityTransition(this, ItinSignInWidget::class.java, AddGuestItinWidget::class.java) {
    }

    init {
        Ui.getApplication(context).defaultTripComponents()
        View.inflate(context, R.layout.itin_sign_in_presenter, this)
        addDefaultTransition(defaultTransition)
        addTransition(signInToAddGuestTransition)
        show(signInWidget)
        signInWidget.viewModel.addGuestItinClickSubject.subscribe {
            show(signInWidget)
            show(addGuestItinWidget)
        }
    }

    inner class createSyncAdapter() : ItineraryManager.ItinerarySyncAdapter() {

        override fun onSyncFailure(error: ItineraryManager.SyncError?) {
            signInWidget.viewModel.syncFailure(error)
        }

        override fun onSyncFinished(trips: MutableCollection<Trip>?) {
            signInWidget.viewModel.syncError(trips)
        }

    }
}
