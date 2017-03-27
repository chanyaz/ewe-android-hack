package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils

fun isMaterialFormsEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)
}