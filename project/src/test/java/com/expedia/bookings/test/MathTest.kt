package com.expedia.bookings.test

import org.junit.Test
import kotlin.test.assertEquals
import org.junit.Assert

public class ScaleToRangeTest {
    @Test fun scaleRange() {
        assert(Math.abs(.5f - com.expedia.util.scaleValueToRange(.5f, 1f, 0f, 1f, .75f)) < 0.001)
        assert(Math.abs(1f - com.expedia.util.scaleValueToRange(.5f, 1f, 0f, 1f, 1f)) < 0.001)
        assert(Math.abs(0f - com.expedia.util.scaleValueToRange(.5f, 1f, 0f, 1f, .5f)) < 0.001)
        assert(Math.abs(.7f - com.expedia.util.scaleValueToRange(.5f, 1f, 0f, 1f, .85f)) < 0.001)
        assert(Math.abs(3f - com.expedia.util.scaleValueToRange(0f, 1f, 0f, 100f, .03f)) < 0.001)
    }
}
