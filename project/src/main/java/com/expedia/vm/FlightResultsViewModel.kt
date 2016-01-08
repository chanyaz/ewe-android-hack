package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.packages.FlightLeg
import rx.subjects.BehaviorSubject

public class FlightResultsViewModel(private val context: Context) {

    // Outputs
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()


}
