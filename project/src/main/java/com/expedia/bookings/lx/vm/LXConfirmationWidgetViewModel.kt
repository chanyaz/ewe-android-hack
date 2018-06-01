package com.expedia.bookings.lx.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.Ui
import io.reactivex.subjects.PublishSubject

class LXConfirmationWidgetViewModel(val context: Context) {

    val titleTextObservable = PublishSubject.create<String>()
    val ticketsTextObservable = PublishSubject.create<String>()
    val locationTextObservable = PublishSubject.create<String>()
    val dateTextObservable = PublishSubject.create<String>()
    val emailTextObservable = PublishSubject.create<String>()
    val confirmationTextObservable = PublishSubject.create<String>()
    val reservationConfirmationTextObservable = PublishSubject.create<String>()
    val itineraryNumberTextObservable = PublishSubject.create<String>()

    val itinDetailsResponseObservable = PublishSubject.create<AbstractItinDetailsResponse>()
    val lxStateObservable = PublishSubject.create<LXState>()
    val confirmationScreenUiObservable = PublishSubject.create<Unit>()
    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        itinDetailsResponseObservable.subscribe { response ->
            val itinDetails = response.getResponseDataForItin()!!
            dateTextObservable.onNext(itinDetails.startTime.localizedFullDate)
            val email = itinDetails.email?.address ?: context.getString(R.string.lx_default_when_no_email)
            emailTextObservable.onNext(email)
            if (email == context.getString(R.string.lx_default_when_no_email)) {
                confirmationTextObservable.onNext(context.getString(R.string.lx_successful_checkout_no_email_label))
            } else {
                confirmationTextObservable.onNext(context.getString(R.string.lx_successful_checkout_email_label))
            }
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinDetails.tripNumber.toString())
            }
            reservationConfirmationTextObservable.onNext(context.getString(R.string.lx_successful_checkout_reservation_label))
            itineraryNumberTextObservable.onNext(context.getString(R.string.successful_checkout_TEMPLATE, itinDetails.tripNumber))
        }
        lxStateObservable.subscribe { lxState ->
            titleTextObservable.onNext(lxState.activity.title)
            ticketsTextObservable.onNext(lxState.selectedTicketsCountSummary(context))
            locationTextObservable.onNext(lxState.activity.location)
        }
    }
}
