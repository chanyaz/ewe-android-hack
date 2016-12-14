package com.expedia.util

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.pos.PointOfSale

class LoyaltyUtil {
    companion object {
        fun isShopWithPointsAvailable(context: Context) : Boolean {
            return User.isLoggedIn(context) && PointOfSale.getPointOfSale().isSWPEnabledForHotels
                    && Db.getUser()?.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false
        }
    }
}