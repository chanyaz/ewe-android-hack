package com.expedia.bookings

import com.expedia.bookings.analytics.AnalyticsProvider
import org.mockito.Mockito

class OmnitureTestUtils : ADMS_Measurement() {
    companion object {
        @JvmStatic fun setMockAnalyticsProvider(): AnalyticsProvider {
            val mock = Mockito.mock(AnalyticsProvider::class.java)
            setAnalyticsProviderForTest(mock)
            return mock
        }

        @JvmStatic fun setNormalAnalyticsProvider() {
            setAnalyticsProviderForTest(null)
        }
    }
}