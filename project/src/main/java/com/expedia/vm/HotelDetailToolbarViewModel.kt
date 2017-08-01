package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.utils.HotelsV2DataUtil
import io.reactivex.subjects.BehaviorSubject

class HotelDetailToolbarViewModel(val context: Context, val hotelName: String, val hotelStarRating: Float, soldOut: Boolean) {
    val hotelSoldOut = BehaviorSubject.createDefault<Boolean>(soldOut)
    val toolBarRatingColor = hotelSoldOut.map { if (it) ContextCompat.getColor(context, android.R.color.white) else ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color) }
    val hotelNameObservable = BehaviorSubject.createDefault<String>(hotelName)
    val hotelRatingObservable = BehaviorSubject.createDefault<Float>(hotelStarRating)
    val hotelRatingContentDescriptionObservable = BehaviorSubject.createDefault<String>(HotelsV2DataUtil.getHotelRatingContentDescription(context, hotelStarRating.toInt()))
    val hotelRatingObservableVisibility = BehaviorSubject.createDefault<Boolean>(hotelStarRating > 0)
}

