package com.expedia.util

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration

class LoyaltyUtil {
    companion object {
        fun isShopWithPointsAvailable(context: Context): Boolean {
            return User.isLoggedIn(context) && PointOfSale.getPointOfSale().isSWPEnabledForHotels
                    && Db.getUser()?.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false
        }

        fun shouldShowEarnMessage(earnMessage: String, isPackage: Boolean): Boolean {
            val userBucketed = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelLoyaltyEarnMessage)
            val forceShow = ProductFlavorFeatureConfiguration.getInstance().forceShowHotelLoyaltyEarnMessage()
            val validMessage = earnMessage.isNotBlank()
            val enabledForPOS = if (isPackage) PointOfSale.getPointOfSale().isEarnMessageEnabledForPackages else PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels
            return (forceShow || userBucketed) && validMessage && enabledForPOS
        }

        fun getEarnMessagingString(context: Context, isPackage: Boolean, hotelEarnInfo: LoyaltyEarnInfo?, packageEarnInfo: LoyaltyEarnInfo?): String {
            if (isPackage) {
                return packageEarnInfo?.getEarnMessage(context) ?: ""
            }
            return hotelEarnInfo?.getEarnMessage(context) ?: ""
        }
    }
}
