package com.expedia.bookings.itin.common

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasURLAnchor
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinPriceSummaryCardViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Test
    fun testItinCarPriceSummaryClicked() {
        val scope = MockScope(ItinMocker.carDetailsHappy, TripProducts.CAR.name)
        val sut = ItinPriceSummaryCardViewModel(scope)

        assertFalse(scope.mockTracking.trackItinCarPriceSummaryClicked)

        sut.cardClickListener.invoke()

        assertTrue(scope.mockTracking.trackItinCarPriceSummaryClicked)
    }

    @Test
    fun testItinActivityPriceSummaryClicked() {
        val scope = MockScope(ItinMocker.lxDetailsHappy, TripProducts.ACTIVITY.name)
        val sut = ItinPriceSummaryCardViewModel(scope)

        assertFalse(scope.mockTracking.trackItinActivityPriceSummaryClicked)

        sut.cardClickListener.invoke()

        assertTrue(scope.mockTracking.trackItinActivityPriceSummaryClicked)
    }

    private class MockScope(itin: Itin, lobType: String) : HasStringProvider, HasWebViewLauncher, HasItinRepo, HasItinType, HasTripsTracking, HasURLAnchor {
        val mockTracking = MockTripsTracking()
        override val strings: StringSource = MockStringProvider()
        override val tripsTracking: ITripsTracking = mockTracking
        override val webViewLauncher = MockWebViewLauncher()
        override val type = lobType
        override val urlAnchor: String = "anchor"
        override val itinRepo: ItinRepoInterface = MockItinRepo()

        init {
            itinRepo.liveDataItin.value = itin
        }
    }
}
