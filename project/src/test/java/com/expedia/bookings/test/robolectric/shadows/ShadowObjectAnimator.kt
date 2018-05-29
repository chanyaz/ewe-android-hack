package com.expedia.bookings.test.robolectric.shadows

import android.animation.ObjectAnimator
import org.robolectric.annotation.Implements

@Implements(ObjectAnimator::class)
class ShadowObjectAnimator : ShadowValueAnimator()
