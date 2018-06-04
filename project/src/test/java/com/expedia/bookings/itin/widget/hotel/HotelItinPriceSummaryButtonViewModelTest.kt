package com.expedia.bookings.itin.widget.hotel

import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockAbacusSource
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockFeature
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.hotel.details.HotelItinPriceSummaryButtonViewModel
import com.expedia.bookings.itin.scopes.HasAbacusProvider
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasFeature
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

        assertEquals((R.string.itin_hotel_details_price_summary_rewards_heading).toString(), viewModel.headingText)

        assertEquals(null, viewModel.subheadingText)

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

        assertEquals((R.string.itin_hotel_details_price_summary_rewards_heading).toString(), viewModel.headingText)

        val expected = (R.string.itin_hotel_details_price_summary_pay_later_TEMPLATE).toString().plus(mapOf("amount" to ItinMocker.hotelDetailsHappy.firstHotel()?.totalPriceDetails?.totalFormatted!!))
        assertEquals(expected, viewModel.subheadingText)

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

        val expected = (R.string.itin_hotel_details_price_summary_pay_now_TEMPLATE).toString().plus(mapOf("amount" to ItinMocker.hotelPackageHappy.packagePrice()!!))
        assertEquals(expected, viewModel.subheadingText)
    }
}

class TestHotelDetailsScopeNoPriceDetails : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher, HasTripsTracking, HasActivityLauncher, HasAbacusProvider, HasFeature {
    override val activityLauncher: IActivityLauncher = MockActivityLauncher()
    override val abacus: AbacusSource = MockAbacusSource(false)
    override val itin: Itin = ItinMocker.hotelDetailsNoPriceDetails
    override val hotel: ItinHotel = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()!!
    override val strings: StringSource = MockStringProvider()
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
    override val feature = MockFeature()
}

class TestHotelDetailsScopeHappy(bucketed: Boolean) : HasItin, HasHotel, HasStringProvider, HasActivityLauncher, HasWebViewLauncher, HasTripsTracking, HasAbacusProvider, HasFeature {
    val activityMockLauncher = MockActivityLauncher()
    override val activityLauncher: IActivityLauncher = activityMockLauncher
    override val abacus: AbacusSource = MockAbacusSource(bucketed)
    override val itin: Itin = ItinMocker.hotelDetailsHappy
    override val hotel: ItinHotel = ItinMocker.hotelDetailsHappy.firstHotel()!!
    override val strings: StringSource = MockStringProvider()
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
    override val feature = MockFeature()
}

class TestPackageHotelDetailsScopeHappy(bucketed: Boolean) : HasItin, HasHotel, HasStringProvider, HasActivityLauncher, HasWebViewLauncher, HasTripsTracking, HasAbacusProvider, HasFeature {
    val activityMockLauncher = MockActivityLauncher()
    override val activityLauncher: IActivityLauncher = activityMockLauncher
    override val abacus: AbacusSource = MockAbacusSource(bucketed)
    override val itin: Itin = ItinMocker.hotelPackageHappy
    override val hotel: ItinHotel = ItinMocker.hotelPackageHappy.firstHotel()!!
    override val strings: StringSource = MockStringProvider()
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
    val mockTripsTracking = MockTripsTracking()
    override val tripsTracking: ITripsTracking = mockTripsTracking
    override val feature = MockFeature()
}
