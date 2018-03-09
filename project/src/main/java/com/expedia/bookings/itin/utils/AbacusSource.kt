package com.expedia.bookings.itin.utils

import com.expedia.bookings.data.abacus.ABTest

interface AbacusSource {
    fun isBucketedForTest(abacusTest: ABTest): Boolean
}
