package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class ManageBookingFlightItinViewModel(val context: Context) {
    lateinit var itinCardDataFlight: ItinCardDataFlight

    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val updateToolbarSubject = PublishSubject.create<ItinToolbarViewModel.ToolbarParams>()

    fun setUp(itinId: String) {
        updateItinCardDataFlight(itinId)
    }

    private fun updateToolbar() {
        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", itinCardDataFlight.flightLeg.lastWaypoint.airport.mCity ?: "").format().toString()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    private fun updateItinCardDataFlight(itinId: String) {
        val freshItinCardDataFlight = ItineraryManager.getInstance().getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlight = freshItinCardDataFlight
            updateToolbar()
        }
    }
}