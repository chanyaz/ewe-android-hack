package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TicketingStatus

class FlightItinConfirmationViewModel(private val context: Context) : ItinConfirmationViewModel() {
    override fun updateWidget(widgetParams: WidgetParams) {
        val status = updateConfirmationStatus(widgetParams.confirmationStatus)
        widgetConfirmationNumbersSubject.onNext(widgetParams.confirmationNumbers)
        widgetConfirmationStatusSubject.onNext(status)
    }
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateConfirmationStatus(ticketingStatus: TicketingStatus): CharSequence = when (ticketingStatus) {
        TicketingStatus.CANCELLED -> context.getString(R.string.flight_itin_cancelled_status_label)
        TicketingStatus.VOIDED -> context.getString(R.string.flight_itin_cancelled_status_label)
        TicketingStatus.INPROGRESS -> context.getString(R.string.flight_itin_in_progress_status_label)
        else -> context.getString(R.string.flight_itin_confirmation_status_label)
    }
}
