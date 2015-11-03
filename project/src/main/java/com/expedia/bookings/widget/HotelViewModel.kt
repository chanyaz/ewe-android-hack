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
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject

public class HotelViewModel(private val context: Context, private val hotel: Hotel) {
    val resources = context.resources

    val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val hotelId = hotel.hotelId
    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false))
    val hotelStrikeThroughPriceObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, true))
    val hotelStrikeThroughPriceVisibilityObservable = BehaviorSubject.create<Boolean>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isDiscountTenPercentOrBetter()  && !hotel.lowRateInfo.airAttached)
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating.toString())
    val isHotelGuestRatingAvailableObservable = BehaviorSubject.create<Boolean>(hotel.hotelGuestRating > 0)
    val hotelPreviewRatingObservable = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val pricePerNightObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false))
    val soldOut = BehaviorSubject.create<Boolean>(hotel.isSoldOut)
    val toolBarRatingColor = soldOut.map { if (it) context.resources.getColor(android.R.color.white) else context.resources.getColor(R.color.hotelsv2_detail_star_color) }
    val imageColorFilter = soldOut.map { if (it) HotelDetailView.zeroSaturationColorMatrixColorFilter else null }

    val fewRoomsLeftUrgency = BehaviorSubject.create<UrgencyMessage>(null)
    val tonightOnlyUrgency = BehaviorSubject.create<UrgencyMessage>(null)
    val mobileExclusiveUrgency = BehaviorSubject.create<UrgencyMessage>(null)
    val soldOutUrgency = BehaviorSubject.create<UrgencyMessage>(null)

    val urgencyMessageObservable = Observable.combineLatest(fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency, soldOutUrgency) {
        fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency, soldOutUrgency ->
        //Order in list below is important as it decides the priority of messages to be displayed in the urgency banner!
        listOf(soldOutUrgency, fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency).firstOrNull { it != null }
    }
    val urgencyMessageVisibilityObservable = urgencyMessageObservable.map { if (it != null) it.visibility else false }
    val urgencyMessageBoxObservable = urgencyMessageObservable.filter { it != null }.map { it!!.message }
    val urgencyMessageBackgroundObservable = urgencyMessageObservable.filter { it != null }.map { resources.getColor(it!!.backgroundColorId) }
    val urgencyIconObservable = urgencyMessageObservable.filter { it != null && it.iconDrawableId != null }.map { resources.getDrawable(it!!.iconDrawableId!!) }
    val urgencyIconVisibilityObservable = urgencyMessageObservable.map { it != null && it.iconDrawableId != null }

    val vipMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val airAttachVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isAirAttached())
    val topAmenityTitleObservable = BehaviorSubject.create(getTopAmenityTitle(hotel, resources))
    val topAmenityVisibilityObservable = topAmenityTitleObservable.map { (it != "") }

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

        if (hotel.roomsLeftAtThisRate > 0 && hotel.roomsLeftAtThisRate <= ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
            fewRoomsLeftUrgency.onNext(UrgencyMessage(true, R.drawable.urgency,
                    R.color.hotel_urgency_message_color,
                    resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate)))
        }
        if (hotel.isSameDayDRR) {
            tonightOnlyUrgency.onNext(UrgencyMessage(true, R.drawable.tonight_only,
                    R.color.hotel_tonight_only_color,
                    resources.getString(R.string.tonight_only)))
        }
        if (hotel.isDiscountRestrictedToCurrentSourceType) {
                mobileExclusiveUrgency.onNext(UrgencyMessage(true, R.drawable.mobile_exclusive,
                    R.color.hotel_mobile_exclusive_color,
                    resources.getString(R.string.mobile_exclusive)))
        }

        soldOut.filter { it == true }.map {
            UrgencyMessage(true, null,
                    R.color.hotel_sold_out_color,
                    resources.getString(R.string.trip_bucket_sold_out))
        }.subscribe(soldOutUrgency)
    }

    private fun getTopAmenityTitle(hotel: Hotel, resources: Resources): String {
        if (hotel.isSponsoredListing) return resources.getString(R.string.sponsored)
        else if (hotel.isShowEtpChoice) return resources.getString(R.string.book_now_pay_later)
        else if (hotel.hasFreeCancellation) return resources.getString(R.string.free_cancellation)
        else return ""
    }

    data class UrgencyMessage(val visibility: Boolean, val iconDrawableId: Int?, val backgroundColorId: Int, val message: String)

    public fun setImpressionTracked(tracked: Boolean) {
        hotel.hasShownImpression = tracked
    }
}

