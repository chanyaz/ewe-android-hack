package com.expedia.bookings.itin.widget.hotel

import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockAbacusSource
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.hotel.details.HotelItinPriceSummaryButtonViewModel
import com.expedia.bookings.itin.scopes.HasAbacusProvider
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasHotel
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.packagePrice
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelItinPriceSummaryButtonViewModelTest {

    @Test
    fun testNoPriceDetails() {
        val scope = TestHotelDetailsScopeNoPriceDetails()
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertEquals(R.drawable.ic_itin_credit_card_icon, viewModel.iconImage)

        assertEquals(R.string.itin_hotel_details_price_summary_rewards_heading, scope.mockStrings.lastSeenFetchArgs)
        assertEquals("someString", viewModel.headingText)

        assertEquals(null, viewModel.subheadingText)
        assertEquals(null, scope.mockStrings.lastSeenFetchWithMapArgs)

        viewModel.cardClickListener.invoke()
        assertEquals("7331281600686", scope.mockWebViewLauncher.lastSeenTripId)
        assertEquals("https://www.expedia.com/trips/7331281600686", scope.mockWebViewLauncher.lastSeenURL)
        assertEquals(R.string.itin_hotel_details_price_summary_heading, scope.mockWebViewLauncher.lastSeenTitle)
        assertTrue(scope.mockTripsTracking.trackHotelItinPricingRewardsClicked)
    }

    @Test
    fun testHappyUnBucketedPath() {
        val scope = TestHotelDetailsScopeHappy(false)
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertEquals(R.drawable.ic_itin_credit_card_icon, viewModel.iconImage)

        assertEquals(R.string.itin_hotel_details_price_summary_rewards_heading, scope.mockStrings.lastSeenFetchArgs)
        assertEquals("someString", viewModel.headingText)

        assertEquals("somePhraseString", viewModel.subheadingText)
        assertEquals(Pair(R.string.itin_hotel_details_price_summary_pay_later_TEMPLATE, mapOf("amount" to ItinMocker.hotelDetailsHappy.firstHotel()?.totalPriceDetails?.totalFormatted!!)), scope.mockStrings.lastSeenFetchWithMapArgs)

        viewModel.cardClickListener.invoke()
        assertEquals("7280999576135", scope.mockWebViewLauncher.lastSeenTripId)
        assertEquals("https://www.expedia.com/trips/7280999576135", scope.mockWebViewLauncher.lastSeenURL)
        assertEquals(R.string.itin_hotel_details_price_summary_heading, scope.mockWebViewLauncher.lastSeenTitle)
        assertTrue(scope.mockTripsTracking.trackHotelItinPricingRewardsClicked)
    }

    @Test
    fun testHappyBucketedPath() {
        val scope = TestHotelDetailsScopeHappy(true)
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertFalse(scope.activityMockLauncher.launched)
        viewModel.cardClickListener.invoke()
        assertTrue(scope.activityMockLauncher.launched)
        assertTrue(scope.mockTripsTracking.trackHotelItinPricingRewardsClicked)
    }

    @Test
fun testPackageHappyPath() {
        val scope = TestPackageHotelDetailsScopeHappy(true)
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertEquals("somePhraseString", viewModel.subheadingText)
        assertEquals(Pair(R.string.itin_hotel_details_price_summary_pay_now_TEMPLATE, mapOf("amount" to ItinMocker.hotelPackageHappy.packagePrice()!!)), scope.mockStrings.lastSeenFetchWithMapArgs)
    }
}

class TestHotelDetailsScopeNoPriceDetails : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher, HasTripsTracking, HasActivityLauncher, HasAbacusProvider {
    override val activityLauncher: IActivityLauncher = MockActivityLauncher()
    override val abacus: AbacusSource = MockAbacusSource(false)
    override val itin: Itin = ItinMocker.hotelDetailsNoPriceDetails
    override val hotel: ItinHotel = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()!!
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
}

class TestHotelDetailsScopeHappy(bucketed: Boolean) : HasItin, HasHotel, HasStringProvider, HasActivityLauncher, HasWebViewLauncher, HasTripsTracking, HasAbacusProvider {
    val activityMockLauncher = MockActivityLauncher()
    override val activityLauncher: IActivityLauncher = activityMockLauncher
    override val abacus: AbacusSource = MockAbacusSource(bucketed)
    override val itin: Itin = ItinMocker.hotelDetailsHappy
    override val hotel: ItinHotel = ItinMocker.hotelDetailsHappy.firstHotel()!!
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
}

class TestPackageHotelDetailsScopeHappy(bucketed: Boolean) : HasItin, HasHotel, HasStringProvider, HasActivityLauncher, HasWebViewLauncher, HasTripsTracking, HasAbacusProvider {
    val activityMockLauncher = MockActivityLauncher()
    override val activityLauncher: IActivityLauncher = activityMockLauncher
    override val abacus: AbacusSource = MockAbacusSource(bucketed)
    override val itin: Itin = ItinMocker.hotelPackageHappy
    override val hotel: ItinHotel = ItinMocker.hotelPackageHappy.firstHotel()!!
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
}
