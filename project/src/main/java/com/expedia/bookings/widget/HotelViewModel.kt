package com.expedia.bookings.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isAirAttached
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject

public class HotelViewModel(private val context: Context, private val hotel: Hotel) {
    val resources = context.resources

    val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false))
    val hotelStrikeThroughPriceObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, true))
    val hotelStrikeThroughPriceVisibilityObservable = BehaviorSubject.create<Boolean>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isDiscountTenPercentOrBetter()  && !hotel.lowRateInfo.airAttached)
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating.toString())
    val isHotelGuestRatingAvailableObservable = BehaviorSubject.create<Boolean>(hotel.hotelGuestRating > 0)
    val hotelPreviewRatingObservable = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val pricePerNightObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false))

    val urgencyMessageObservable = BehaviorSubject.create(getMostApplicableUrgencyMessage(hotel, resources))
    val urgencyMessageVisibilityObservable = urgencyMessageObservable.map { if (it != null) it!!.visibility else false }
    val urgencyMessageBoxObservable = urgencyMessageObservable.filter { it != null }.map { it!!.message }
    val urgencyMessageBackgroundObservable = urgencyMessageObservable.filter { it != null }.map { it!!.background }
    val urgencyIconObservable = urgencyMessageObservable.filter { it != null }.map { it!!.icon }
    
    val vipMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val airAttachVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isAirAttached())
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
        val lowRateInfo = hotel.lowRateInfo
        val strikethroughPriceToShowUsers = lowRateInfo.strikethroughPriceToShowUsers
        val priceToShowUsers = lowRateInfo.priceToShowUsers
        hotelStrikeThroughPriceVisibilityObservable.onNext(priceToShowUsers < strikethroughPriceToShowUsers)

        val hasDistance = hotel.proximityDistanceInMiles > 0
        val distance = if (hasDistance) HotelUtils.formatDistanceForNearby(resources, hotel, true) else ""
        distanceFromCurrentLocationObservable.onNext(distance)

        val isVipAvailable = hotel.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess() && User.isLoggedIn(context)
        val isGoldOrSivler = Db.getUser() != null && (Db.getUser().primaryTraveler.loyaltyMembershipTier == Traveler.LoyaltyMembershipTier.SILVER || Db.getUser().primaryTraveler.loyaltyMembershipTier == Traveler.LoyaltyMembershipTier.GOLD)
        vipMessageVisibilityObservable.onNext(isVipAvailable && isGoldOrSivler)
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

