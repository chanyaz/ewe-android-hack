package com.expedia.vm

import com.expedia.bookings.data.flights.KrazyglueResponse
import io.reactivex.subjects.PublishSubject

class KrazyglueHotelViewHolderViewModel {
    val hotelNameObservable = PublishSubject.create<String>()
    val hotelStarRatingObservable = PublishSubject.create<Float>()
    val hotelStarRatingVisibilityObservable = hotelStarRatingObservable.map { it > 0 }
    val hotelGuestRatingObservable = PublishSubject.create<String>()
    val hotelGuestRatingVisibilityObservable = hotelGuestRatingObservable.map { it.toFloat() > 0 }
    val hotelStrikeThroughPriceObservable = PublishSubject.create<String>()
    val hotelPricePerNightObservable = PublishSubject.create<String>()
    val hotelImageURLObservable = PublishSubject.create<String>()
    val hotelObservable = PublishSubject.create<KrazyglueResponse.KrazyglueHotel>()
    var hotelId: String? = null

    init {
        hotelObservable.subscribe { hotel ->
            hotelNameObservable.onNext(hotel.hotelName)
            hotelImageURLObservable.onNext(hotel.hotelImage)
            hotelStarRatingObservable.onNext(hotel.starRating.toFloat())
            hotelGuestRatingObservable.onNext(hotel.guestRating)
            hotelPricePerNightObservable.onNext(hotel.airAttachedPrice)
            hotelStrikeThroughPriceObservable.onNext(hotel.standAlonePrice)
            hotelId = hotel.hotelId
        }
    }
}
