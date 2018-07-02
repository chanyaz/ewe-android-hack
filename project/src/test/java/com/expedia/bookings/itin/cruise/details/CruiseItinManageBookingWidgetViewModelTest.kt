package com.expedia.bookings.itin.cruise.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull

class CruiseItinManageBookingWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var vm: CruiseItinManageBookingWidgetViewModel<MockScope>

    @Test
    fun testViewModelsProperlySetForAllModules() {
        vm = CruiseItinManageBookingWidgetViewModel(MockScope())
        assertNotNull(vm.moreHelpViewModel)
        assertNotNull(vm.priceSummaryViewModel)
        assertNotNull(vm.additionalInfoViewModel)
    }

    class MockScope(override val itin: Itin = ItinMocker.cruiseDetailsHappy) : HasStringProvider, HasItinRepo, HasWebViewLauncher, HasTripsTracking, HasItinType, HasItin {
        override val strings: StringSource = MockStringProvider()
        override val webViewLauncher: IWebViewLauncher = MockWebViewLauncher()
        override val tripsTracking: ITripsTracking = MockTripsTracking()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override val type: String = TripProducts.CRUISE.name

        init {
            itinRepo.liveDataItin.value = itin
        }
    }
}
