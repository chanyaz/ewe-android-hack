package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.LoyaltyUtil
import rx.subjects.BehaviorSubject

abstract class BaseHotelDetailPriceViewModel(val context: Context) {

    abstract fun getPriceString(): String?
    abstract fun getPerDescriptorString(): String?
    abstract fun getSearchInfoString(): String
    abstract fun getPriceContainerContentDescriptionString(): String

    val isSoldOut = BehaviorSubject.create<Boolean>(false)

    protected lateinit var offerResponse: HotelOffersResponse
    protected var chargeableRateInfo: HotelRate? = null
    protected var isShopWithPointsRate = false
    protected var isAirAttached = false
    protected var isBucketToHideStrikeThroughPrice = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)
    protected var showTotalPrice = false

    protected lateinit var hotelSearchParams: HotelSearchParams

    fun bind(offerResponse: HotelOffersResponse, hotelSearchParams: HotelSearchParams) {
        this.offerResponse = offerResponse
        chargeableRateInfo = offerResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        isShopWithPointsRate = chargeableRateInfo?.loyaltyInfo?.isBurnApplied ?: false
        isAirAttached = chargeableRateInfo?.isShowAirAttached() ?: false
        showTotalPrice = chargeableRateInfo?.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES

        this.hotelSearchParams = hotelSearchParams
    }

    fun getStrikeThroughPriceString(): String? {
        if (!shouldShowStrikeThroughPrice()) {
            return null
        }
        val formattedStrikeThroughPriceString = priceFormatter(context.resources, chargeableRateInfo, true, !offerResponse.isPackage).toString()
        return if (formattedStrikeThroughPriceString.isNullOrBlank()) null else formattedStrikeThroughPriceString
    }

    fun getTaxFeeDescriptorString(): String? {
        if (isSoldOut.value) {
            return null
        }

        val bucketedToShowPriceDescriptorProminence = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)
        if (bucketedToShowPriceDescriptorProminence) {
            val priceType = chargeableRateInfo?.getUserPriceType()

            return when (priceType) {
                HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> context.getString(R.string.total_including_taxes_fees)
                HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES -> context.getString(R.string.excluding_taxes_fees)
                else -> null
            }
        }
        return null
    }

    fun getEarnMessageString(): String? {
        if (isSoldOut.value) {
            return null
        }
        val packageLoyaltyInformation = offerResponse.hotelRoomResponse?.firstOrNull()?.packageLoyaltyInformation
        val earnMessage = LoyaltyUtil.getEarnMessagingString(context, offerResponse.isPackage, chargeableRateInfo?.loyaltyInfo?.earn, packageLoyaltyInformation?.earn)
        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage(earnMessage, offerResponse.isPackage)
        if (shouldShowEarnMessage) {
            return earnMessage
        } else {
            return null
        }
    }

    fun getSearchInfoTextColor(): Int {
        if (isSoldOut.value) {
            return ContextCompat.getColor(context, R.color.gray3)
        } else {
            return ContextCompat.getColor(context, R.color.gray6)
        }
    }

    protected fun shouldShowStrikeThroughPrice(): Boolean {
        if (chargeableRateInfo != null && !isSoldOut.value) {
            return isShopWithPointsRate || (!isBucketToHideStrikeThroughPrice && !isAirAttached)
        }
        return false
    }
}
