package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.vm.ShopWithPointsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class ShopWithPointsViewModelTest {

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    lateinit private var shopWithPointsViewModel: ShopWithPointsViewModel
    lateinit private var paymentModel: PaymentModel<HotelCreateTripResponse>

    private val context = RuntimeEnvironment.application

    @Test
    fun loggedOutUser() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel)
        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        testObserver.assertValueCount(1)
        assertFalse(testObserver.onNextEvents[0])
    }

    @Test
    fun loggedInUserWithoutLoyaltyPoints() {
        UserLoginTestUtil.Companion.setupUserAndMockLogin(mockUser())

        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel)
        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertFalse(testObserver.onNextEvents[0])
    }

    @Test
    fun loggedInUserWithLoyaltyPoints() {
        val user = mockUser()
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        val pointsAvailable = 4444.toDouble()
        loyaltyInfo.loyaltyPointsAvailable = pointsAvailable
        loyaltyInfo.isAllowedToShopWithPoints = true
        user.loyaltyMembershipInformation = loyaltyInfo
        UserLoginTestUtil.Companion.setupUserAndMockLogin(user)
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel)

        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        val testPointsAvailableObserver: TestSubscriber<Double> = TestSubscriber.create()
        shopWithPointsViewModel.numberOfPointsObservable.subscribe(testPointsAvailableObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertTrue(testObserver.onNextEvents[0])
        assertEquals(pointsAvailable, testPointsAvailableObserver.onNextEvents[0])
    }

    @Test
    fun loggedInUserWithChangingLoyaltyPoints() {
        val user = mockUser()
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        var pointsAvailable = 4444.0
        loyaltyInfo.loyaltyPointsAvailable = pointsAvailable
        loyaltyInfo.isAllowedToShopWithPoints = true
        user.loyaltyMembershipInformation = loyaltyInfo
        UserLoginTestUtil.Companion.setupUserAndMockLogin(user)

        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel)

        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        val testPointsAvailableObserver: TestSubscriber<Double> = TestSubscriber.create()
        shopWithPointsViewModel.numberOfPointsObservable.subscribe(testPointsAvailableObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertTrue(testObserver.onNextEvents[0])
        assertEquals(pointsAvailable, testPointsAvailableObserver.onNextEvents[0])

        pointsAvailable = 3600.0
        loyaltyInfo.loyaltyPointsAvailable = pointsAvailable
        Db.setUser(user)
        val userAccountRefresher = UserAccountRefresher(context, LineOfBusiness.HOTELS, UserAccountRefresher.IUserAccountRefreshListener {
            assertEquals(loyaltyInfo.isAllowedToShopWithPoints, testObserver.onNextEvents[testObserver.onNextEvents.size - 1])
            assertEquals(pointsAvailable, testPointsAvailableObserver.onNextEvents[testPointsAvailableObserver.onNextEvents.size - 1])
        })
        userAccountRefresher.ensureAccountIsRefreshed()

        loyaltyInfo.isAllowedToShopWithPoints = false
        Db.setUser(user)
        userAccountRefresher.ensureAccountIsRefreshed()
    }

    @Test
    fun loyaltyHeaderChangeTest() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel)
        val headerTestObservable = TestSubscriber.create<String>()
        shopWithPointsViewModel.swpHeaderStringObservable.subscribe(headerTestObservable)

        assertEquals(context.getString(R.string.swp_on_widget_header), headerTestObservable.onNextEvents[0])

        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        assertEquals(context.getString(R.string.swp_off_widget_header), headerTestObservable.onNextEvents[1])

        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        assertEquals(context.getString(R.string.swp_on_widget_header), headerTestObservable.onNextEvents[2])
    }

    private fun mockUser(): User {
        val user = User()
        val traveler = Traveler()
        traveler.loyaltyMembershipTier = Traveler.LoyaltyMembershipTier.GOLD
        user.primaryTraveler = traveler
        return user
    }
}