package com.expedia.bookings.test

import com.expedia.bookings.tracking.AppStartupTimeClientLog
import com.expedia.bookings.tracking.RouterToSignInTimeLogger
import com.expedia.bookings.tracking.TimeLogger
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppStartUpTimeClientLogTest {

    lateinit var mockClientLogServices: MockClientLogServices
    lateinit var logger: TimeLogger
    lateinit var mockTimeNowSource: MockTimeSource

    @Before
    fun setup() {
        mockClientLogServices = MockClientLogServices()
        mockTimeNowSource = MockTimeSource()
        logger = TimeLogger(timeSource = mockTimeNowSource, pageName = "Mock.Time")
    }

    @Test
    fun testTimeLoggerTrackedAndCleared() {
        mockTimeNowSource.timeNow = 1
        logger.setStartTime()
        mockTimeNowSource.timeNow = 2
        logger.setEndTime()

        AppStartupTimeClientLog.trackTimeLogger(logger, mockClientLogServices)
        assertEquals("Mock.Time", mockClientLogServices.lastSeenClientLog?.pageName)
        assertEquals(1, mockClientLogServices.lastSeenClientLog?.requestToUser)
        assertFalse(logger.isComplete())
    }

    @Test
    fun testTimeLoggerShouldNotBeTracked() {
        AppStartupTimeClientLog.trackTimeLogger(logger, mockClientLogServices)
        assertTrue(mockClientLogServices.lastSeenClientLog == null)
    }

    @Test
    fun testRouterToSignInTrackedAndGoToSignInCleared() {
        val logger = RouterToSignInTimeLogger()
        logger.setStartTime()
        logger.setEndTime()

        logger.shouldGoToSignIn = true
        AppStartupTimeClientLog.trackTimeLogger(logger, mockClientLogServices)
        assertEquals("Router.To.SignIn.Time", mockClientLogServices.lastSeenClientLog?.pageName)
        assertFalse(logger.shouldGoToSignIn)
    }
}
