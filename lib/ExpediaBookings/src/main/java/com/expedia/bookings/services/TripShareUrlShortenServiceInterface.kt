package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

interface TripShareUrlShortenServiceInterface {
    fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Disposable
}
