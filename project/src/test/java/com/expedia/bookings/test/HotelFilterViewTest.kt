package com.expedia.bookings.test

import android.app.Activity
import android.widget.ArrayAdapter
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelFilterView
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelFilterViewModel.Sort
import com.expedia.vm.ShopWithPointsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelFilterViewTest {
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    var hotelFilterView: HotelFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var shopWithPointsViewModel: ShopWithPointsViewModel by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    @Before fun before() {
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(RuntimeEnvironment.application, paymentModel, UserLoginStateChangedModel())
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels);
        Ui.getApplication(activity).defaultHotelComponents()
        hotelFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_filter_view_test, null) as HotelFilterView
        hotelFilterView.viewmodel = HotelFilterViewModel(activity, LineOfBusiness.HOTELS)
        hotelFilterView.sortByButtonGroup.onItemSelectedListener = null
        hotelFilterView.sortByButtonGroup.setOnTouchListener { view, motionEvent -> false }
        hotelFilterView.shopWithPointsViewModel = shopWithPointsViewModel
    }

    @Test
    fun testSortByDistanceIsRemovedForNonCurrentLocationSearch(){
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList= listOf(Sort.POPULAR, Sort.PRICE, Sort.DEALS, Sort.RATING).toCollection(ArrayList<Sort>())
        val expectedEnumOfSortingLists = getItems(hotelFilterView.sortByAdapter)
        assertEquals(expectedEnumOfSortingLists,enumOfSortingList)
    }

    @Test
    fun testSortByDealsIsRemovedForSwP(){
        hotelFilterView.shopWithPointsViewModel?.swpEffectiveAvailability?.onNext(true)
        hotelFilterView.sortByObserver.onNext(false)
        val enumOfSortingList= listOf(Sort.POPULAR, Sort.PRICE, Sort.RATING).toCollection(ArrayList<Sort>())
        val expectedEnumOfSortingLists = getItems(hotelFilterView.sortByAdapter)

        assertEquals(expectedEnumOfSortingLists,enumOfSortingList)
    }

    fun getItems(adapter: ArrayAdapter<Sort>): ArrayList<Sort> {
        val array: ArrayList<Sort> = arrayListOf()
        for(i in 1 .. adapter.count )
            array.add(adapter.getItem(i-1))
        return array
    }
}