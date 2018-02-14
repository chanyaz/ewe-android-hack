package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
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

    fun bind(offerResponse: HotelOffersResponse) {
        var soldOut = CollectionUtils.isEmpty(offerResponse.hotelRoomResponse)
        bind(offerResponse.hotelName, offerResponse.hotelStarRating.toFloat(), soldOut)
    }

    fun bind(hotelName: String, hotelStarRating: Float, soldOut: Boolean) {
        hotelSoldOut.onNext(soldOut)
        hotelNameObservable.onNext(hotelName)
        hotelRatingObservable.onNext(hotelStarRating)
        hotelRatingContentDescriptionObservable.onNext(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, hotelStarRating.toDouble()))
        hotelRatingObservableVisibility.onNext(hotelStarRating > 0)
    }
}
