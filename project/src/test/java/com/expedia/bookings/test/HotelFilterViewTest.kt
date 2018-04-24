package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelServerFilterView
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.HotelFilterViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFilterViewTest {
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    var hotelFilterView: HotelServerFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var shopWithPointsViewModel: ShopWithPointsViewModel by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    private lateinit var viewModel: HotelFilterViewModel

    @Before
    fun before() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(RuntimeEnvironment.application, paymentModel, UserLoginStateChangedModel())
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testVipAccessVisibilityGermanPosDisabled() {
        setPOS(PointOfSaleId.GERMANY)
        initViewModel()
        assertEquals(View.GONE, hotelFilterView.filterVipView.visibility)
        assertEquals(View.GONE, hotelFilterView.optionLabel.visibility)
        setPOS(PointOfSaleId.UNITED_STATES)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testVipAccessVisibilityUsPos() {
        initViewModel()
        assertEquals(View.VISIBLE, hotelFilterView.filterVipView.visibility)
        assertEquals(View.VISIBLE, hotelFilterView.optionLabel.visibility)
    }

    @Test
    fun testSortByDistanceIsRemovedForNonCurrentLocationSearch() {
        initViewModel()
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList = listOf(DisplaySort.RECOMMENDED, DisplaySort.PRICE, DisplaySort.DEALS, DisplaySort.RATING).toCollection(ArrayList<DisplaySort>())
        assertEquals(hotelFilterView.hotelSortOptionsView.getSortItems(), enumOfSortingList)
    }

    @Test
    fun testSortByDealsIsRemovedForSwP() {
        initViewModel()
        hotelFilterView.shopWithPointsViewModel?.swpEffectiveAvailability?.onNext(true)
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList = listOf(DisplaySort.RECOMMENDED, DisplaySort.PRICE, DisplaySort.RATING).toCollection(ArrayList<DisplaySort>())
        assertEquals(hotelFilterView.hotelSortOptionsView.getSortItems(), enumOfSortingList)
    }

    @Test
    fun testNeighborhoodManyToNone() {
        // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/1164
        initViewModel()
        viewModel.neighborhoodListObservable.onNext(getNeighborhoodList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.VISIBLE)

        viewModel.neighborhoodListObservable.onNext(emptyList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.GONE)
    }

    @Test
    fun testOneNeighborhoodIsValid() {
        // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/1682
        initViewModel()
        val list = ArrayList<Neighborhood>()
        list.add(Neighborhood())
        viewModel.neighborhoodListObservable.onNext(list)

        assertTrue(hotelFilterView.neighborhoodView.visibility == View.VISIBLE)
        assertTrue(hotelFilterView.neighborhoodLabel.visibility == View.VISIBLE)
    }

    @Test
    fun testNeighborhoodNoneToMany() {
        initViewModel()
        viewModel.neighborhoodListObservable.onNext(emptyList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.GONE)

        viewModel.neighborhoodListObservable.onNext(getNeighborhoodList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.VISIBLE)
    }

    @Test
    fun testUpdateNameWithSearchOptions() {
        initViewModel()

        val testSubscriber = TestObserver<CharSequence>()
        hotelFilterView.hotelNameFilterView.filterNameChangedSubject.subscribe(testSubscriber)

        val userFilters = UserFilterChoices()
        val name = "Hyatt"
        userFilters.name = name
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertEquals(name, testSubscriber.values()[0].toString())
    }

    @Test
    fun testUpdateStarsWithSearchOptions() {
        initViewModel()

        val testSubscriber1 = TestObserver<Unit>()
        val testSubscriber2 = TestObserver<Unit>()
        val testSubscriber3 = TestObserver<Unit>()
        hotelFilterView.starRatingView.oneStarSubject.subscribe(testSubscriber1)
        hotelFilterView.starRatingView.twoStarSubject.subscribe(testSubscriber2)
        hotelFilterView.starRatingView.threeStarSubject.subscribe(testSubscriber3)

        val userFilters = UserFilterChoices()
        val stars = UserFilterChoices.StarRatings(true, true, false, false, false)
        userFilters.hotelStarRating = stars
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        testSubscriber1.assertValueCount(1)
        testSubscriber2.assertValueCount(1)
        testSubscriber3.assertValueCount(0)
    }

    @Test
    fun testUpdateVipWithSearchOptions() {
        initViewModel()

        val testSubscriber = TestObserver<Boolean>()
        hotelFilterView.filterVipView.vipCheckedSubject.subscribe(testSubscriber)

        val userFilters = UserFilterChoices()
        userFilters.isVipOnlyAccess = true
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertTrue(testSubscriber.values()[0])
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun initViewModel() {
        hotelFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_filter_view_test, null) as HotelServerFilterView
        viewModel = HotelFilterViewModel(activity)
        hotelFilterView.initViewModel(viewModel)
        hotelFilterView.shopWithPointsViewModel = shopWithPointsViewModel
    }

    private fun getNeighborhoodList(): List<Neighborhood> {
        val list = ArrayList<Neighborhood>()
        list.add(Neighborhood())
        list.add(Neighborhood())
        list.add(Neighborhood())
        return list
    }
}
