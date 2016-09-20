package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.widget.rail.TicketDeliveryMethod
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailTicketDeliveryEntryViewModel(val context: Context) {
    //inputs
    val deliveryByMailSupported = PublishSubject.create<Boolean>()

    //ouputs
    val ticketDeliveryObservable = BehaviorSubject.create<TicketDeliveryMethod>(TicketDeliveryMethod.PICKUP_AT_STATION)
    val ticketDeliveryMethodSelected = BehaviorSubject.create<TicketDeliveryMethod>()

    init {
        deliveryByMailSupported.subscribe { supported ->
            if (!supported) {
                ticketDeliveryObservable.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
                ticketDeliveryMethodSelected.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
            }
        }
    }
}
