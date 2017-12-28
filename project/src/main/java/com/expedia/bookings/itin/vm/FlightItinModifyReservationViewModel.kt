package com.expedia.bookings.itin.vm

import rx.subjects.PublishSubject

class FlightItinModifyReservationViewModel {

    data class FlightItinModifyReservationWidgetParams(
            val changeReservationURL: String,
            val isChangeable: Boolean,
            val cancelReservationURL: String,
            val isCancellable: Boolean
    )
    val modifyReservationSubject = PublishSubject.create<FlightItinModifyReservationWidgetParams>()

}