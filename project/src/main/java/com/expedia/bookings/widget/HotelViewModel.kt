package com.expedia.bookings.widget

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.Images
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

public class HotelViewModel(private val hotel: Hotel, private val context: Context) {
    val resources = context.getResources()
    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceObservable = BehaviorSubject.create(priceFormatter(hotel, false))
    val hotelStrikeThroughPriceObservable = BehaviorSubject.create(priceFormatter(hotel, true))
    val hasDiscountObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.discountPercent < 0)
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating.toString())
    val hotelPreviewRatingObservable = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val hotelThumbnailUrlObservable = BehaviorSubject.create(Images.getMediaHost() + hotel.thumbnailUrl)
    val pricePerNightObservable = BehaviorSubject.create(priceFormatter(hotel, false))
    val guestRatingPercentageObservable = BehaviorSubject.create(Phrase.from(resources, R.string.customer_rating_percent_Template).put("rating", hotel.percentRecommended.toInt()).put("percent", "%").format().toString())
    val topAmenityTitleObservable = BehaviorSubject.create(if (hotel.isSponsoredListing) resources.getString(R.string.sponsored) else if (hotel.hasFreeCancellation) resources.getString(R.string.free_cancellation) else "")
    val hotelStarRatingObservable = BehaviorSubject.create(hotel.hotelStarRating)
    val hotelLargeThumbnailUrlObservable = BehaviorSubject.create(Images.getMediaHost() + hotel.largeThumbnailUrl)
    val hotelDiscountPercentageObservable = BehaviorSubject.create(Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo.discountPercent.toInt()).format().toString())

    init {
        if (hotel.isSponsoredListing && !hotel.hasShownImpression) {
            hotel.hasShownImpression = true
            AdImpressionTracking.trackAdClickOrImpression(context, hotel.impressionTrackingUrl, null)
        }
    }
}