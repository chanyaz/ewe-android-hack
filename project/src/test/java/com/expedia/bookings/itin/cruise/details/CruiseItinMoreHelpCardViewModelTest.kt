package com.expedia.bookings.itin.cruise.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasURLAnchor
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CruiseItinMoreHelpCardViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var vm: CruiseItinMoreHelpCardViewModel<MockScope>
    lateinit var webViewLauncher: MockWebViewLauncher

    @Before
    fun setup() {
        vm = CruiseItinMoreHelpCardViewModel(MockScope())
        webViewLauncher = vm.scope.mockWebViewLauncher
    }

    @Test
    fun testClickingMoreHelpGoesToWebView() {
        assertNull(webViewLauncher.lastSeenTitle)
        assertNull(webViewLauncher.lastSeenURL)
        assertNull(webViewLauncher.lastSeenTripId)
        assertFalse(webViewLauncher.shouldScrapTitle)
        assertFalse(webViewLauncher.isGuest)

        vm.cardClickListener.invoke()

        assertEquals(R.string.itin_more_help_text, webViewLauncher.lastSeenTitle)
        assertEquals("https://wwwexpediacom.integration.sb.karmalab.net/trips/71296028520", webViewLauncher.lastSeenURL)
        assertEquals("8a246ebb-ef3d-43cc-aa9e-0bede99e38bd", webViewLauncher.lastSeenTripId)
        assertFalse(webViewLauncher.shouldScrapTitle)
        assertFalse(webViewLauncher.isGuest)
        assertEquals("moreHelp", vm.scope.urlAnchor)
    }

    @Test
    fun testClickingMoreHelpWithEmptyTripDoesNothing() {
        vm = CruiseItinMoreHelpCardViewModel(MockScope(itin = ItinMocker.emptyTrip))
        webViewLauncher = vm.scope.mockWebViewLauncher

        vm.cardClickListener.invoke()

        assertNull(webViewLauncher.lastSeenTitle)
        assertNull(webViewLauncher.lastSeenURL)
        assertNull(webViewLauncher.lastSeenTripId)
        assertFalse(webViewLauncher.shouldScrapTitle)
        assertFalse(webViewLauncher.isGuest)
    }

    @Test
    fun testIconImageAndHeadingTexts() {
        val vm = CruiseItinMoreHelpCardViewModel(MockScope())
        assertEquals(R.string.itin_more_help_text.toString(), vm.headingText)
        assertEquals(R.string.itin_customer_support_info_text.toString(), vm.subheadingText)
        assertEquals(R.drawable.ic_itin_manage_booking_icon, vm.iconImage)
    }

    class MockScope(itin: Itin = ItinMocker.cruiseDetailsHappy) : HasItinRepo, HasWebViewLauncher, HasStringProvider, HasTripsTracking, HasURLAnchor {
        override val strings: StringSource = MockStringProvider()
        override val tripsTracking: ITripsTracking = MockTripsTracking()
        val mockWebViewLauncher = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
        override val urlAnchor: String = "moreHelp"
        override val itinRepo: ItinRepoInterface = MockItinRepo()

        init {
            itinRepo.liveDataItin.value = itin
        }
    }
}
