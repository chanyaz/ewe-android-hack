package com.expedia.bookings.tracking.hotel

import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageUsableDataTest {

    @Test
    fun oneMilliRoundsDownToZero() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(1)

        assertEquals("0.00", sut.getLoadTimeInSeconds())
    }

    @Test
    fun sixMillisRoundsUpToOneHundredth() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(6)

        assertEquals("0.01", sut.getLoadTimeInSeconds())
    }

    @Test
    fun ninetyNineMillisRoundsUpToOneTenth() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(99)

        assertEquals("0.10", sut.getLoadTimeInSeconds())
    }

    @Test
    fun nineHundredNinetyNineMillisRoundsUpToOneSecond() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(999)

        assertEquals("1.00", sut.getLoadTimeInSeconds())
    }

    @Test
    fun nineHundredNinetyMillisIsExactlyNinetyNineHundredths() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(990)

        assertEquals("0.99", sut.getLoadTimeInSeconds())
    }

    @Test
    fun germanyUsesPeriodInsteadOfComma() {
        val originalLocale = Locale.getDefault()
        val sut = PageUsableData()
        Locale.setDefault(Locale.GERMANY)

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(1000)

        assertEquals("1.00", sut.getLoadTimeInSeconds())

        Locale.setDefault(originalLocale)
    }

    @Test
    fun secondCallToGetLoadTimeYieldsNull() {
        val sut = PageUsableData()

        sut.markPageLoadStarted(0)
        sut.markAllViewsLoaded(1000)

        assertEquals("1.00", sut.getLoadTimeInSeconds())
        assertNull(sut.getLoadTimeInSeconds())
    }
}
