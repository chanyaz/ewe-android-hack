package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LxItinManageBookingWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var sut: LxItinManageBookingWidgetViewModel<MockLxItinManageBookingWidgetViewModelScope>
    lateinit var scope: MockLxItinManageBookingWidgetViewModelScope
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

    }

    @Test
    fun itinMoreHelpCardViewModelTest() {
        setupHappy()
        assertEquals((R.string.itin_lx_more_info_heading).toString(), sut.moreHelpViewModel.headingText)
        assertEquals((R.string.itin_lx_more_info_subheading).toString(), sut.moreHelpViewModel.subheadingText)
    }

    @Test
    fun itinLxPriceSummaryCardViewModelHappyTest() {
        setupHappy()
        assertEquals((R.string.itin_hotel_details_price_summary_heading).toString(), sut.priceSummaryViewModel.headingText)
        assertNull(sut.priceSummaryViewModel.subheadingText)
        assertNull(scope.webViewLauncerMock.lastSeenURL)

        sut.priceSummaryViewModel.cardClickListener.invoke()
        assertEquals("https://wwwexpediacom.trunk.sb.karmalab.net/trips/71196729802", scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxPriceSummaryCardViewModelUnHappyNoIDTest() {
        scope = MockLxItinManageBookingWidgetViewModelScope(ItinMocker.lxDetailsNoTripID)
        sut = LxItinManageBookingWidgetViewModel(scope)
        sut.priceSummaryViewModel.cardClickListener.invoke()
        assertNull(scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxPriceSummaryCardViewModelUnHappyNoUrlTest() {
        scope = MockLxItinManageBookingWidgetViewModelScope(ItinMocker.lxDetailsNoDetailsUrl)
        sut = LxItinManageBookingWidgetViewModel(scope)
        sut.priceSummaryViewModel.cardClickListener.invoke()
        assertNull(scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxAdditionalInfoCardViewModelHappyTest() {
        setupHappy()
        assertEquals((R.string.itin_hotel_details_additional_info_heading).toString(), sut.additionalInfoViewModel.headingText)
        assertNull(sut.additionalInfoViewModel.subheadingText)
        assertNull(scope.webViewLauncerMock.lastSeenURL)

        sut.additionalInfoViewModel.cardClickListener.invoke()
        assertEquals("https://wwwexpediacom.trunk.sb.karmalab.net/trips/71196729802", scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxAdditionalInfoCardViewModelUnHappyNoIdTest() {
        scope = MockLxItinManageBookingWidgetViewModelScope(ItinMocker.lxDetailsNoTripID)
        sut = LxItinManageBookingWidgetViewModel(scope)
        sut.additionalInfoViewModel.cardClickListener.invoke()
        assertNull(scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxAdditionalInfoCardViewModelUnHappyNoUrlTest() {
        scope = MockLxItinManageBookingWidgetViewModelScope(ItinMocker.lxDetailsNoDetailsUrl)
        sut = LxItinManageBookingWidgetViewModel(scope)
        sut.additionalInfoViewModel.cardClickListener.invoke()
        assertNull(scope.webViewLauncerMock.lastSeenURL)
    }

    @Test
    fun itinLxMoreHelpClickTracked() {
        setupHappy()
        sut.moreHelpViewModel.cardClickListener.invoke()
        assertTrue(scope.tripsTracking.trackItinLxMoreHelpClicked)
    }

    fun setupHappy() {
        scope = MockLxItinManageBookingWidgetViewModelScope()
        sut = LxItinManageBookingWidgetViewModel(scope)
    }

    class MockLxItinManageBookingWidgetViewModelScope(itin: Itin = ItinMocker.lxDetailsHappy) : HasWebViewLauncher, HasActivityLauncher, HasLxRepo, HasStringProvider, HasTripsTracking {
        override val strings: StringSource = MockStringProvider()
        val webViewLauncerMock = MockWebViewLauncher()
        override val webViewLauncher: IWebViewLauncher = webViewLauncerMock
        override val activityLauncher: IActivityLauncher = MockActivityLauncher()
        val mockRepo = MockLxRepo(itin = itin)
        override val itinLxRepo: ItinLxRepoInterface = mockRepo
        override val tripsTracking = MockTripsTracking()
    }
}
