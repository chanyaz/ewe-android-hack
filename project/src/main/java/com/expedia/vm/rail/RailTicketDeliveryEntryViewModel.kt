package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailTicketDeliveryOption
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.widget.rail.TicketDeliveryMethod
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailTicketDeliveryEntryViewModel(val context: Context) {
    //inputs
    val ticketDeliveryOptions = PublishSubject.create<List<RailTicketDeliveryOption>>()

    //ouputs
    val ticketDeliveryByPostOptions = ticketDeliveryOptions.map { options ->
        options.filter { option ->
            CollectionUtils.isNotEmpty(option.ticketDeliveryCountryCodeList) && option.ticketDeliveryCountryCodeList.contains("GB")
        }
    }

    val deliveryByMailSupported = ticketDeliveryByPostOptions.map { CollectionUtils.isNotEmpty(it) }
    val ticketDeliveryObservable = BehaviorSubject.create<TicketDeliveryMethod>(TicketDeliveryMethod.PICKUP_AT_STATION)
    val ticketDeliveryMethodSelected = PublishSubject.create<TicketDeliveryMethod>()
    var ticketDeliveryOption: TicketDeliveryOption? = null
    val ticketDeliveryOptionSubject = BehaviorSubject.create<TicketDeliveryOption>()

    init {
        deliveryByMailSupported.subscribe { supported ->
            if (!supported) {
                ticketDeliveryObservable.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
                ticketDeliveryMethodSelected.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
                ticketDeliveryOption = TicketDeliveryOption(RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE)
                ticketDeliveryOptionSubject.onNext(ticketDeliveryOption)
            }
        }
    }
}
