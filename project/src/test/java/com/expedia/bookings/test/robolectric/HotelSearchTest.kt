package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.ShopWithPointsViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelSearchTest {
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    var vm: HotelSearchViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100
    var activity : Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultHotelComponents()
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)

    }

    @Test
    fun selectDatesAndSearch() {
        val testSubscriber = TestSubscriber<HotelSearchParams>()
        val expected = arrayListOf<HotelSearchParams>()
        val suggestion = getDummySuggestion()

        vm = HotelSearchViewModel(activity)
        vm.searchParamsObservable.subscribe(testSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.destinationLocationObserver.onNext(suggestion)

        // Selecting only start date should search with end date as the next day
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        expected.add(HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams)

        // Select both start date and end date and search
        vm.datesObserver.onNext(Pair(LocalDate.now(), LocalDate.now().plusDays(3)))
        vm.searchObserver.onNext(Unit)
        expected.add(HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)).build() as HotelSearchParams)

        // When neither start date nor end date are selected, search should not fire anything
        vm.datesObserver.onNext(Pair(null, null))
        vm.searchObserver.onNext(Unit)
        vm.searchObserver.onNext(Unit)
        vm.searchObserver.onNext(Unit)

        testSubscriber.requestMore(LOTS_MORE)
        assertEquals(testSubscriber.onNextEvents[0].checkOut, expected[0].checkOut)
        assertEquals(testSubscriber.onNextEvents[1].checkOut, expected[1].checkOut)
    }

    @Test
    fun shopWithPointsSelection() {
        val testSubscriber = TestSubscriber<HotelSearchParams>()
        val expected = arrayListOf<HotelSearchParams>()
        val suggestion = getDummySuggestion()

        UserLoginTestUtil.setupUserAndMockLogin(getUserWithSWPEnabled())
        vm = HotelSearchViewModel(activity)
        vm.searchParamsObservable.subscribe(testSubscriber)

        vm.shopWithPointsViewModel = ShopWithPointsViewModel(activity, paymentModel, UserLoginStateChangedModel())
        vm.destinationLocationObserver.onNext(suggestion)
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)

        val builder = HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                .destination(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)) as HotelSearchParams.Builder
        expected.add(builder.shopWithPoints(true).build())

        // Turn SWP Off
        vm.shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        vm.searchObserver.onNext(Unit)
        expected.add(builder.shopWithPoints(false).build())

        // Turn SWP ON
        vm.shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        vm.searchObserver.onNext(Unit)
        expected.add(builder.shopWithPoints(true).build())

        assertEquals(testSubscriber.onNextEvents[0].shopWithPoints, expected[0].shopWithPoints)
        assertEquals(testSubscriber.onNextEvents[1].shopWithPoints, expected[1].shopWithPoints)
        assertEquals(testSubscriber.onNextEvents[2].shopWithPoints, expected[2].shopWithPoints)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }

    private fun getUserWithSWPEnabled(): User {
        val user = User()
        val traveler = Traveler()
        traveler.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        user.primaryTraveler = traveler
        var pointsAvailable = 4444.0
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.loyaltyPointsAvailable = pointsAvailable
        loyaltyInfo.isAllowedToShopWithPoints = true
        user.loyaltyMembershipInformation = loyaltyInfo
        return user
    }
}
