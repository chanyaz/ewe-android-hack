package com.expedia.bookings.shared

import android.os.SystemClock
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DebounceOnClickListenerTest {

    private var currentTime = 1000L
    private var count = 0
    private val listener = DebounceOnClickListener({
        count++
    })

    @Before
    fun before() {
        currentTime = 1000L
        SystemClock.setCurrentTimeMillis(currentTime)
        count = 0
    }

    @Test
    fun testFastDoubleClick() {
        listener.onClick(null)
        listener.onClick(null)

        assertEquals(1, count)
    }

    @Test
    fun testAverageDoubleClick() {
        listener.onClick(null)
        advanceTime(110)
        listener.onClick(null)

        assertEquals(1, count)
    }
    @Test
    fun testSlowDoubleClick() {
        listener.onClick(null)
        advanceTime(700)
        listener.onClick(null)

        assertEquals(2, count)
    }

    @Test
    fun testDebounceTimeout() {
        var count = 0
        val listener = DebounceOnClickListener({
            count++
        }, 250)

        listener.onClick(null)
        advanceTime(150)
        listener.onClick(null)

        assertEquals(1, count)

        advanceTime(350)
        listener.onClick(null)
        advanceTime(500)
        listener.onClick(null)

        assertEquals(3, count)
    }

    private fun advanceTime(milliSec: Long) {
        currentTime += milliSec
        SystemClock.setCurrentTimeMillis(currentTime)
    }
}
