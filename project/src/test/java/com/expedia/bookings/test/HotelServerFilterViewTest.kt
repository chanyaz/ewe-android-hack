package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.widget.OnHotelAmenityFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelNameFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelNeighborhoodFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelPriceFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelSortChangedListener
import com.expedia.bookings.hotel.widget.OnHotelVipFilterChangedListener
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.GuestRatingValue
import com.expedia.bookings.widget.HotelServerFilterView
import com.expedia.bookings.widget.OnHotelGuestRatingFilterChangedListener
import com.expedia.bookings.widget.OnHotelStarRatingFilterChangedListener
import com.expedia.bookings.widget.StarRatingValue
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelServerFilterViewTest {
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
        AbacusTestUtils.bucketTests(AbacusUtils.HotelAmenityFilter, AbacusUtils.HotelGuestRatingFilter)
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

        val listener = object : OnHotelNameFilterChangedListener {
            var hotelName = ""
            override
            fun onHotelNameFilterChanged(hotelName: CharSequence, doTracking: Boolean) {
                this.hotelName = hotelName.toString()
            }
        }
        hotelFilterView.hotelNameFilterView.setOnHotelNameChangedListener(listener)

        val userFilters = UserFilterChoices()
        val name = "Hyatt"
        userFilters.name = name
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertEquals(name, listener.hotelName)
    }

    @Test
    fun testUpdateSortWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelSortChangedListener {
            var displaySort = DisplaySort.getDefaultSort()

            override
            fun onHotelSortChanged(displaySort: DisplaySort, doTracking: Boolean) {
                this.displaySort = displaySort
            }
        }

        hotelFilterView.hotelSortOptionsView.setOnHotelSortChangedListener(listener)

        val userFilters = UserFilterChoices()
        userFilters.userSort = DisplaySort.RATING
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertEquals(DisplaySort.RATING, listener.displaySort)
    }

    @Test
    fun testUpdateStarsWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelStarRatingFilterChangedListener {
            val starRatings = arrayOf(false, false, false, false, false)

            override
            fun onHotelStarRatingFilterChanged(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean) {
                when (starRatingValue) {
                    StarRatingValue.One -> starRatings[0] = selected
                    StarRatingValue.Two -> starRatings[1] = selected
                    StarRatingValue.Three -> starRatings[2] = selected
                    StarRatingValue.Four -> starRatings[3] = selected
                    StarRatingValue.Five -> starRatings[4] = selected
                }
            }
        }
        hotelFilterView.starRatingView.setOnHotelStarRatingFilterChangedListener(listener)

        val userFilters = UserFilterChoices()
        val stars = UserFilterChoices.StarRatings(true, true, false, false, false)
        userFilters.hotelStarRating = stars
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertTrue(listener.starRatings[0])
        assertTrue(listener.starRatings[1])
        assertFalse(listener.starRatings[2])
        assertFalse(listener.starRatings[3])
        assertFalse(listener.starRatings[4])
    }

    @Test
    fun testUpdateGuestRatingWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelGuestRatingFilterChangedListener {
            val guestRatings = arrayOf(false, false, false)

            override
            fun onHotelGuestRatingFilterChanged(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean) {
                when (guestRatingValue) {
                    GuestRatingValue.Three -> guestRatings[0] = selected
                    GuestRatingValue.Four -> guestRatings[1] = selected
                    GuestRatingValue.Five -> guestRatings[2] = selected
                }
            }
        }
        hotelFilterView.guestRatingView.setOnHotelGuestRatingFilterChangedListener(listener)

        val userFilters = UserFilterChoices()
        val guestRating = UserFilterChoices.GuestRatings(true, false, true)
        userFilters.hotelGuestRating = guestRating
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertFalse(listener.guestRatings[0])
        assertFalse(listener.guestRatings[1])
        assertTrue(listener.guestRatings[2])
    }

    @Test
    fun testUpdateVipWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelVipFilterChangedListener {
            var vipChecked = false

            override
            fun onHotelVipFilterChanged(vipChecked: Boolean, doTracking: Boolean) {
                this.vipChecked = vipChecked
            }
        }
        hotelFilterView.filterVipView.setOnHotelVipFilterChangedListener(listener)

        val userFilters = UserFilterChoices()
        userFilters.isVipOnlyAccess = true
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertTrue(listener.vipChecked)
    }

    @Test
    fun testUpdatePriceRangeWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelPriceFilterChangedListener {
            var minPrice = 0
            var maxPrice = 0

            override
            fun onHotelPriceFilterChanged(minPrice: Int, maxPrice: Int, doTracking: Boolean) {
                this.minPrice = minPrice
                this.maxPrice = maxPrice
            }
        }
        hotelFilterView.priceRangeView.setOnHotelPriceFilterChanged(listener)

        val userFilters = UserFilterChoices()
        userFilters.minPrice = 100
        userFilters.maxPrice = 10

        val priceRange = PriceRange("USD", 0, 100)
        hotelFilterView.priceRangeView.newPriceRangeObservable.onNext(priceRange)
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertEquals(100, listener.minPrice)
        assertEquals(10, listener.maxPrice)
    }

    @Test
    fun testUpdateAmenityWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelAmenityFilterChangedListener {
            val amenityMap = HashMap<Amenity, Boolean>()

            override
            fun onHotelAmenityFilterChanged(amenity: Amenity, selected: Boolean, doTracking: Boolean) {
                amenityMap[amenity] = selected
            }
        }
        hotelFilterView.amenityViews.forEach { amenityGridItem ->
            amenityGridItem.setOnHotelAmenityFilterChangedListener(listener)
        }

        val userFilters = UserFilterChoices()
        userFilters.amenities = hashSetOf(Amenity.getSearchKey(Amenity.ACCESSIBLE_PATHS),
                Amenity.getSearchKey(Amenity.PETS),
                Amenity.getSearchKey(Amenity.AIRPORT_SHUTTLE),
                Amenity.getSearchKey(Amenity.AC_UNIT),
                Amenity.getSearchKey(Amenity.ALL_INCLUSIVE))
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertFalse(listener.amenityMap.containsKey(Amenity.ACCESSIBLE_PATHS))
        assertFalse(listener.amenityMap.containsKey(Amenity.BREAKFAST))
        assertFalse(listener.amenityMap.containsKey(Amenity.POOL))
        assertFalse(listener.amenityMap.containsKey(Amenity.PARKING))
        assertFalse(listener.amenityMap.containsKey(Amenity.INTERNET))
        assertTrue(listener.amenityMap.containsKey(Amenity.PETS))
        assertTrue(listener.amenityMap.containsKey(Amenity.AIRPORT_SHUTTLE))
        assertTrue(listener.amenityMap.containsKey(Amenity.AC_UNIT))
        assertTrue(listener.amenityMap.containsKey(Amenity.ALL_INCLUSIVE))
    }

    @Test
    fun testUpdateNeighborhoodWithSearchOptions() {
        initViewModel()

        val listener = object : OnHotelNeighborhoodFilterChangedListener {
            var neighborhood = Neighborhood()

            override
            fun onHotelNeighborhoodFilterChanged(neighborhood: Neighborhood, selected: Boolean, doTracking: Boolean) {
                this.neighborhood = neighborhood
            }
        }
        hotelFilterView.neighborhoodView.setOnHotelNeighborhoodFilterChangedListener(listener)

        val userFilters = UserFilterChoices()
        val neighborhood = Neighborhood().apply {
            this.name = "1"
            this.id = "1"
        }
        userFilters.neighborhoods = hashSetOf(neighborhood)
        viewModel.presetFilterOptionsUpdatedSubject.onNext(userFilters)

        assertEquals(neighborhood, listener.neighborhood)
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
