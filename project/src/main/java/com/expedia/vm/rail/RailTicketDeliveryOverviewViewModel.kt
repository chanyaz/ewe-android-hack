package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.rail.widget.TicketDeliveryMethod
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class RailTicketDeliveryOverviewViewModel(val context: Context) {

    val iconObservable = PublishSubject.create<Int>()
    val titleObservable = PublishSubject.create<String>()
    val ticketDeliverySelectedObserver = BehaviorSubject.create<TicketDeliveryMethod>()

    init {
        ticketDeliverySelectedObserver.subscribe { selected ->
            if (selected == TicketDeliveryMethod.DELIVER_BY_MAIL) {
                iconObservable.onNext(R.drawable.ticket_delivery_cko_mail)
                titleObservable.onNext(context.getString(R.string.delivery_by_mail))
            } else {
                iconObservable.onNext(R.drawable.ticket_delivery_cko_station)
                titleObservable.onNext(context.getString(R.string.pickup_at_station))
            }
        }
    }
}
