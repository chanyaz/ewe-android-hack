package com.expedia.bookings.itin.vm

import io.reactivex.subjects.PublishSubject

class FlightItinModifyReservationViewModel {

    data class FlightItinModifyReservationWidgetParams(
            val changeReservationURL: String,
            val isChangeable: Boolean,
            val cancelReservationURL: String,
            val isCancellable: Boolean,
            val customerSupportNumber: String
    )

    val modifyReservationSubject = PublishSubject.create<FlightItinModifyReservationWidgetParams>()

}
