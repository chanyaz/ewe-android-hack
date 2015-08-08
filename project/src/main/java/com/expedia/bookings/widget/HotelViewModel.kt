package com.expedia.bookings.widget

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.Images
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

public class HotelViewModel(private val hotel: Hotel, private val resources: Resources) {
    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceObservable = BehaviorSubject.create(priceFormatter(hotel, false))
    val hotelStrikeThroughPriceObservable = BehaviorSubject.create(priceFormatter(hotel, true))
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating.toString())
    val hotelPreviewRatingObservable = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val hotelThumbnailUrlObservable = BehaviorSubject.create(Images.getMediaHost() + hotel.thumbnailUrl)
    val pricePerNightObservable = BehaviorSubject.create(Phrase.from(resources, R.string.per_nt_TEMPLATE).put("price", hotel.lowRateInfo.nightlyRateTotal.toString()).format().toString())
    val guestRatingPercentageObservable = BehaviorSubject.create(Phrase.from(resources, R.string.customer_rating_percent_Template).put("rating", hotel.percentRecommended.toInt()).put("percent", "%").format().toString())
    val topAmenityTitleObservable = BehaviorSubject.create(if (hotel.hasFreeCancellation) resources.getString(R.string.free_cancellation) else "")
    val hotelStarRatingObservable = BehaviorSubject.create(hotel.hotelStarRating)
    val hotelLargeThumbnailUrlObservable = BehaviorSubject.create(Images.getMediaHost() + hotel.largeThumbnailUrl)
}