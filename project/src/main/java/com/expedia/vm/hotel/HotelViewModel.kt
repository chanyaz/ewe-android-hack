package com.expedia.vm.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.ColorMatrixColorFilter
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
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
import com.expedia.vm.ShopWithPointsViewModel
import javax.inject.Inject

open class HotelViewModel(private val context: Context) {

    var isHotelSoldOut = false

    protected val resources = context.resources
    protected lateinit var hotel: Hotel

    private val ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    val soldOut = BehaviorSubject.create<Boolean>()

    val hotelId: String get() = hotel.hotelId
    val hotelName: String get() = hotel.localizedName
    val hotelPriceFormatted: CharSequence get() = priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage)
    val hotelStrikeThroughPriceFormatted: CharSequence get() = priceFormatter(resources, hotel.lowRateInfo, true, !hotel.isPackage)
    val strikeThroughPriceToShowUsers: Float get() = hotel.lowRateInfo?.strikethroughPriceToShowUsers ?: -1f
    val priceToShowUsers: Float get() = hotel.lowRateInfo?.priceToShowUsers ?: -1f
    val pricePerNight: CharSequence get() = priceFormatter(resources, hotel.lowRateInfo, false, !hotel.isPackage)
    val pricePerNightColor: Int get() = ContextCompat.getColor(context, getPricePerNightTextColor())

    val topAmenityTitle: String get() = getTopAmenityTitle(resources)
    val loyaltyAvailable: Boolean get() = hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false
    val showDiscount: Boolean get() = (hotel.lowRateInfo?.isDiscountPercentNotZero ?: false) && !(hotel.lowRateInfo?.airAttached ?: false) && !loyaltyAvailable
    val hotelDiscountPercentage: String get() = Phrase.from(resources, R.string.hotel_discount_percent_Template).put("discount", hotel.lowRateInfo?.discountPercent?.toInt() ?: 0).format().toString()

    val hotelStarRating: Float get() = hotel.hotelStarRating
    val showStarRating: Boolean get() = hotelStarRating > 0
    val hotelGuestRating: Float get() = hotel.hotelGuestRating ?: 0f
    val isHotelGuestRatingAvailable: Boolean get() = hotelGuestRating > 0
    val showNoGuestRating: Boolean get() = !isHotelGuestRatingAvailable
    val showHotelPreviewRating: Boolean get() = hotelStarRating >= 0.5f
    val showHotelAmenityOrDistance: Boolean get() = hotel.proximityDistanceInMiles > 0 || hotel.proximityDistanceInKiloMeters > 0

    val earnMessage: String get() = LoyaltyUtil.getEarnMessagingString(context, hotel.isPackage, hotel.lowRateInfo?.loyaltyInfo?.earn, hotel.packageOfferModel?.loyaltyInfo?.earn)
    val showEarnMessage: Boolean get() = LoyaltyUtil.shouldShowEarnMessage(earnMessage, hotel.isPackage)
    val showAirAttachWithDiscountLabel: Boolean get() = (hotel.lowRateInfo?.isShowAirAttached() ?: false) && !loyaltyAvailable
    val showAirAttachIconWithoutDiscountLabel: Boolean get() = (hotel.lowRateInfo?.isShowAirAttached() ?: false) && loyaltyAvailable

    init {
        soldOut.subscribe { soldOut ->
            isHotelSoldOut = soldOut
        }
    }

    val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

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

    fun getImageColorFilter() : ColorMatrixColorFilter? {
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

    fun shouldShowStrikeThroughPrice(): Boolean {
        if (isHotelSoldOut) {
            return false
        }

        if (hotel.isPackage) {
            val showPackageTripSavings = hotel.packageOfferModel?.price?.showTripSavings ?: false
            return showPackageTripSavings
        } else if (hotel.lowRateInfo?.loyaltyInfo?.isBurnApplied ?: false) {
            return true
        } else if (!(hotel.lowRateInfo?.isShowAirAttached() ?: false) &&
            !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)) {
            return priceToShowUsers < strikeThroughPriceToShowUsers
        } else {
            return false
        }
    }

    fun getHotelLargeThumbnailUrl(): String {
        if (hotel.isPackage) {
            return hotel.thumbnailUrl ?: ""
        }

        return Images.getMediaHost() + hotel.largeThumbnailUrl
    }

    fun showVipMessage(): Boolean {
        return (hotel.isVipAccess ?: false) && PointOfSale.getPointOfSale().supportsVipAccess()
    }

    fun showVipLoyaltyMessage(): Boolean {
        val isMidOrTopTier = Db.getUser()?.loyaltyMembershipInformation?.loyaltyMembershipTier?.isMidOrTopTier ?: false
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
        val soldOutUrgency = getSoldOutUrgencyMessage()

        if (soldOutUrgency != null) {
            return soldOutUrgency
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

    open fun hasMemberDeal(hotel: Hotel): Boolean {
        return hotel.isMemberDeal && userStateManager.isUserAuthenticated()
    }

    fun getRatingContentDesc(hotel: Hotel): String {
        val phrase: Phrase
        val hotelStarRatingContentDescription = HotelsV2DataUtil.getHotelRatingContentDescription(context, hotel.hotelStarRating.toInt())

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

        if (shouldShowStrikeThroughPrice()) {
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

    private fun getSoldOutUrgencyMessage(): UrgencyMessage? {
        if (isHotelSoldOut) {
            return UrgencyMessage(null, R.color.hotel_sold_out_color, resources.getString(R.string.trip_bucket_sold_out))
        }
        return null
    }

    private fun getMemberDealUrgencyMessage(): UrgencyMessage? {
        if (hasMemberDeal(hotel)) {
            return UrgencyMessage(R.drawable.ic_hotel_member, R.color.hotel_member_pricing_bg_color,
                    resources.getString(R.string.member_pricing), R.color.hotel_member_pricing_text_color)

        }
        return null
    }

    private fun getFewRoomsLeftUrgencyMessage(): UrgencyMessage? {
        if (hotel.roomsLeftAtThisRate > 0 && hotel.roomsLeftAtThisRate <= ROOMS_LEFT_CUTOFF_FOR_DECIDING_URGENCY) {
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
            if (ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable == 0) {
                return UrgencyMessage(R.drawable.mobile_exclusive, R.color.hotel_mobile_exclusive_color,
                        resources.getString(R.string.mobile_exclusive))
            } else {
                return UrgencyMessage(ProductFlavorFeatureConfiguration.getInstance().hotelDealImageDrawable,
                        R.color.hotel_mobile_exclusive_color, "")
            }
        }
        return null
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
        return R.color.hotel_cell_gray_text
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
