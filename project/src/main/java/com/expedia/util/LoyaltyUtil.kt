package com.expedia.util

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration

class LoyaltyUtil {
    companion object {
        fun isShopWithPointsAvailable(userStateManager: UserStateManager): Boolean {
            return userStateManager.isUserAuthenticated() && PointOfSale.getPointOfSale().isSWPEnabledForHotels
                    && Db.getUser()?.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false
        }

        fun shouldShowEarnMessage(earnMessage: String, isPackage: Boolean): Boolean {
            val brandShowLoyaltyEarn = ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()
            val validMessage = earnMessage.isNotBlank()
            val enabledForPOS = if (isPackage) PointOfSale.getPointOfSale().isEarnMessageEnabledForPackages else PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels
            return brandShowLoyaltyEarn && validMessage && enabledForPOS
        }

        fun getEarnMessagingString(context: Context, isPackage: Boolean, hotelEarnInfo: LoyaltyEarnInfo?, packageEarnInfo: LoyaltyEarnInfo?): String {
            if (isPackage) {
                return packageEarnInfo?.getEarnMessage(context) ?: ""
            }
            return hotelEarnInfo?.getEarnMessage(context) ?: ""
        }
    }
}
