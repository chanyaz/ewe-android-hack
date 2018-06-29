package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.PriceOption
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.StarRatingValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageFilterViewModelTest {
    var vm: PackageFilterViewModel by Delegates.notNull()
    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get
    lateinit var context: Context
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        vm = PackageFilterViewModel(context)
    }

    @Test
    fun filterStars() {
        vm.userFilterChoices.hotelStarRating.one = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.One, true, false)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.one)

        vm.userFilterChoices.hotelStarRating.two = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Two, true, false)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.two)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.One, false, false)
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
    }

    @Test
    fun testStarFilterSelections() {
        Db.setPackageResponse(mockPackageServiceRule.getMIDHotelResponse())
        vm.originalResponse = fakeFilteredResponse()

        applyFiltersAndAssertCount(StarRatingValue.One, expectedCount = 0)
        applyFiltersAndAssertCount(StarRatingValue.Two, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.One, StarRatingValue.Two, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Three, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Three, StarRatingValue.One, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Three, StarRatingValue.Two, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Three, StarRatingValue.Two, StarRatingValue.One, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Four, expectedCount = 0)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.One, expectedCount = 0)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Two, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Two, StarRatingValue.One, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Three, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Three, StarRatingValue.One, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Three, StarRatingValue.Two, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Four, StarRatingValue.Three, StarRatingValue.Two, StarRatingValue.One, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.One, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Two, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Two, StarRatingValue.One, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Three, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Three, StarRatingValue.One, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Three, StarRatingValue.Two, expectedCount = 3)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Three, StarRatingValue.Two, StarRatingValue.One, expectedCount = 3)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.One, expectedCount = 1)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.Two, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.Two, StarRatingValue.One, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.Three, expectedCount = 2)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.Three, StarRatingValue.Two, expectedCount = 3)
        applyFiltersAndAssertCount(StarRatingValue.Five, StarRatingValue.Four, StarRatingValue.Three, StarRatingValue.Two, StarRatingValue.One, expectedCount = 3)
    }

    private fun applyFiltersAndAssertCount(vararg ratings: StarRatingValue, expectedCount: Int) {
        for (starRating in ratings) {
            when (starRating) {
                StarRatingValue.One -> vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.One, true, false)
                StarRatingValue.Two -> vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Two, true, false)
                StarRatingValue.Three -> vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Three, true, false)
                StarRatingValue.Four -> vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Four, true, false)
                StarRatingValue.Five -> vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Five, true, false)
            }
        }
        assertEquals(expectedCount, vm.filteredResponse.hotelList.size)
        vm.clearObservable.onNext(Unit)
    }

    @Test
    fun clearFilters() {
        val ogResponse = fakeFilteredResponse()
        vm.originalResponse = ogResponse

        val str = "Hilton"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(str, false)

        vm.userFilterChoices.hotelStarRating.one = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.One, true, false)

        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)

        vm.userFilterChoices.minPrice = 20
        vm.userFilterChoices.maxPrice = 50

        vm.clearObservable.onNext(Unit)

        assertTrue(vm.userFilterChoices.name.isBlank())
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(0, vm.userFilterChoices.minPrice)
        assertEquals(0, vm.userFilterChoices.maxPrice)
        assertEquals(ogResponse.hotelList.size, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun filterResultsCount() {
        vm.originalResponse = fakeFilteredResponse()
        var str = "Hil"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(str, false)
        assertEquals(1, vm.filteredResponse.hotelList.size)

        str = "junk"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(str, false)
        assertEquals(0, vm.filteredResponse.hotelList.size)

        str = ""
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(str, false)
        assertEquals(3, vm.filteredResponse.hotelList.size)

        vm.userFilterChoices.hotelStarRating.three = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Three, true, false)
        assertEquals(1, vm.filteredResponse.hotelList.size)

        vm.clearObservable.onNext(Unit)
        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)
        assertEquals(1, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun emptyFilters() {
        vm.doneObservable.onNext(Unit)
        assertEquals(0, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun sortByPopular() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.RECOMMENDED)
        assertResultsSortedByRecommended()
    }

    @Test
    fun sortByRecommendedMID() {
        vm.filteredResponse = HotelSearchResponse.convertPackageToSearchResponse(mockPackageServiceRule.getMIDHotelResponse(), false)
        vm.sortByObservable.onNext(DisplaySort.DEALS)
        vm.sortByObservable.onNext(DisplaySort.RECOMMENDED)
        assertResultsSortedByRecommended()
    }

    @Test
    fun sortByPrice() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.PRICE)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.priceToShowUsers
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.priceToShowUsers
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByDeals() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.DEALS)
        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val currentDeals = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.discountPercent
            val previousDeals = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.discountPercent
            assertTrue(currentDeals >= previousDeals, "Expected $currentDeals >= $previousDeals")
        }
    }

    @Test
    fun sortByPackageDiscount() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.PACKAGE_DISCOUNT)
        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val currentDeals = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.discountPercent
            val previousDeals = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.discountPercent
            assertTrue(currentDeals >= previousDeals, "Expected $currentDeals >= $previousDeals")
        }
    }

    @Test
    fun sortByRating() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.RATING)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).hotelGuestRating
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).hotelGuestRating
            assertTrue(current <= previous, "Expected $current <= $previous")
        }
    }

    @Test
    fun sortByDistance() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(DisplaySort.DISTANCE)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).proximityDistanceInMiles
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).proximityDistanceInMiles
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun filterCount() {
        vm.originalResponse = fakeFilteredResponse()
        val str = "Hil"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(str, false)
        assertTrue(vm.filterCountObservable.value == 1)

        vm.userFilterChoices.hotelStarRating.three = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Three, true, false)
        assertTrue(vm.filterCountObservable.value == 2)

        vm.userFilterChoices.hotelStarRating.four = false
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Four, true, false)
        assertTrue(vm.filterCountObservable.value == 3)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Four, false, false)
        assertTrue(vm.filterCountObservable.value == 2)

        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)
        assertTrue(vm.filterCountObservable.value == 3)
    }

    private fun fakeFilteredResponse(): HotelSearchResponse {
        val response = HotelSearchResponse()
        response.priceOptions = listOf(PriceOption(), PriceOption())
        response.priceOptions[0].minPrice = 0
        response.priceOptions[1].minPrice = 300

        response.hotelList = ArrayList<Hotel>()

        val hotel1 = Hotel()
        hotel1.sortIndex = 1
        hotel1.localizedName = "Hilton"
        hotel1.lowRateInfo = HotelRate()
        hotel1.lowRateInfo.total = 100.0f
        hotel1.lowRateInfo.priceToShowUsers = 100.0f
        hotel1.lowRateInfo.currencyCode = "USD"
        hotel1.lowRateInfo.discountPercent = -10f
        hotel1.hotelGuestRating = 4.5f
        hotel1.proximityDistanceInMiles = 1.2
        hotel1.locationDescription = "Civic Center"
        hotel1.hotelStarRating = 5f
        hotel1.isVipAccess = true
        hotel1.rateCurrencyCode = "USD"
        hotel1.packageOfferModel = PackageOfferModel()
        hotel1.packageOfferModel.price = PackageOfferModel.PackagePrice()
        hotel1.packageOfferModel.price.tripSavings = Money()
        hotel1.packageOfferModel.price.tripSavings.amount = BigDecimal(36.5)
        val amenities1 = ArrayList<Hotel.HotelAmenity>()
        val amenity1 = Hotel.HotelAmenity()
        amenity1.id = "4"
        amenities1.add(amenity1)
        hotel1.amenities = amenities1

        val hotel2 = Hotel()
        hotel2.sortIndex = 2
        hotel2.localizedName = "Double Tree"
        hotel2.lowRateInfo = HotelRate()
        hotel2.lowRateInfo.total = 200.0f
        hotel2.lowRateInfo.priceToShowUsers = 200.0f
        hotel2.lowRateInfo.currencyCode = "USD"
        hotel2.lowRateInfo.discountPercent = -15f
        hotel2.hotelGuestRating = 5f
        hotel2.proximityDistanceInMiles = 2.0
        hotel2.locationDescription = "Fisherman's Wharf"
        hotel2.hotelStarRating = 3.5f
        hotel2.isVipAccess = false
        hotel2.rateCurrencyCode = "USD"
        hotel2.packageOfferModel = PackageOfferModel()
        hotel2.packageOfferModel.price = PackageOfferModel.PackagePrice()
        hotel2.packageOfferModel.price.tripSavings = Money()
        hotel2.packageOfferModel.price.tripSavings.amount = BigDecimal(46.5)
        val amenities2 = ArrayList<Hotel.HotelAmenity>()
        val amenity2 = Hotel.HotelAmenity()
        amenity2.id = "1"
        amenities2.add(amenity2)
        hotel2.amenities = amenities2

        val hotel3 = Hotel()
        hotel3.sortIndex = 3
        hotel3.localizedName = "Marriott"
        hotel3.lowRateInfo = HotelRate()
        hotel3.lowRateInfo.total = 300.0f
        hotel3.lowRateInfo.priceToShowUsers = -300.0f
        hotel3.lowRateInfo.currencyCode = "USD"
        hotel3.lowRateInfo.discountPercent = -10f
        hotel3.hotelGuestRating = 2.5f
        hotel3.proximityDistanceInMiles = 1.2
        hotel3.locationDescription = "Market St"
        hotel3.hotelStarRating = 2f
        hotel3.isVipAccess = false
        hotel3.rateCurrencyCode = "USD"
        hotel3.packageOfferModel = PackageOfferModel()
        hotel3.packageOfferModel.price = PackageOfferModel.PackagePrice()
        hotel3.packageOfferModel.price.tripSavings = Money()
        hotel3.packageOfferModel.price.tripSavings.amount = BigDecimal(16.5)

        response.hotelList.add(hotel1)
        response.hotelList.add(hotel2)
        response.hotelList.add(hotel3)

        return response
    }

    @Test
    fun testSortBy() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        vm.setHotelList(generateHotelSearchResponse())
        val expectedList = arrayListOf<List<String>>(
                // List Sorted by PRICE
                arrayListOf("Non Merchant Hotel", "happypath", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "no_guest_review", "vip_hotel", "air_attached_hotel", "hotel_price_change"),
                // List Sorted by DEALS
                arrayListOf("air_attached_hotel", "Non Merchant Hotel", "happypath", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "no_guest_review", "vip_hotel", "hotel_price_change"),
                // List Sorted by RATING
                arrayListOf("vip_hotel", "happypath", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "air_attached_hotel", "hotel_price_change", "Non Merchant Hotel", "no_guest_review"))
        val resultsList = ArrayList<List<String>>()

        vm.filterObservable
                .map { response ->
                    response.hotelList
                }
                .map { list ->
                    list.map { hotel ->
                        hotel.localizedName
                    }
                }
                .subscribe { listOfHotelNames ->
                    resultsList.add(listOfHotelNames)
                }
        vm.sortByObservable.onNext(DisplaySort.PRICE)
        vm.sortByObservable.onNext(DisplaySort.DEALS)
        vm.sortByObservable.onNext(DisplaySort.RATING)
        assertEquals(expectedList, resultsList)
    }

    private fun generateHotelSearchResponse(): HotelSearchResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/search/happy.json")
        val response = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return response
    }

    @Test
    fun testOriginalResponseShownWhenDefaultFilterOptions() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.ONE.value)
        vm = PackageFilterViewModel(context)
        vm.userFilterChoices = UserFilterChoices()
        vm.presetFilterOptions = false
        vm.originalResponse = generateHotelSearchResponse()
        val testSubscriber = TestObserver.create<HotelSearchResponse>()
        vm.filterObservable.subscribe(testSubscriber)
        vm.doneObservable.onNext(Unit)
        assertEquals(vm.originalResponse, testSubscriber.values()[0])
    }

    @Test
    fun testPreviousResultsIfFilterUnchanged() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.ONE.value)
        vm = PackageFilterViewModel(context)
        vm.userFilterChoices = UserFilterChoices()
        vm.userFilterChoices.name = "Test_Hotel"
        vm.userFilterChoices.isVipOnlyAccess = true
        vm.previousFilterChoices = vm.userFilterChoices.copy()
        val testSubscriber = TestObserver.create<Unit>()
        vm.showPreviousResultsObservable.subscribe(testSubscriber)
        vm.doneObservable.onNext(Unit)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testNewFilterChoicesHonored() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.ONE.value)
        vm = PackageFilterViewModel(context)
        vm.userFilterChoices = UserFilterChoices()
        vm.userFilterChoices.name = "Test_Hotel"
        vm.userFilterChoices.isVipOnlyAccess = true
        val testSubscriber = TestObserver.create<UserFilterChoices>()
        vm.filterChoicesObservable.subscribe(testSubscriber)
        vm.doneObservable.onNext(Unit)
        assertEquals(testSubscriber.values()[0], vm.userFilterChoices)
    }

    @Test
    fun testFilterAppliedTrackingFilterChoicesChangedClientSideFiltering() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        vm = PackageFilterViewModel(context)
        vm.userFilterChoices = UserFilterChoices()
        vm.userFilterChoices.name = "Test_Hotel"
        vm.userFilterChoices.isVipOnlyAccess = true
        vm.doneObservable.onNext(Unit)
        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testFilterAppliedNotTrackedFilterChoicesUnchangedClientSideFiltering() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        vm = PackageFilterViewModel(context)
        vm.userFilterChoices = UserFilterChoices()
        vm.userFilterChoices.name = "Test_Hotel"
        vm.userFilterChoices.isVipOnlyAccess = true
        vm.previousFilterChoices = vm.userFilterChoices
        vm.doneObservable.onNext(Unit)
        OmnitureTestUtils.assertLinkNotTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.Apply", mockAnalyticsProvider)
    }

    @Test
    fun testFilterAppliedNotTrackedFilterChoicesNotSetClientSideFiltering() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        vm = PackageFilterViewModel(context)
        vm.doneObservable.onNext(Unit)
        OmnitureTestUtils.assertLinkNotTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.Apply", mockAnalyticsProvider)
    }

    @Test
    fun testIsFilteredToZeroResults_noFiltersApplied() {
        vm.originalResponse = fakeFilteredResponse()
        assertFalse(vm.isFilteredToZeroResults())
    }

    @Test
    fun testIsFilteredToZeroResults_filterAppliedWithFilteredResults() {
        vm.originalResponse = fakeFilteredResponse()
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged("Hil", false)
        assertFalse(vm.isFilteredToZeroResults())
    }

    @Test
    fun testIsFilteredToZeroResults_filterAppliedWithNoResults() {
        vm.originalResponse = fakeFilteredResponse()
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged("Wrong_hotel_name", false)
        assertTrue(vm.isFilteredToZeroResults())
    }

    private fun assertResultsSortedByRecommended() {
        for (i in 1 until vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).sortIndex
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).sortIndex
            assertTrue(current >= previous)
        }
    }
}
