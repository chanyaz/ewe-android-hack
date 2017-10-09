package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.ShopWithPointsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
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
    lateinit private var userLoginStateChangedModel: UserLoginStateChangedModel

    private val context = RuntimeEnvironment.application

    @Before
    fun setUp() {
        userLoginStateChangedModel = UserLoginStateChangedModel()
    }

    @Test
    fun loggedOutUser() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel, userLoginStateChangedModel)
        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        testObserver.assertValueCount(1)
        assertFalse(testObserver.onNextEvents[0])
    }

    @Test
    fun loggedInUserWithoutLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(getUserEnrolledInRewardsWithNoPoints())
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel, userLoginStateChangedModel)
        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertFalse(testObserver.onNextEvents[0])
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun loggedInUserWithLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(getUserWithPointsToSpend())
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel, userLoginStateChangedModel)

        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        val testPointsDetailStringObserver: TestSubscriber<String> = TestSubscriber.create()
        shopWithPointsViewModel.pointsDetailStringObservable.subscribe(testPointsDetailStringObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertTrue(testObserver.onNextEvents[0])
        assertEquals("You have 4,444 points", testPointsDetailStringObserver.onNextEvents[0])
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun loggedInUserWithChangingLoyaltyPoints() {
        val user = getUserWithPointsToSpend()
        UserLoginTestUtil.setupUserAndMockLogin(user)

        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel, userLoginStateChangedModel)

        val testObserver: TestSubscriber<Boolean> = TestSubscriber.create()
        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribe(testObserver)

        val testPointsDetailStringObserver: TestSubscriber<String> = TestSubscriber.create()
        shopWithPointsViewModel.pointsDetailStringObservable.subscribe(testPointsDetailStringObserver)

        assertTrue(shopWithPointsViewModel.shopWithPointsToggleObservable.value)
        assertTrue(testObserver.onNextEvents[0])
        assertEquals("You have 4,444 points", testPointsDetailStringObserver.onNextEvents[0])

        val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

        user.loyaltyMembershipInformation?.loyaltyPointsAvailable = 3600.0
        userStateManager.userSource.user = user
        userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        assertTrue(testObserver.onNextEvents[1])
        assertEquals("You have 3,600 points", testPointsDetailStringObserver.onNextEvents[1])

        user.loyaltyMembershipInformation?.isAllowedToShopWithPoints = false
        userStateManager.userSource.user = user
        userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        assertFalse(testObserver.onNextEvents[2])
    }

    @Test
    fun loyaltyHeaderChangeTest() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(context, paymentModel, userLoginStateChangedModel)
        val headerTestObservable = TestSubscriber.create<String>()
        shopWithPointsViewModel.swpHeaderStringObservable.subscribe(headerTestObservable)

        assertEquals(context.getString(R.string.swp_on_widget_header), headerTestObservable.onNextEvents[0])

        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        assertEquals(context.getString(R.string.swp_off_widget_header), headerTestObservable.onNextEvents[1])

        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        assertEquals(context.getString(R.string.swp_on_widget_header), headerTestObservable.onNextEvents[2])
    }

    private fun getUserEnrolledInRewardsWithNoPoints(): User {
        val user = User()
        val traveler = Traveler()
        user.primaryTraveler = traveler
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 0.0
        loyaltyInfo.loyaltyPointsPending = 0.0
        loyaltyInfo.isLoyaltyMembershipActive = true
        user.loyaltyMembershipInformation = loyaltyInfo
        return user
    }

    private fun getUserWithPointsToSpend(): User {
        val user = getUserEnrolledInRewardsWithNoPoints()
        user.loyaltyMembershipInformation?.loyaltyPointsAvailable = 4444.0
        user.loyaltyMembershipInformation?.isAllowedToShopWithPoints = true
        return user
    }
}