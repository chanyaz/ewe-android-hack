package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import rx.Observer
import rx.Subscription

interface TripShareUrlShortenServiceInterface {
    fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Subscription
}
