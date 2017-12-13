package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.utils.HotelsV2DataUtil
import rx.subjects.BehaviorSubject

class HotelInfoToolbarViewModel(val context: Context, val hotelName: String,
                                val hotelStarRating: Float, soldOut: Boolean) {
    val hotelSoldOut = BehaviorSubject.create<Boolean>(soldOut)
    val toolBarRatingColor = hotelSoldOut.map { if (it) ContextCompat.getColor(context, android.R.color.white) else ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color) }
    val hotelNameObservable = BehaviorSubject.create<String>(hotelName)
    val hotelRatingObservable = BehaviorSubject.create<Float>(hotelStarRating)
    val hotelRatingContentDescriptionObservable = BehaviorSubject.create<String>(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, hotelStarRating.toDouble()))
    val hotelRatingObservableVisibility = BehaviorSubject.create<Boolean>(hotelStarRating > 0)
}

