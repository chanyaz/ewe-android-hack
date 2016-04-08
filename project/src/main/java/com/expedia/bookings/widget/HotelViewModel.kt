package com.expedia.bookings.widget

import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Images
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject

class HotelViewModel(private val context: Context, private val hotel: Hotel) {
    val resources = context.resources

    val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val hotelId = hotel.hotelId
    val soldOut = BehaviorSubject.create<Boolean>(hotel.isSoldOut)
    val toolBarRatingColor = soldOut.map { if (it) ContextCompat.getColor(context, R.color.hotelsv2_sold_out_hotel_gray) else ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color) }
    val imageColorFilter = soldOut.map { if (it) HotelDetailView.zeroSaturationColorMatrixColorFilter else null }
    val hotelNameObservable = BehaviorSubject.create(hotel.localizedName)
    val hotelPriceFormatted = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage))
    val hotelStrikeThroughPriceFormatted = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, true, !hotel.isPackage))
    val strikethroughPriceToShowUsers = BehaviorSubject.create(hotel.lowRateInfo.strikethroughPriceToShowUsers)
    val priceToShowUsers = BehaviorSubject.create(hotel.lowRateInfo.priceToShowUsers)
    val hotelStrikeThroughPriceVisibility = BehaviorSubject.create(false)
    val loyaltyAvailabilityObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo?.loyaltyInfo?.isShopWithPoints ?: false)
    val showDiscountObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isDiscountPercentNotZero && !hotel.lowRateInfo.airAttached && !loyaltyAvailabilityObservable.value)
    val hotelGuestRatingObservable = BehaviorSubject.create(hotel.hotelGuestRating)
    val isHotelGuestRatingAvailableObservable = BehaviorSubject.create<Boolean>(hotel.hotelGuestRating > 0)
    val hotelPreviewRating = BehaviorSubject.create<Float>(hotel.hotelStarRating)
    val hotelPreviewRatingVisibility = BehaviorSubject.create<Float>(hotel.hotelStarRating).map { it >= 0.5f }
    val pricePerNightObservable = BehaviorSubject.create(priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage))
    val pricePerNightColorObservable = BehaviorSubject.create(ContextCompat.getColor(context, if (hotel.lowRateInfo?.loyaltyInfo?.isShopWithPoints ?: false) R.color.hotels_primary_color else R.color.hotel_cell_gray_text))

    val fewRoomsLeftUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val tonightOnlyUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val mobileExclusiveUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val soldOutUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val memberDealUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)

    val highestPriorityUrgencyMessageObservable = Observable.combineLatest(fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency, soldOutUrgency, memberDealUrgency) {
        fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency, soldOutUrgency, memberDealUrgency ->
        //Order in list below is important as it decides the priority of messages to be displayed in the urgency banner!
        listOf(soldOutUrgency, memberDealUrgency, fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency).firstOrNull { it != null }
    }
    val urgencyMessageVisibilityObservable = highestPriorityUrgencyMessageObservable.map { it != null }
    val urgencyMessageBoxObservable = highestPriorityUrgencyMessageObservable.filter { it != null }.map { it!!.message }
    val urgencyMessageBackgroundObservable = highestPriorityUrgencyMessageObservable.filter { it != null }.map { ContextCompat.getColor(context, it!!.backgroundColorId) }
    val urgencyIconObservable = highestPriorityUrgencyMessageObservable.filter { it != null && it.iconDrawableId != null }.map { ContextCompat.getDrawable(context, it!!.iconDrawableId!!) }
    val urgencyIconVisibilityObservable = highestPriorityUrgencyMessageObservable.map { it != null && it.iconDrawableId != null }

    val vipMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val vipLoyaltyMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val mapLoyaltyMessageTextObservable = BehaviorSubject.create<Spanned>()
    val airAttachWithDiscountLabelVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isShowAirAttached() && !loyaltyAvailabilityObservable.value)
    val airAttachIconWithoutDiscountLabelVisibility = BehaviorSubject.create<Boolean>(hotel.lowRateInfo.isShowAirAttached() && loyaltyAvailabilityObservable.value)
    val earnMessagingObservable = Observable.just(getEarnMessage())
    val earnMessagingVisibilityObservable = earnMessagingObservable.map { it.isNotBlank() && PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels }

    val topAmenityTitleObservable = BehaviorSubject.create(getTopAmenityTitle(hotel, resources))
    val topAmenityVisibilityObservable = earnMessagingVisibilityObservable.zipWith(topAmenityTitleObservable, { earnMessagingEnabled, topAmenityTitle ->
        !earnMessagingEnabled && topAmenityTitle.isNotBlank()})

    val hotelStarRatingObservable = BehaviorSubject.create(hotel.hotelStarRating)
    val ratingAmenityContainerVisibilityObservable = BehaviorSubject.create<Boolean>(hotel.hotelStarRating > 0 || hotel.proximityDistanceInMiles > 0 || hotel.proximityDistanceInKiloMeters > 0)
    val hotelLargeThumbnailUrlObservable = BehaviorSubject.create(if (hotel.isPackage) hotel.thumbnailUrl else Images.getMediaHost() + hotel.largeThumbnailUrl)
    val hotelDiscountPercentageObservable = BehaviorSubject.create(Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo.discountPercent.toInt()).format().toString())
    val distanceFromCurrentLocation = BehaviorSubject.create(if (hotel.proximityDistanceInMiles > 0) HotelUtils.formatDistanceForNearby(resources, hotel, true) else "")
    val adImpressionObservable = BehaviorSubject.create<String>()
    val priceIncludesFlightsObservable = BehaviorSubject.create<Boolean>(hotel.isPackage)
    val unrealDealMessageObservable = BehaviorSubject.create(hotel.packageOfferModel?.brandedDealData?.dealVariation ?: "")
    val unrealDealMessageContainerVisibilityObservable = BehaviorSubject.create<Boolean>()

    init {
        if (hotel.isSponsoredListing && !hotel.hasShownImpression) {
            adImpressionObservable.onNext(hotel.impressionTrackingUrl)
        }

        Observable.combineLatest(strikethroughPriceToShowUsers, priceToShowUsers, soldOut) { strikethroughPriceToShowUsers, priceToShowUsers, soldOut -> !soldOut && (priceToShowUsers < strikethroughPriceToShowUsers) }.subscribe(hotelStrikeThroughPriceVisibility)

        val isVipAvailable = hotel.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess() && User.isLoggedIn(context)
        val isGoldOrSilver = Db.getUser() != null && (Db.getUser().primaryTraveler.loyaltyMembershipTier == Traveler.LoyaltyMembershipTier.SILVER || Db.getUser().primaryTraveler.loyaltyMembershipTier == Traveler.LoyaltyMembershipTier.GOLD)
        vipMessageVisibilityObservable.onNext(isVipAvailable && isGoldOrSilver)
        val isVipLoyaltyApplied = isVipAvailable && isGoldOrSilver && loyaltyAvailabilityObservable.value
        vipLoyaltyMessageVisibilityObservable.onNext(isVipLoyaltyApplied)

        val mapLoyaltyMessageString = if (isVipLoyaltyApplied) resources.getString(R.string.vip_loyalty_applied_map_message) else resources.getString(R.string.regular_loyalty_applied_message)
        mapLoyaltyMessageTextObservable.onNext(Html.fromHtml(mapLoyaltyMessageString))

        unrealDealMessageContainerVisibilityObservable.onNext(hotel.packageOfferModel?.brandedDealData?.dealVariation?.isNotEmpty() ?: false)
        // NOTE: Any changes to this logic should also be made in HotelDetailViewModel.getPromoText()
        val isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsMemberDealTest)
        if (hotel.isMemberDeal && isUserBucketedForTest && User.isLoggedIn(context)) {
            memberDealUrgency.onNext(UrgencyMessage(R.drawable.ic_hotel_banner_expedia, R.color.hotel_member_pricing_color,
                    resources.getString(R.string.member_pricing)))
        }
        if (hotel.roomsLeftAtThisRate > 0 && hotel.roomsLeftAtThisRate <= ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
            fewRoomsLeftUrgency.onNext(UrgencyMessage(R.drawable.urgency, R.color.hotel_urgency_message_color,
                    resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate)))
        }
        if (hotel.isSameDayDRR) {
            tonightOnlyUrgency.onNext(UrgencyMessage(R.drawable.tonight_only, R.color.hotel_tonight_only_color,
                    resources.getString(R.string.tonight_only)))
        }
        if (hotel.isDiscountRestrictedToCurrentSourceType) {
            mobileExclusiveUrgency.onNext(UrgencyMessage(R.drawable.mobile_exclusive, R.color.hotel_mobile_exclusive_color,
                    resources.getString(R.string.mobile_exclusive)))
        }

        soldOut.filter { it == true }.map {
            UrgencyMessage(null, R.color.hotel_sold_out_color,
                    resources.getString(R.string.trip_bucket_sold_out))
        }.subscribe(soldOutUrgency)
    }

    private fun getTopAmenityTitle(hotel: Hotel, resources: Resources): String {
        if (hotel.isSponsoredListing) return resources.getString(R.string.sponsored)
        else if (hotel.isShowEtpChoice) return resources.getString(R.string.book_now_pay_later)
        else if (hotel.hasFreeCancellation) return resources.getString(R.string.free_cancellation)
        else return ""
    }

    data class UrgencyMessage(val iconDrawableId: Int?, val backgroundColorId: Int, val message: String)

    fun setImpressionTracked(tracked: Boolean) {
        hotel.hasShownImpression = tracked
    }

    private fun getEarnMessage(): String {
        val earnMessagePointsOrPrice = hotel.lowRateInfo?.loyaltyInfo?.earn?.getEarnMessagePointsOrPrice()
        if (earnMessagePointsOrPrice?.isNotBlank() ?: false) return Phrase.from(resources, R.string.earn_message_TEMPLATE).put("earn", earnMessagePointsOrPrice).format().toString()
        else return ""
    }
}

