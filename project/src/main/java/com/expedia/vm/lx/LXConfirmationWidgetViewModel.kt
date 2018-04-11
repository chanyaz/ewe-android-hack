package com.expedia.vm.lx

import android.content.Context
import android.net.Uri
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.LXState
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

    init {
        itinDetailsResponseObservable.subscribe { response ->
            val itinDetails = response.getResponseDataForItin()!!
            dateTextObservable.onNext(itinDetails.startTime.localizedFullDate)
            val email = getEmailFromURL(itinDetails.webDetailsURL)
            emailTextObservable.onNext(email)
            if (email == context.getString(R.string.lx_default_when_no_email)) {
                confirmationTextObservable.onNext(context.getString(R.string.lx_successful_checkout_no_email_label))
            } else {
                confirmationTextObservable.onNext(context.getString(R.string.lx_successful_checkout_email_label))
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

    private fun getEmailFromURL(url: String?): String {
        val uri = Uri.parse(url)
        val email = uri.getQueryParameter("email")
        if (email.isNullOrEmpty()) {
            return context.getString(R.string.lx_default_when_no_email)
        }
        return email
    }
}
