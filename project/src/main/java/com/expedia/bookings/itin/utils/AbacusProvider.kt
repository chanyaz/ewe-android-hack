package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

class AbacusProvider(private val context: Context) : AbacusSource {
    override fun isBucketedForTest(abacusTest: ABTest): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, abacusTest)
    }
}
