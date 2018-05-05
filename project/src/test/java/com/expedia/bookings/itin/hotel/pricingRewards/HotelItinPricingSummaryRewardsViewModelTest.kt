package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasE3Endpoint
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.tracking.ITripsTracking
import org.junit.Test
import kotlin.test.assertTrue

class HotelItinPricingSummaryRewardsViewModelTest {
    private val mockWithRewardList = ItinMocker.hotelDetailsHappy
    private val mockWithoutRewardList = ItinMocker.hotelPackageHappy

    @Test
    fun testHideWidgetSubject() {
        val testObserver = TestObserver<Unit>()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(MockItinRewardsScope())
        viewModel.hideWidgetSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithoutRewardList)
        testObserver.assertValueCount(1)

        testObserver.dispose()
    }

    @Test
    fun testRewardButtonClickSubject() {
        val testObserver = TestObserver<Unit>()
        val scope = MockItinRewardsScope()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(scope)
        viewModel.rewardsButtonClickSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        viewModel.rewardsButtonClickSubject.onNext(Unit)
        testObserver.assertValueCount(1)
        assertTrue(scope.mockWebViewLauncher.shouldScrapTitle)
        assertTrue(scope.mockWebViewLauncher.lastSeenTripId == "58fc868b-63e9-42cc-a0c3-6ac4dd78beaa")
        assertTrue(scope.mockWebViewLauncher.lastSeenTitle == R.string.itin_hotel_details_price_summary_rewards_title)
        assertTrue(scope.mockWebViewLauncher.lastSeenURL == "https://expedia.com/user/rewards")
        assertTrue(scope.mockTripsTracking.trackHotelItinViewRewardsCalled)

        testObserver.dispose()
    }

    @Test
    fun testLogoSubject() {
        val testObserver = TestObserver<String>()
        val scope = MockItinRewardsScope()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(scope)
        viewModel.logoSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        testObserver.assertValueCount(1)
        assertTrue(testObserver.values().first() == "https://expedia.com/static/default/default/images/myrewards/RewardsLogo_193x76.png")

        testObserver.dispose()
    }

    @Test
    fun testEarnedPointsSubject() {
        val testObserver = TestObserver<String>()
        val scope = MockItinRewardsScope()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(scope)
        viewModel.earnedPointsSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        testObserver.assertValueCount(1)
        val expected = (R.string.itin_hotel_details_price_summary_rewards_earned_points_TEMPLATE).toString().plus(
                mapOf("points" to "217.00", "program" to "ExpediaRewards")
        )
        assertTrue(testObserver.values().first() == expected)

        testObserver.dispose()
    }

    @Test
    fun testBasePointsSubject() {
        val testObserver = TestObserver<String>()
        val scope = MockItinRewardsScope()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(scope)
        viewModel.basePointsSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        testObserver.assertValueCount(1)
        val expected = (R.string.itin_hotel_details_price_summary_rewards_base_points_TEMPLATE).toString().plus(
                mapOf("points" to "109.00")
        )
        assertTrue(testObserver.values().first() == expected)

        testObserver.dispose()
    }

    @Test
    fun testBonusPointsSubject() {
        val testObserver = TestObserver<List<String>>()
        val scope = MockItinRewardsScope()
        val viewModel = HotelItinPricingSummaryRewardsViewModel(scope)
        viewModel.bonusPointsSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.itinObserver.onChanged(mockWithRewardList)
        testObserver.assertValueCount(1)
        val expected = (R.string.itin_hotel_details_price_summary_rewards_bonus_points_TEMPLATE).toString().plus(
                mapOf("points" to "108.00", "program" to "App Booking Double Points")
        )
        assertTrue(testObserver.values().first() == listOf(expected))

        testObserver.dispose()
    }

    class MockItinRewardsScope : HasStringProvider, HasHotelRepo, HasWebViewLauncher, HasTripsTracking, HasE3Endpoint, HasLifecycleOwner {
        val mockWebViewLauncher = MockWebViewLauncher()
        val mockTripsTracking = MockTripsTracking()
        override val strings: StringSource = MockStringProvider()
        override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
        override val tripsTracking: ITripsTracking = mockTripsTracking
        override val e3Endpoint: String = "https://expedia.com/"
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}