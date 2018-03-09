package com.expedia.bookings.itin.helpers

import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.itin.utils.AbacusSource

class MockAbacusSource(val truth: Boolean) : AbacusSource {
    var called = false
    override fun isBucketedForTest(abacusTest: ABTest): Boolean {
        called = true
        return truth
    }
}
