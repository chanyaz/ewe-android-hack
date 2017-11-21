package com.expedia.vm.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.ColorMatrixColorFilter
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

open class HotelViewModel(private val context: Context) {

    var isHotelSoldOut = false

    protected val resources = context.resources
    protected lateinit var hotel: Hotel

    private val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val soldOut = BehaviorSubject.create<Boolean>()

    val hotelId: String get() = hotel.hotelId
    val hotelName: String get() = hotel.localizedName
    val hotelPriceFormatted: CharSequence get() = priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage)
    val hotelStrikeThroughPriceFormatted: CharSequence? get() = getStrikeThroughPriceToShowUsers()
    val strikeThroughPriceToShowUsers: Float get() = hotel.lowRateInfo?.strikethroughPriceToShowUsers ?: -1f
    val priceToShowUsers: Float get() = hotel.lowRateInfo?.priceToShowUsers ?: -1f
    val pricePerNight: CharSequence get() = priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage)
    val pricePerNightColor: Int get() = ContextCompat.getColor(context, getPricePerNightTextColor())
    val pricePerDescriptor: String? get() = getPricePerDescriptorString()

    val topAmenityTitle: String get() = getTopAmenityTitle(resources)
    val loyaltyAvailable: Boolean get() = hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false
    val showDiscount: Boolean get() = (hotel.lowRateInfo?.isDiscountPercentNotZero ?: false) && !(hotel.lowRateInfo?.airAttached ?: false) && !loyaltyAvailable
    val hotelDiscountPercentage: String get() = Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo?.discountPercent?.toInt() ?: 0).format().toString()

    val hotelStarRating: Float get() = hotel.hotelStarRating
    val showStarRating: Boolean get() = hotelStarRating > 0
    val hotelGuestRating: Float get() = hotel.hotelGuestRating
    val isHotelGuestRatingAvailable: Boolean get() = hotelGuestRating > 0
    val showNoGuestRating: Boolean get() = !isHotelGuestRatingAvailable
    val showHotelPreviewRating: Boolean get() = hotelStarRating >= 0.5f
    val isShowHotelHotelDistance: Boolean get() = (hotel.proximityDistanceInMiles > 0 || hotel.proximityDistanceInKiloMeters > 0) && hotel.isCurrentLocationSearch

    val earnMessage: String get() = LoyaltyUtil.getEarnMessagingString(context, hotel.isPackage, hotel.lowRateInfo?.loyaltyInfo?.earn, hotel.packageOfferModel?.loyaltyInfo?.earn)
    val showEarnMessage: Boolean get() = LoyaltyUtil.shouldShowEarnMessage(earnMessage, hotel.isPackage)
    val showAirAttachWithDiscountLabel: Boolean get() = (hotel.lowRateInfo?.isShowAirAttached() ?: false) && !loyaltyAvailable
    val showAirAttachIconWithoutDiscountLabel: Boolean get() = (hotel.lowRateInfo?.isShowAirAttached() ?: false) && loyaltyAvailable
    val neighborhoodName: String get() = hotel.neighborhoodName ?: ""

    init {
        soldOut.subscribe { soldOut ->
            isHotelSoldOut = soldOut
        }
    }

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    @CallSuper
    open fun bindHotelData(hotel: Hotel) {
        this.hotel = hotel
        this.isHotelSoldOut = hotel.isSoldOut

        if (hotel.isSponsoredListing && !hotel.hasShownImpression) {
            AdImpressionTracking.trackAdClickOrImpression(context, hotel.impressionTrackingUrl, null)
            hotel.hasShownImpression = true
        }
    }

    fun getStarRatingColor(): Int {
        if (isHotelSoldOut) {
            return ContextCompat.getColor(context, R.color.hotelsv2_sold_out_hotel_gray)
        } else {
            return ContextCompat.getColor(context, R.color.hotelsv2_detail_star_color)
        }
    }

    fun getImageColorFilter(): ColorMatrixColorFilter? {
        if (isHotelSoldOut) {
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f)
            return ColorMatrixColorFilter(colorMatrix)
        }
        return null
    }

    fun distanceFromCurrentLocation(): String {
        if (hotel.proximityDistanceInMiles > 0) {
            return HotelUtils.formatDistanceForNearby(resources, hotel, true)
        } else return ""
    }

    fun getStrikeThroughPriceToShowUsers(): CharSequence? {
        if (shouldShowStrikeThroughPrice()) {
            return priceFormatter(resources, hotel.lowRateInfo, true, !hotel.isPackage)
        } else {
            return null
        }
    }

    fun getHotelLargeThumbnailUrl(): String {
        if (hotel.isPackage && (hotel.thumbnailUrl?.startsWith("http") ?: true)) {
            return hotel.thumbnailUrl ?: ""
        }

        return Images.getMediaHost() + hotel.largeThumbnailUrl
    }

    fun showVipMessage(): Boolean {
        return hotel.isVipAccess && PointOfSale.getPointOfSale().supportsVipAccess()
    }

    fun showVipLoyaltyMessage(): Boolean {
        val user = userStateManager.userSource.user
        val isMidOrTopTier = user?.loyaltyMembershipInformation?.loyaltyMembershipTier?.isMidOrTopTier ?: false
        return showVipMessage() && isMidOrTopTier && loyaltyAvailable
    }

    fun getMapLoyaltyMessageText(): Spanned {
        if (showVipLoyaltyMessage()) {
            return HtmlCompat.fromHtml(resources.getString(R.string.vip_loyalty_applied_map_message))
        } else {
            return HtmlCompat.fromHtml(resources.getString(R.string.regular_loyalty_applied_message))
        }
    }

    fun showRatingPointsContainer(): Boolean {
        return isHotelGuestRatingAvailable || showNoGuestRating || showEarnMessage
    }

    fun getHighestPriorityUrgencyMessage(): UrgencyMessage? {
        if (isHotelSoldOut) {
            return null
        }

        val memberDealUrgency = getMemberDealUrgencyMessage()
        if (memberDealUrgency != null) {
            return memberDealUrgency
        }

        val fewRoomsLeftUrgency = getFewRoomsLeftUrgencyMessage()
        if (fewRoomsLeftUrgency != null) {
            return fewRoomsLeftUrgency
        }

        val tonightOnlyUrgency = getTonightOnlyUrgencyMessage()
        if (tonightOnlyUrgency != null) {
            return tonightOnlyUrgency
        }

        return getMobileExclusiveUrgencyMessage()
    }

    open fun hasMemberDeal(): Boolean {
        return hotel.isMemberDeal && userStateManager.isUserAuthenticated()
    }

    fun getRatingContentDesc(hotel: Hotel): String {
        val phrase: Phrase
        val hotelStarRatingContentDescription = HotelsV2DataUtil.getHotelRatingContentDescription(context, hotel.hotelStarRating.toDouble())

        if (hotel.hotelStarRating.toInt() <= 0 && hotel.hotelGuestRating <= 0f) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_starrating_zero_guestrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
        } else if (hotel.hotelStarRating.toInt() <= 0) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_starrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("guestrating", hotel.hotelGuestRating.toString())
        } else if (hotel.hotelGuestRating <= 0f) {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_zero_guestrating_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("starrating", hotelStarRatingContentDescription)
        } else {
            phrase = Phrase.from(context, R.string.hotel_details_cont_desc_TEMPLATE)
                    .put("hotel", hotel.localizedName)
                    .put("starrating", hotelStarRatingContentDescription)
                    .put("guestrating", hotel.hotelGuestRating.toString())
        }
        return phrase.format().toString()
    }

    open fun getHotelContentDesc(): CharSequence {
        val result = SpannableBuilder()
        result.append(getRatingContentDesc(hotel))

        val urgencyMessage = getHighestPriorityUrgencyMessage()
        if (urgencyMessage != null) {
            result.append(urgencyMessage.message + " ")
        }

        if (showDiscount) {
            val discountPercentage = Phrase.from(context, R.string.hotel_discount_percent_Template)
                    .put("discount", Math.abs(hotel.lowRateInfo?.discountPercent?.toInt() ?: 0))
                    .format()
                    .toString()

            result.append(Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE)
                    .put("percentage", discountPercentage)
                    .format()
                    .toString())
        }

        result.append(earnMessage + " ")

        if (hotelStrikeThroughPriceFormatted != null) {
            result.append(Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", hotelStrikeThroughPriceFormatted)
                    .put("price", pricePerNight)
                    .format()
                    .toString())
        } else {
            result.append(Phrase.from(context, R.string.hotel_card_view_price_cont_desc_TEMPLATE)
                    .put("price", pricePerNight)
                    .format()
                    .toString())
        }

        result.append(Phrase.from(context.resources.getString(R.string.accessibility_cont_desc_role_button)).format().toString())

        return result.build()
    }

    private fun shouldShowStrikeThroughPrice(): Boolean {
        if (isHotelSoldOut) {
            return false
        }

        if (hotel.isPackage) {
            val showPackageTripSavings = hotel.packageOfferModel?.price?.showTripSavings ?: false

            return showPackageTripSavings
        } else if (LoyaltyUtil.isShopWithPoints(hotel.lowRateInfo)) {
            return true
        } else if (!hotel.lowRateInfo.airAttached &&
                !AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)) {
            return priceToShowUsers < strikeThroughPriceToShowUsers
        } else {
            return false
        }
    }

    private fun getMemberDealUrgencyMessage(): UrgencyMessage? {
        if (hasMemberDeal()) {
            return UrgencyMessage(R.drawable.ic_member_only_tag, R.color.brand_secondary,
                    resources.getString(R.string.member_pricing), R.color.brand_primary)
        }
        return null
    }

    private fun getFewRoomsLeftUrgencyMessage(): UrgencyMessage? {
        if (hotel.roomsLeftAtThisRate in 1..ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
            return UrgencyMessage(R.drawable.urgency, R.color.hotel_urgency_message_color,
                    resources.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate))
        }
        return null
    }

    private fun getTonightOnlyUrgencyMessage(): UrgencyMessage? {
        if (hotel.isSameDayDRR) {
            return UrgencyMessage(R.drawable.tonight_only, R.color.hotel_tonight_only_color,
                    resources.getString(R.string.tonight_only))
        }
        return null
    }

    private fun getMobileExclusiveUrgencyMessage(): UrgencyMessage? {
        if (hotel.isDiscountRestrictedToCurrentSourceType) {
            return UrgencyMessage(R.drawable.mobile_exclusive, R.color.hotel_mobile_exclusive_color,
                    resources.getString(R.string.mobile_exclusive))
        }
        return null
    }

    private fun getPricePerDescriptorString(): String? {
        val bucketedToShowPriceDescriptorProminence = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)
        val priceType = hotel.lowRateInfo.getUserPriceType()
        if (bucketedToShowPriceDescriptorProminence) {
            return when (priceType) {
                HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> context.getString(R.string.total_stay)
                HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES -> context.getString(R.string.per_night)
                else -> null
            }
        } else {
            return null
        }
    }

    private fun getTopAmenityTitle(resources: Resources): String {
        if (hotel.isSponsoredListing) {
            return resources.getString(R.string.sponsored)
        } else if (hotel.isShowEtpChoice) {
            return resources.getString(R.string.book_now_pay_later)
        } else if (hotel.hasFreeCancellation) {
            return resources.getString(R.string.free_cancellation)
        } else return ""
    }

    private fun getPricePerNightTextColor(): Int {
        if (hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false) {
            return R.color.hotels_primary_color
        }
        return R.color.default_text_color
    }

    data class UrgencyMessage(val iconDrawableId: Int?, val backgroundColorId: Int, val message: String, val messageTextColorId: Int = R.color.white) {
        fun hasIconDrawable(): Boolean {
            return iconDrawableId != null
        }

        fun getMessageTextColor(context: Context): Int {
            return ContextCompat.getColor(context, messageTextColorId)
        }

        fun getBackgroundColor(context: Context): Int {
            return ContextCompat.getColor(context, backgroundColorId)
        }
    }
}
