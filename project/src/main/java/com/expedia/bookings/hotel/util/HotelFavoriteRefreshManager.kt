package com.expedia.bookings.hotel.util

import android.content.Context
import rx.subjects.PublishSubject

class HotelFavoriteRefreshManager(private val hotelInfoManager: HotelInfoManager,
                                  private val fromIntentService: Boolean = false) {
    val allHotelsRefreshedSubject = PublishSubject.create<Unit>()

    private var hotelsRefreshed = 0

    fun refreshHotels(context: Context) {
        hotelsRefreshed = 0

        val favorites = HotelFavoriteCache.getFavorites(context)

        hotelInfoManager.offerSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(context, response, true)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                allHotelsRefreshedSubject.onNext(Unit)
            }
        }

        hotelInfoManager.infoSuccessSubject.subscribe { response ->
            HotelFavoriteCache.saveHotelData(context, response)
            hotelsRefreshed++

            if (hotelsRefreshed == favorites.size) {
                allHotelsRefreshedSubject.onNext(Unit)
            }
        }

        val checkInDate = HotelFavoriteCache.getCheckInDate(context)
        val checkOutDate = HotelFavoriteCache.getCheckOutDate(context)

        if (checkInDate != null && checkOutDate != null) {
            if (fromIntentService) {
                hotelInfoManager.fetchOffersFromIntentService(checkInDate, checkOutDate, favorites)
            } else {
                hotelInfoManager.fetchOffers(checkInDate, checkOutDate, favorites)
            }
        } else {
            hotelInfoManager.fetchDatelessInfo(favorites)
        }
    }
}