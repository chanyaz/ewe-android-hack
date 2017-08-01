package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.widget.TicketDeliverySelectionStatus
import io.reactivex.subjects.PublishSubject

class RailTicketDeliveryOptionViewModel(val context: Context) {
    val statusChanged = PublishSubject.create<TicketDeliverySelectionStatus>()
}
