package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import rx.subjects.BehaviorSubject
import kotlin.collections.last

public class FlightViewModel(private val context: Context, private val flight: FlightLeg) {
    val resources = context.resources

    val flightTimeObserver = BehaviorSubject.create(getFlightDepartureArivalTimeAndDays(flight))
    val priceObserver = BehaviorSubject.create(if (flight.packageOfferModel.price.deltaPositive) ("+" + flight.packageOfferModel.price.differentialPriceFormatted) else flight.packageOfferModel.price.differentialPriceFormatted)
    val airlineObserver = BehaviorSubject.create(flight.carrierName)
    val durationObserver = BehaviorSubject.create(resources.getString(R.string.flight_duration_template, flight.elapsedDays, flight.durationHour, flight.durationMinute, 0))
    val airportsObserver = BehaviorSubject.create(getAllAirports(flight))

    public fun getAllAirports(flight: FlightLeg): String {
        var airports : String = ""
        for (segment in flight.flightSegments) {
            airports += segment.departureAirportCode + " "
        }
        airports += flight.flightSegments.last().arrivalAirportCode
        return airports
    }

    public fun getFlightDepartureArivalTimeAndDays(flight: FlightLeg): String {
        if (flight.elapsedDays > 0) {
            return resources.getString(R.string.flight_departure_arrival_time_multi_day_template, flight.departureTimeShort, flight.arrivalTimeShort, flight.elapsedDays)
        }
        return resources.getString(R.string.flight_departure_arrival_time_template, flight.departureTimeShort, flight.arrivalTimeShort)

    }
}
