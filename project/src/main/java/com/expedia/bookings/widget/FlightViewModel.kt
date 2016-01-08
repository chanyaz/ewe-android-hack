package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.data.packages.FlightLeg
import rx.subjects.BehaviorSubject

public class FlightViewModel(private val context: Context, private val flight: FlightLeg) {
    val resources = context.resources

    val airlineObserver = BehaviorSubject.create(flight.carrierName)

}
