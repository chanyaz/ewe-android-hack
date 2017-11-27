package com.expedia.bookings.data.extensions

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.isHotelMaterialForms

class LineOfBusinessExtensions {
    companion object {
        fun isUniversalCheckout(lob: LineOfBusiness, context: Context): Boolean {
            return lob == LineOfBusiness.FLIGHTS_V2 || lob == LineOfBusiness.PACKAGES || (isLXUniversalCheckout(context) && lob == LineOfBusiness.LX)
        }

        private fun isLXUniversalCheckout(context: Context): Boolean {
            return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_universal_checkout_on_lx) && AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppBringUniversalCheckoutToLX)
        }
    }
}

fun LineOfBusiness.isMaterialLineOfBusiness(): Boolean {
    return this != LineOfBusiness.FLIGHTS && this != LineOfBusiness.ITIN
}

fun LineOfBusiness.isUniversalCheckout(context: Context): Boolean {
    return LineOfBusinessExtensions.isUniversalCheckout(this, context)
}

fun LineOfBusiness.hasBillingInfo(): Boolean {
    return this == LineOfBusiness.FLIGHTS_V2 || this == LineOfBusiness.PACKAGES || this == LineOfBusiness.FLIGHTS
}

fun LineOfBusiness.isMaterialFormEnabled(context: Context) : Boolean {
    return (LineOfBusinessExtensions.isUniversalCheckout(this, context) || this.isMaterialHotelEnabled(context))
}

fun LineOfBusiness.isMaterialHotelEnabled(context: Context): Boolean {
    return this == LineOfBusiness.HOTELS && isHotelMaterialForms(context)
}
