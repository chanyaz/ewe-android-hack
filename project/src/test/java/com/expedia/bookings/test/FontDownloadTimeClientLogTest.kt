package com.expedia.bookings.test

import com.expedia.bookings.tracking.DownloadableFontsTimeLogger
import com.expedia.bookings.tracking.FontDownloadTimeClientLog
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FontDownloadTimeClientLogTest {

    lateinit var mockClientLogServices: MockClientLogServices
    lateinit var logger: DownloadableFontsTimeLogger
    lateinit var mockTimeNowSource: MockTimeSource

    @Before
    fun setup() {
        mockClientLogServices = MockClientLogServices()
        mockTimeNowSource = MockTimeSource()
        logger = DownloadableFontsTimeLogger(timeSource = mockTimeNowSource, pageName = "Mock.Time", fontName = "font")
    }

    @Test
    fun testFontNameTracking() {
        mockTimeNowSource.timeNow = 1
        logger.setStartTime()
        mockTimeNowSource.timeNow = 3
        logger.setEndTime()

        FontDownloadTimeClientLog.trackDownloadTimeLogger(logger, mockClientLogServices)

        assertEquals("Mock.Time", mockClientLogServices.lastSeenClientLog?.pageName)
        assertEquals("font", mockClientLogServices.lastSeenClientLog?.eventName)
        assertEquals(2, mockClientLogServices.lastSeenClientLog?.responseTime)
    }
}
