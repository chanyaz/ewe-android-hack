package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import android.widget.ArrayAdapter
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.HotelClientFilterViewModel
import com.expedia.vm.ShopWithPointsViewModel
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

    var hotelFilterView: HotelClientFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var shopWithPointsViewModel: ShopWithPointsViewModel by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    @Before fun before() {
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
        assertEquals(View.GONE, hotelFilterView.filterVipContainer.visibility)
        assertEquals(View.GONE, hotelFilterView.optionLabel.visibility)
        setPOS(PointOfSaleId.UNITED_STATES)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testVipAccessVisibilityUsPos() {
        initViewModel()
        assertEquals(View.VISIBLE, hotelFilterView.filterVipContainer.visibility)
        assertEquals(View.VISIBLE, hotelFilterView.optionLabel.visibility)
    }

    @Test
    fun testSortByDistanceIsRemovedForNonCurrentLocationSearch() {
        initViewModel()
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList = listOf(Sort.RECOMMENDED, Sort.PRICE, Sort.DEALS, Sort.RATING).toCollection(ArrayList<Sort>())
        val expectedEnumOfSortingLists = getItems(hotelFilterView.sortByAdapter)
        assertEquals(expectedEnumOfSortingLists, enumOfSortingList)
    }

    @Test
    fun testClearFilterContentDesc() {
        initViewModel()
        val clearFilterCD = hotelFilterView.clearNameButton.contentDescription.toString()
        assertEquals("Clear Filters", clearFilterCD)
    }

    @Test
    fun testSortByDealsIsRemovedForSwP() {
        initViewModel()
        hotelFilterView.shopWithPointsViewModel?.swpEffectiveAvailability?.onNext(true)
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList = listOf(Sort.RECOMMENDED, Sort.PRICE, Sort.RATING).toCollection(ArrayList<Sort>())
        val expectedEnumOfSortingLists = getItems(hotelFilterView.sortByAdapter)

        assertEquals(expectedEnumOfSortingLists, enumOfSortingList)
    }

    @Test
    fun testNeighborhoodManyToNone() {
        // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/1164
        initViewModel()
        hotelFilterView.viewModel.neighborhoodListObservable.onNext(getNeighborhoodList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.VISIBLE)

        hotelFilterView.viewModel.neighborhoodListObservable.onNext(emptyList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.GONE)
    }

    @Test
    fun testNeighborhoodNoneToMany() {
        initViewModel()
        hotelFilterView.viewModel.neighborhoodListObservable.onNext(emptyList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.GONE)

        hotelFilterView.viewModel.neighborhoodListObservable.onNext(getNeighborhoodList())
        assertTrue(hotelFilterView.neighborhoodView.visibility == View.VISIBLE)
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun initViewModel() {
        hotelFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_filter_view_test, null) as HotelClientFilterView
        hotelFilterView.viewModel = HotelClientFilterViewModel(activity)
        hotelFilterView.sortByButtonGroup.onItemSelectedListener = null
        hotelFilterView.sortByButtonGroup.setOnTouchListener { view, motionEvent -> false }
        hotelFilterView.shopWithPointsViewModel = shopWithPointsViewModel
    }

    fun getItems(adapter: ArrayAdapter<Sort>): ArrayList<Sort> {
        val array: ArrayList<Sort> = arrayListOf()
        for (i in 1..adapter.count)
            array.add(adapter.getItem(i - 1))
        return array
    }

    private fun getNeighborhoodList() : List<HotelSearchResponse.Neighborhood> {
        val list = ArrayList<HotelSearchResponse.Neighborhood>()
        list.add(HotelSearchResponse.Neighborhood())
        list.add(HotelSearchResponse.Neighborhood())
        list.add(HotelSearchResponse.Neighborhood())
        return list
    }
}