package com.expedia.bookings

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.test.CustomMatchers.Companion.hasEntries
import com.expedia.bookings.test.NullSafeMockitoHamcrest.mapThat
import org.mockito.Mockito

class OmnitureTestUtils : ADMS_Measurement() {
    companion object {
        @JvmStatic
        fun setMockAnalyticsProvider(): AnalyticsProvider {
            val mock = Mockito.mock(AnalyticsProvider::class.java)
            setAnalyticsProviderForTest(mock)
            return mock
        }

        @JvmStatic
        fun setNormalAnalyticsProvider() {
            setAnalyticsProviderForTest(null)
        }

        @JvmStatic
        fun assertNoTrackingHasOccurred(mockAnalyticsProvider: AnalyticsProvider) {
            Mockito.verify(mockAnalyticsProvider, Mockito.never())
                    .trackState(Mockito.anyString(), Mockito.anyMapOf(String::class.java, Any::class.java))
            Mockito.verify(mockAnalyticsProvider, Mockito.never())
                    .trackAction(Mockito.anyString(), Mockito.anyMapOf(String::class.java, Any::class.java))
        }

        @JvmStatic
        fun assertLinkTracked(linkName: String, rfrrId: String, mockAnalyticsProvider: AnalyticsProvider) {
            val expectedData = mapOf(
                    "&&linkType" to "o",
                    "&&linkName" to linkName,
                    "&&v28" to rfrrId,
                    "&&c16" to rfrrId
            )

            Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
        }

        @JvmStatic
        fun assertStateTrackedWithEvents(appState: String, events: String, mockAnalyticsProvider: AnalyticsProvider) {
            val expectedData = mapOf("&&events" to events)

            Mockito.verify(mockAnalyticsProvider).trackState(Mockito.eq(appState), mapThat(hasEntries(expectedData)))
        }
    }
}