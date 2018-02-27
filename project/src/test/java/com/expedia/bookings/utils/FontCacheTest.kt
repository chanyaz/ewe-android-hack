package com.expedia.bookings.utils

import android.graphics.Typeface
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.fonts.TestFontProvider
import com.expedia.bookings.test.MockMultipleClientLogsService
import com.expedia.bookings.test.MockTimeSource
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.HashMap
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FontCacheTest {

    val context = RuntimeEnvironment.application

    lateinit var fontProvider: TestFontProvider
    lateinit var mockMultipleClientLogsService: MockMultipleClientLogsService
    lateinit var mockTimeNowSource: MockTimeSource
    lateinit var cachedFonts: HashMap<FontCache.Font, Typeface>

    @Before
    fun setup() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.DownloadableFonts)
        mockTimeNowSource = MockTimeSource()
        mockMultipleClientLogsService = MockMultipleClientLogsService()
        fontProvider = TestFontProvider()
    }

    @Test
    fun testDownloadAllFonts() {

        cachedFonts = HashMap<FontCache.Font, Typeface>()
        mockTimeNowSource.timeNow = 1
        FontCache.downloadFonts(context, fontProvider, mockMultipleClientLogsService, cachedFonts, mockTimeNowSource)

        assertEquals(1011, cachedFonts.get(FontCache.Font.ROBOTO_LIGHT)?.style)
        assertEquals(0, cachedFonts.get(FontCache.Font.ROBOTO_BOLD)?.style)
        assertEquals(1011, cachedFonts.get(FontCache.Font.ROBOTO_MEDIUM)?.style)
        assertEquals(1011, cachedFonts.get(FontCache.Font.ROBOTO_REGULAR)?.style)
        assertEquals(4, cachedFonts.size)

        assertEquals("App.DownloadableFonts.Success", mockMultipleClientLogsService.clientLogList.get(0).pageName)
        assertEquals("ROBOTO_LIGHT", mockMultipleClientLogsService.clientLogList.get(0).eventName)
        assertEquals("App.DownloadableFonts.Success", mockMultipleClientLogsService.clientLogList.get(1).pageName)
        assertEquals("ROBOTO_MEDIUM", mockMultipleClientLogsService.clientLogList.get(1).eventName)
        assertEquals("App.DownloadableFonts.Failure", mockMultipleClientLogsService.clientLogList.get(2).pageName)
        assertEquals("ROBOTO_BOLD", mockMultipleClientLogsService.clientLogList.get(2).eventName)
        assertEquals("App.DownloadableFonts.Success", mockMultipleClientLogsService.clientLogList.get(3).pageName)
        assertEquals("ROBOTO_REGULAR", mockMultipleClientLogsService.clientLogList.get(3).eventName)

        assertEquals(0, mockMultipleClientLogsService.clientLogList.get(0).responseTime)
    }
}
