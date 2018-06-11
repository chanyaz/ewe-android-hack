package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.util.HotelFavoritesCache.Companion.isFavoriteHotel
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HotelInfoToolbarViewModel(val context: Context) {
    val hotelSoldOut = BehaviorSubject.createDefault<Boolean>(false)
    val hotelNameObservable = PublishSubject.create<String>()
    var hotelRatingObservable = PublishSubject.create<Float>()
    var hotelRatingContentDescriptionObservable = PublishSubject.create<String>()
    var hotelRatingObservableVisibility = BehaviorSubject.createDefault<Boolean>(false)
    var hotelFavoriteIconResIdObserver = PublishSubject.create<Int>()
    var hotelFavoriteIconVisibilityObserver = BehaviorSubject.createDefault<Boolean>(false)
    var favoriteClickObserver = BehaviorSubject.create<Unit>()
    var favoriteToggledObserver = BehaviorSubject.create<Boolean>()
    private var offerResponse: HotelOffersResponse? = null

    init {
        favoriteClickObserver.subscribe { toggleFavoriteIcon() }
    }

    fun bind(offerResponse: HotelOffersResponse, showFavoriteIcon: Boolean = false) {
        var soldOut = CollectionUtils.isEmpty(offerResponse.hotelRoomResponse)
        bind(offerResponse.hotelName, offerResponse.hotelStarRating.toFloat(), soldOut, showFavoriteIcon)
        this.offerResponse = offerResponse
        hotelFavoriteIconResIdObserver.onNext(getFavoriteImageDrawableId(offerResponse.hotelId))
    }

    fun bind(hotelName: String, hotelStarRating: Float, soldOut: Boolean, showFavoriteIcon: Boolean = false) {
        hotelSoldOut.onNext(soldOut)
        hotelNameObservable.onNext(hotelName)
        hotelRatingObservable.onNext(hotelStarRating)
        hotelRatingContentDescriptionObservable.onNext(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, hotelStarRating.toDouble()))
        hotelRatingObservableVisibility.onNext(hotelStarRating > 0)
        hotelFavoriteIconVisibilityObserver.onNext(showFavoriteIcon)
    }

    private fun getFavoriteImageDrawableId(hotelId: String): Int {
        if (isFavoriteHotel(context, hotelId)) {
            return R.drawable.ic_favorite_active
        }
        return R.drawable.ic_favorite_inactive
    }

    private fun toggleFavoriteIcon() {
        if (offerResponse == null) {
            return
        }
        val isFavoriteNow = !isFavoriteHotel(context, offerResponse?.hotelId!!)
        if (isFavoriteNow) {
            hotelFavoriteIconResIdObserver.onNext(R.drawable.ic_favorite_active)
        } else {
            hotelFavoriteIconResIdObserver.onNext(R.drawable.ic_favorite_inactive)
        }
        favoriteToggledObserver.onNext(isFavoriteNow)
    }
}
