package com.expedia.bookings.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

public class HotelViewModel(private val context: Context, private val hotel: Hotel) {
    val resources = context.resources

    val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceObservable = BehaviorSubject.create(priceFormatter(hotel.lowRateInfo, false))
    val hotelStrikeThroughPriceObservable = BehaviorSubject.create(priceFormatter(hotel.lowRateInfo, true))
    val hasDiscountObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.discountPercent < 0 && !hotel.lowRateInfo.airAttached)
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating.toString())
    val hotelPreviewRatingObservable = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val pricePerNightObservable = BehaviorSubject.create(priceFormatter(hotel.lowRateInfo, false))

    val urgencyMessageObservable = BehaviorSubject.create(getMostApplicableUrgencyMessage(hotel, resources))
    val urgencyMessageVisibilityObservable = urgencyMessageObservable.map { if (it != null) it!!.visibility else false }
    val urgencyMessageBoxObservable = urgencyMessageObservable.filter { it != null }.map { it!!.message }
    val urgencyMessageBackgroundObservable = urgencyMessageObservable.filter { it != null }.map { it!!.background }
    val urgencyIconObservable = urgencyMessageObservable.filter { it != null }.map { it!!.icon }

    val vipMessageVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.isVipAccess)
    val airAttachVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.discountPercent < 0 && hotel.lowRateInfo.airAttached)
    val topAmenityTitleObservable = BehaviorSubject.create(getTopAmenityTitle(hotel, resources))
    val topAmenityVisibilityObservable = topAmenityTitleObservable.map { (it!="")}

    val hotelStarRatingObservable = BehaviorSubject.create(hotel.hotelStarRating)
    val ratingAmenityContainerVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.hotelStarRating > 0 || hotel.proximityDistanceInMiles > 0 || hotel.proximityDistanceInKiloMeters > 0)
    val hotelLargeThumbnailUrlObservable = BehaviorSubject.create(Images.getMediaHost() + hotel.largeThumbnailUrl)
    val hotelDiscountPercentageObservable = BehaviorSubject.create(Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo.discountPercent.toInt()).format().toString())
    val distanceFromCurrentLocationObservable: BehaviorSubject<kotlin.String> = BehaviorSubject.create<String>()
    val adImpressionObservable = BehaviorSubject.create<String>()

    init {
        if (hotel.isSponsoredListing && !hotel.hasShownImpression) {
            adImpressionObservable.onNext(hotel.impressionTrackingUrl)
        }

        if (hotel.proximityDistanceInMiles > 0) {
            val isAbbreviated = true
            distanceFromCurrentLocationObservable.onNext(HotelUtils.formatDistanceForNearby(resources, hotel, isAbbreviated))
        }
    }

    private fun getTopAmenityTitle(hotel: Hotel,resources: Resources):String {
        if (hotel.isSponsoredListing) return resources.getString(R.string.sponsored)
        else if (hotel.isShowEtpChoice) return resources.getString(R.string.book_now_pay_later)
        else if (hotel.hasFreeCancellation) return resources.getString(R.string.free_cancellation)
        else return ""
    }

    data class MostApplicableUrgencyMessage(val visibility: Boolean,val icon: Drawable, val background: Int,val message: String)

    public fun getMostApplicableUrgencyMessage(hotel: Hotel,resources: Resources): MostApplicableUrgencyMessage? {
        val roomsLeft = hotel.roomsLeftAtThisRate;
        if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
            return MostApplicableUrgencyMessage(true, resources.getDrawable(R.drawable.urgency),
                                                resources.getColor(R.color.hotel_urgency_message_color),
                                                resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft, roomsLeft))
        } else if (hotel.isSameDayDRR) {
            return MostApplicableUrgencyMessage(true, resources.getDrawable(R.drawable.tonight_only),
                                                resources.getColor(R.color.hotel_tonight_only_color),
                                                resources.getString(R.string.tonight_only))
        } else if (hotel.isDiscountRestrictedToCurrentSourceType) {
            return MostApplicableUrgencyMessage(true, resources.getDrawable(R.drawable.mobile_exclusive),
                                                resources.getColor(R.color.hotel_mobile_exclusive_color),
                                                resources.getString(R.string.mobile_exclusive))
        }
        return null
    }

    public fun setImpressionTracked(tracked : Boolean) {
        hotel.hasShownImpression = tracked
    }
}

