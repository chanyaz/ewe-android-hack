package com.expedia.bookings.test

import com.expedia.bookings.tracking.AppStartupTimeClientLog
import com.expedia.bookings.tracking.AppStartupTimeLogger
import org.junit.Test
import kotlin.test.assertEquals

class AppStartUpTimeClientLogTest {

    @Test
    fun testAppStartupTimeLog() {
        val mockClientLogServices = MockClientLogServices()
        val logger = AppStartupTimeLogger()
        logger.setAppLaunchedTime(1349333571111)
        logger.setAppLaunchScreenDisplayed(1349333576093)

        AppStartupTimeClientLog.trackAppStartupTime(logger, mockClientLogServices)
        assertEquals(4982L, mockClientLogServices.lastSeenClientLog?.requestToUser)
    }
}
