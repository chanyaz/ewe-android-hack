package com.expedia.bookings.utils

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

fun isMaterialFormsEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
}