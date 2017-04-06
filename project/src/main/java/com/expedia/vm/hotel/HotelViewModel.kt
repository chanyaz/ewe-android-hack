package com.expedia.vm.hotel

import android.content.Context
import android.content.res.Resources
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject

open class HotelViewModel(private val context: Context) {
    val resources = context.resources

    val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    private val hotelObservable = BehaviorSubject.create<Hotel>()

    val hotelId = BehaviorSubject.create<String>()
    val soldOut = BehaviorSubject.create<Boolean>()
    val starRatingColor = soldOut.map { if (it) ContextCompat.getColor(context, R.color.hotelsv2_sold_out_hotel_gray) else ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color) }
    val imageColorFilter = soldOut.map { if (it) HotelDetailView.zeroSaturationColorMatrixColorFilter else null }
    val hotelNameObservable = BehaviorSubject.create<String>()
    val hotelPriceFormatted = BehaviorSubject.create<CharSequence>()
    val hotelStrikeThroughPriceFormatted = BehaviorSubject.create<CharSequence>()
    val strikethroughPriceToShowUsers = BehaviorSubject.create<Float>()
    val priceToShowUsers = BehaviorSubject.create<Float>()
    val showPackageTripSavings = BehaviorSubject.create<Boolean>()
    val hotelStrikeThroughPriceVisibility = BehaviorSubject.create(false)

    val loyaltyAvailabilityObservable = BehaviorSubject.create<Boolean>()
    val showDiscountObservable = BehaviorSubject.create<Boolean>()
    val hotelGuestRatingObservable = BehaviorSubject.create<Float>()
    val isHotelGuestRatingAvailableObservable = BehaviorSubject.create<Boolean>()
    val noGuestRatingVisibility = isHotelGuestRatingAvailableObservable.map { hasRating -> shouldShowNoGuestRating(hasRating) }
    val hotelPreviewRating = BehaviorSubject.create<Float>()
    val hotelPreviewRatingVisibility = hotelPreviewRating.map { it >= 0.5f }
    val pricePerNightObservable = BehaviorSubject.create<CharSequence>()
    val pricePerNightColorObservable = BehaviorSubject.create<Int>()

    val fewRoomsLeftUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val tonightOnlyUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val mobileExclusiveUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val soldOutUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)
    val memberDealUrgency = BehaviorSubject.create<UrgencyMessage>(null as UrgencyMessage?)

    val highestPriorityUrgencyMessageObservable = Observable.combineLatest(soldOutUrgency, memberDealUrgency, fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency) {
        soldOutUrgency, memberDealUrgency, fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency ->
        //Order in list below is important as it decides the priority of messages to be displayed in the urgency banner!
        listOf(soldOutUrgency, memberDealUrgency, fewRoomsLeftUrgency, tonightOnlyUrgency, mobileExclusiveUrgency).firstOrNull { it != null }
    }
    val urgencyMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val urgencyMessageBoxObservable = BehaviorSubject.create<String>()
    val urgencyMessageTextColorObservable = BehaviorSubject.create<Int>()
    val urgencyMessageBackgroundObservable = highestPriorityUrgencyMessageObservable.filter { it != null }.map {
        ContextCompat.getColor(context, it!!.backgroundColorId)
    }
    val urgencyIconObservable = highestPriorityUrgencyMessageObservable.filter { it != null && it.iconDrawableId != null }.map { ContextCompat.getDrawable(context, it!!.iconDrawableId!!) }
    val urgencyIconVisibilityObservable = highestPriorityUrgencyMessageObservable.map { it != null && it.iconDrawableId != null }

    val vipMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val vipLoyaltyMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val mapLoyaltyMessageTextObservable = BehaviorSubject.create<Spanned>()
    val airAttachWithDiscountLabelVisibilityObservable = BehaviorSubject.create<Boolean>()
    val airAttachIconWithoutDiscountLabelVisibility = BehaviorSubject.create<Boolean>()
    val earnMessagingObservable = BehaviorSubject.create<String>()
    val earnMessagingVisibilityObservable = Observable.combineLatest(hotelObservable, earnMessagingObservable, { hotel, earnMessage ->
        LoyaltyUtil.shouldShowEarnMessage(earnMessage, hotel.isPackage)
    })
    val topAmenityTitleObservable = BehaviorSubject.create<String>()
    val topAmenityVisibilityObservable = topAmenityTitleObservable.map { topAmenityTitle ->
        topAmenityTitle.isNotBlank()
    }

    val ratingPointsContainerVisibilityObservable = Observable.combineLatest(isHotelGuestRatingAvailableObservable, noGuestRatingVisibility, earnMessagingVisibilityObservable,
            { guestRatingAvailable, noGuestRatingVisible, earnMessageVisible ->
        guestRatingAvailable || noGuestRatingVisible || earnMessageVisible
    })

    val hotelStarRatingObservable = BehaviorSubject.create<Float>()
    val hotelStarRatingContentDescriptionObservable = BehaviorSubject.create<String>()
    val hotelStarRatingVisibilityObservable = BehaviorSubject.create<Boolean>()
    val hotelAmenityOrDistanceVisibilityObservable = BehaviorSubject.create<Boolean>()
    val hotelLargeThumbnailUrlObservable = BehaviorSubject.create<String>()
    val hotelDiscountPercentageObservable = BehaviorSubject.create<String>()
    val distanceFromCurrentLocation = BehaviorSubject.create<String>()
    val adImpressionObservable = BehaviorSubject.create<String>()

    @CallSuper
    open fun bindHotelData(hotel: Hotel) {
        hotelObservable.onNext(hotel)

        hotelId.onNext(hotel.hotelId)
        soldOut.onNext(hotel.isSoldOut)
        hotelNameObservable.onNext(hotel.localizedName)
        hotelPriceFormatted.onNext(priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage))
        hotelStrikeThroughPriceFormatted.onNext(priceFormatter(resources, hotel.lowRateInfo, true, !hotel.isPackage))
        strikethroughPriceToShowUsers.onNext(hotel.lowRateInfo?.strikethroughPriceToShowUsers ?: -1f)
        priceToShowUsers.onNext(hotel.lowRateInfo?.priceToShowUsers ?: -1f)
        showPackageTripSavings.onNext(hotel.isPackage && hotel.packageOfferModel?.price?.showTripSavings ?: false)
        loyaltyAvailabilityObservable.onNext(hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false)
        showDiscountObservable.onNext((hotel.lowRateInfo?.isDiscountPercentNotZero ?: false) && !(hotel.lowRateInfo?.airAttached ?: false) && !loyaltyAvailabilityObservable.value)
        hotelGuestRatingObservable.onNext(hotel.hotelGuestRating)
        isHotelGuestRatingAvailableObservable.onNext(hotel.hotelGuestRating > 0)
        hotelPreviewRating.onNext(hotel.hotelStarRating)
        pricePerNightObservable.onNext(priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage))

        airAttachWithDiscountLabelVisibilityObservable.onNext((hotel.lowRateInfo?.isShowAirAttached() ?: false) && !loyaltyAvailabilityObservable.value)
        airAttachIconWithoutDiscountLabelVisibility.onNext((hotel.lowRateInfo?.isShowAirAttached() ?: false) && loyaltyAvailabilityObservable.value)
        earnMessagingObservable.onNext(LoyaltyUtil.getEarnMessagingString(context, hotel.isPackage, hotel.lowRateInfo?.loyaltyInfo?.earn, hotel.packageOfferModel?.loyaltyInfo?.earn))
        topAmenityTitleObservable.onNext(getTopAmenityTitle(hotel, resources))

        hotelStarRatingObservable.onNext(hotel.hotelStarRating)
        hotelStarRatingVisibilityObservable.onNext(hotel.hotelStarRating > 0)
        hotelAmenityOrDistanceVisibilityObservable.onNext(hotel.proximityDistanceInMiles > 0 || hotel.proximityDistanceInKiloMeters > 0)
        hotelDiscountPercentageObservable.onNext(Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo?.discountPercent?.toInt() ?: 0).format().toString())
        distanceFromCurrentLocation.onNext(if (hotel.proximityDistanceInMiles > 0) HotelUtils.formatDistanceForNearby(resources, hotel, true) else "")

        if (hotel.isSponsoredListing && !hotel.hasShownImpression) {
            adImpressionObservable.onNext(hotel.impressionTrackingUrl)
        }

        Observable.combineLatest(strikethroughPriceToShowUsers, priceToShowUsers, soldOut, showPackageTripSavings) {
            strikethroughPriceToShowUsers, priceToShowUsers, soldOut, showPackageTripSavings ->
            !soldOut && (if (hotel.isPackage) showPackageTripSavings else priceToShowUsers < strikethroughPriceToShowUsers) }.subscribe(hotelStrikeThroughPriceVisibility)

        val url = if (hotel.isPackage) hotel.thumbnailUrl else Images.getMediaHost() + hotel.largeThumbnailUrl
        if (!url.isNullOrBlank()) hotelLargeThumbnailUrlObservable.onNext(url)

        val isVipAvailable = hotel.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess() && User.isLoggedIn(context)
        val isMidOrTopTier = Db.getUser()?.primaryTraveler?.loyaltyMembershipTier?.isMidOrTopTier ?: false
        vipMessageVisibilityObservable.onNext(isVipAvailable && isMidOrTopTier)
        val isVipLoyaltyApplied = isVipAvailable && isMidOrTopTier && loyaltyAvailabilityObservable.value
        vipLoyaltyMessageVisibilityObservable.onNext(isVipLoyaltyApplied)

        val mapLoyaltyMessageString = if (isVipLoyaltyApplied) resources.getString(R.string.vip_loyalty_applied_map_message) else resources.getString(R.string.regular_loyalty_applied_message)
        mapLoyaltyMessageTextObservable.onNext(HtmlCompat.fromHtml(mapLoyaltyMessageString))

        soldOut.map { soldOut ->
            if (soldOut) {
                UrgencyMessage(null, R.color.hotel_sold_out_color,
                        resources.getString(R.string.trip_bucket_sold_out))
            } else {
                null
            }
        }.subscribe(soldOutUrgency)

        if (hasMemberDeal(hotel)) {
            if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelMemberPricingBadge)) {
                memberDealUrgency.onNext(HotelViewModel.UrgencyMessage(R.drawable.ic_hotel_member_test, R.color.hotel_member_pricing_color_test,
                        resources.getString(R.string.member_pricing)))
                urgencyMessageTextColorObservable.onNext(ContextCompat.getColor(context, R.color.hotel_member_pricing_color))
            } else {
                memberDealUrgency.onNext(HotelViewModel.UrgencyMessage(R.drawable.ic_hotel_banner, R.color.hotel_member_pricing_color,
                        resources.getString(R.string.member_pricing)))
                urgencyMessageTextColorObservable.onNext(ContextCompat.getColor(context, R.color.white))
            }
        } else {
            memberDealUrgency.onNext(null)
            urgencyMessageTextColorObservable.onNext(ContextCompat.getColor(context, R.color.white))
        }

        if (hotel.roomsLeftAtThisRate > 0 && hotel.roomsLeftAtThisRate <= ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
            fewRoomsLeftUrgency.onNext(UrgencyMessage(R.drawable.urgency, R.color.hotel_urgency_message_color,
                    resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate)))
        } else {
            fewRoomsLeftUrgency.onNext(null)
        }

        if (hotel.isSameDayDRR) {
            tonightOnlyUrgency.onNext(UrgencyMessage(R.drawable.tonight_only, R.color.hotel_tonight_only_color,
                    resources.getString(R.string.tonight_only)))
        } else {
            tonightOnlyUrgency.onNext(null)
        }

        if (hotel.isDiscountRestrictedToCurrentSourceType) {
            if (ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable == 0) {
                mobileExclusiveUrgency.onNext(UrgencyMessage(R.drawable.mobile_exclusive, R.color.hotel_mobile_exclusive_color,
                        resources.getString(R.string.mobile_exclusive)))
            } else {
                mobileExclusiveUrgency.onNext(UrgencyMessage(ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable,
                        R.color.hotel_mobile_exclusive_color, ""))
            }
        } else {
            mobileExclusiveUrgency.onNext(null)
        }

        highestPriorityUrgencyMessageObservable.map { it != null }.subscribe(urgencyMessageVisibilityObservable)
        highestPriorityUrgencyMessageObservable.filter { it != null }.map {
            it!!.message
        }.subscribe(urgencyMessageBoxObservable)

        hotelStarRatingContentDescriptionObservable.onNext(HotelsV2DataUtil.getHotelRatingContentDescription(context, hotel.hotelStarRating.toInt()))

        pricePerNightColorObservable.onNext(ContextCompat.getColor(context, getPricePerNightTextColor(hotel)))
    }

    private fun getTopAmenityTitle(hotel: Hotel, resources: Resources): String {
        if (hotel.isSponsoredListing) {
            return resources.getString(R.string.sponsored)
        } else if (hotel.isShowEtpChoice) {
            return resources.getString(R.string.book_now_pay_later)
        } else if (hotel.hasFreeCancellation) {
            return resources.getString(R.string.free_cancellation)
        }
        else return ""
    }

    data class UrgencyMessage(val iconDrawableId: Int?, val backgroundColorId: Int, val message: String)

    fun setImpressionTracked(hotel: Hotel, tracked: Boolean) {
        hotel.hasShownImpression = tracked
    }

    open fun hasMemberDeal(hotel: Hotel): Boolean {
        return hotel.isMemberDeal && User.isLoggedIn(context)
    }

    fun getRatingContentDesc(hotel: Hotel): String {
        var phrase: Phrase
        if (hotel.hotelStarRating.toInt() <= 0 && hotel.hotelGuestRating <= 0f) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_starrating_zero_guestrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
        } else if (hotel.hotelStarRating.toInt() <= 0) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_starrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("guestrating", hotelGuestRatingObservable.value.toString())
        } else if (hotel.hotelGuestRating <= 0f) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_guestrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("starrating", hotelStarRatingContentDescriptionObservable.value)
        } else {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("starrating", hotelStarRatingContentDescriptionObservable.value)
                    .put("guestrating", hotelGuestRatingObservable.value.toString())
        }
        return phrase.format().toString()
    }

    open fun getHotelContentDesc(hotel: Hotel): CharSequence {
        val result = SpannableBuilder()
        result.append(getRatingContentDesc(hotel))

        if (urgencyMessageVisibilityObservable.value) {
            result.append(urgencyMessageBoxObservable.value + " ")
        }

        if (showDiscountObservable.value) {
            val discountPercentage = Phrase.from(context , R.string.hotel_discount_percent_Template)
                    .put("discount", Math.abs(hotel.lowRateInfo?.discountPercent?.toInt() ?: 0))
                    .format()
                    .toString()

            result.append(Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE)
                    .put("percentage", discountPercentage)
                    .format()
                    .toString())
        }

        result.append(earnMessagingObservable.value + " ")

        if (hotelStrikeThroughPriceVisibility.value) {
            result.append(Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", hotelStrikeThroughPriceFormatted.value)
                    .put("price", pricePerNightObservable.value)
                    .format()
                    .toString())
        } else {
            result.append(Phrase.from(context, R.string.hotel_card_view_price_cont_desc_TEMPLATE)
                .put("price", pricePerNightObservable.value)
                .format()
                .toString())
        }

        result.append(Phrase.from(context.resources.getString(R.string.accessibility_cont_desc_role_button)).format().toString())

        return result.build()
    }

    private fun getPricePerNightTextColor(hotel: Hotel): Int {
        if (hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false) {
            return R.color.hotels_primary_color
        }
        return R.color.hotel_cell_gray_text
    }

    private fun shouldShowNoGuestRating(hasRating: Boolean): Boolean {
        return !hasRating && !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHideNoReviewRating)
    }
}
