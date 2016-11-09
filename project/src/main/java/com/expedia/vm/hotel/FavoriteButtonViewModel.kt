package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.tracking.HotelTracking
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FavoriteButtonViewModel(private val context: Context, val hotelId: String, private val hotelTracking: HotelTracking, private val pageName: HotelTracking.PageName) {

    val clickSubject = PublishSubject.create<Unit>()
    val favoriteChangeSubject = PublishSubject.create<Pair<String, Boolean>>()
    val firstTimeFavoritingSubject = BehaviorSubject.create<Boolean>(HotelFavoriteHelper.isFirstTimeFavoriting(context))

    init {
        clickSubject.subscribe {
            firstTimeFavoritingSubject.onNext(HotelFavoriteHelper.isFirstTimeFavoriting(context))
            HotelFavoriteHelper.toggleHotelFavoriteState(context, hotelId)
            val isFavorite = HotelFavoriteHelper.isHotelFavorite(context, hotelId)
            hotelTracking.trackHotelFavoriteClick(hotelId, isFavorite, pageName)
            favoriteChangeSubject.onNext(Pair(hotelId, isFavorite))
        }

    }
}