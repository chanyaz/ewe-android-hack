package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.trips.TicketingStatus
import io.reactivex.subjects.PublishSubject

abstract class ItinConfirmationViewModel {
    data class WidgetParams(
            val confirmationStatus: TicketingStatus,
            val confirmationNumbers: CharSequence
    )

    val widgetConfirmationStatusSubject: PublishSubject<CharSequence> = PublishSubject.create<CharSequence>()
    val widgetConfirmationNumbersSubject: PublishSubject<CharSequence> = PublishSubject.create<CharSequence>()

    abstract fun updateWidget(widgetParams: WidgetParams)
}