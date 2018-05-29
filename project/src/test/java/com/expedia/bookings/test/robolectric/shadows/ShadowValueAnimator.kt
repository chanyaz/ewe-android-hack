package com.expedia.bookings.test.robolectric.shadows

import android.animation.ValueAnimator
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(ValueAnimator::class)
open class ShadowValueAnimator : org.robolectric.shadows.ShadowValueAnimator() {

    @Implementation
    fun setStartDelay(@Suppress("UNUSED_PARAMETER") startDelay: Long) {
        //ignore
    }
}
