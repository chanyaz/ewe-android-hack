package com.expedia.bookings.data.trips

interface IHandleTripResponse {
    fun refreshTripResponseNull(trip: Trip)
    fun refreshTripResponseHasErrors(trip: Trip, tripDetailsResponse: TripDetailsResponse)
    fun refreshTripResponseSuccess(trip: Trip, deepRefresh: Boolean, tripDetailsResponse: TripDetailsResponse)
}
