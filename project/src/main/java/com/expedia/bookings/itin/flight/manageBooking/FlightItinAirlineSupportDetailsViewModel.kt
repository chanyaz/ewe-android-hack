package com.expedia.bookings.itin.flight.manageBooking

import io.reactivex.subjects.PublishSubject

class FlightItinAirlineSupportDetailsViewModel {

    data class FlightItinAirlineSupportDetailsWidgetParams(
        val title: String,
        val airlineSupport: String,
        val ticket: String,
        val confirmation: String,
        val itinerary: String,
        val callSupport: String,
        val siteSupportText: String,
        val siteSupportURL: String
    )

    val airlineSupportDetailsWidgetSubject: PublishSubject<FlightItinAirlineSupportDetailsWidgetParams> = PublishSubject.create<FlightItinAirlineSupportDetailsWidgetParams>()
}
