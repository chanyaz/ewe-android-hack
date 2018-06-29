package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.itin.cars.details.CarItinMoreHelpCardViewModel
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarItinMoreHelpCardViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: CarItinMoreHelpCardViewModel<MockScope>
    lateinit var scope: MockScope
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        scope = MockScope()
        sut = CarItinMoreHelpCardViewModel(scope)
    }

    @Test
    fun moreHelpClickListenerTest() {
        assertFalse(scope.mockTracking.trackItinCarMoreHelpClickedCalled)
        assertFalse(scope.mockActivityLauncher.intentableActivityLaunched)

        sut.cardClickListener.invoke()

        assertTrue(scope.mockTracking.trackItinCarMoreHelpClickedCalled)
        assertTrue(scope.mockActivityLauncher.intentableActivityLaunched)
    }

    class MockScope : HasItinRepo, HasLifecycleOwner, HasActivityLauncher, HasStringProvider, HasTripsTracking {
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val strings: StringSource = MockStringProvider()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        val mockActivityLauncher = MockActivityLauncher()
        override val activityLauncher: IActivityLauncher = mockActivityLauncher
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking

        init {
            itinRepo.liveDataItin.value = ItinMocker.carDetailsHappy
        }
    }
}
