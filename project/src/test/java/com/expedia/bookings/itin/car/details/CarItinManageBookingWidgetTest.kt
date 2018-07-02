package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.details.CarItinManageBookingWidgetViewModel
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CarItinManageBookingWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: CarItinManageBookingWidgetViewModel<*>

    @Before
    fun setup() {
        val scope = CarItinManageBookingWidgetViewModelScope()
        sut = CarItinManageBookingWidgetViewModel(scope)
    }

    @Test
    fun itinMoreHelpCardViewModelTest() {
        assertEquals((R.string.itin_more_help_text).toString(), sut.moreHelpViewModel.headingText)
        assertEquals((R.string.itin_customer_support_info_text).toString(), sut.moreHelpViewModel.subheadingText)
    }

    class CarItinManageBookingWidgetViewModelScope : HasWebViewLauncher, HasItinRepo, HasStringProvider, HasTripsTracking, HasActivityLauncher {
        private val mockLauncher = MockActivityLauncher()
        override val activityLauncher: IActivityLauncher = mockLauncher
        override val strings: StringSource = MockStringProvider()
        private val webViewLauncherMock = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = webViewLauncherMock
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override val tripsTracking = MockTripsTracking()

        init {
            itinRepo.liveDataItin.value = ItinMocker.carDetailsHappy
        }
    }
}
