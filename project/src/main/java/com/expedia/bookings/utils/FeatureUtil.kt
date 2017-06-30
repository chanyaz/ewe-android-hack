package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale

fun isMaterialFormsEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
}

fun isDisabledSTPStateEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppDisabledSTPState)
}
fun isFlexEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_flex)
}

fun isReverseNameEnabled(context:Context): Boolean {
    return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_reverse_traveler_name)
            && (PointOfSale.getPointOfSale().showLastNameFirst() || PointOfSale.getPointOfSale().hideMiddleName())
}