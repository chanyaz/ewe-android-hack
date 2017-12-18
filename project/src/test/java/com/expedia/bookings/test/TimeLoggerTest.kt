package com.expedia.bookings.test

import com.expedia.bookings.tracking.TimeLogger
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeLoggerTest {
    lateinit var logger: TimeLogger
    lateinit var mockTimeNowSource: MockTimeSource

    @Before
    fun setup() {
        mockTimeNowSource = MockTimeSource()
        logger = TimeLogger(timeSource = mockTimeNowSource, pageName = "Mock.Time")
    }

    @Test
    fun testAppStartupTimeLogging() {
        mockTimeNowSource.timeNow = 1
        logger.setStartTime()

        mockTimeNowSource.timeNow = 2
        logger.setEndTime()

        assertEquals(1, logger.calculateTotalTime())
    }

    @Test
    fun testTimeLoggingHasNotStarted() {
        assertFalse(logger.isComplete())
    }

    @Test
    fun testTimeLoggingIncomplete() {
        logger.setStartTime()
        assertFalse(logger.isComplete())
    }

    @Test
    fun testTimeLoggingComplete() {
        logger.setStartTime()
        logger.setEndTime()
        assertTrue(logger.isComplete())
    }
}
