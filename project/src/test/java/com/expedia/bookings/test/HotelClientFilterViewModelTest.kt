package com.expedia.bookings.test

import android.preference.PreferenceManager
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.HotelClientFilterViewModel
import org.junit.Before
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
class HotelClientFilterViewModelTest {
    var vm: HotelClientFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelClientFilterViewModel(context)
    }

    @Test
    fun filterVip() {
        vm.vipFilteredObserver.onNext(true)
        assertEquals(true, vm.userFilterChoices.isVipOnlyAccess)

        vm.vipFilteredObserver.onNext(false)
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
    }

    @Test
    fun filterStars() {
        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.one)

        vm.userFilterChoices.hotelStarRating.two = false
        vm.twoStarFilterObserver.onNext(Unit)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.two)

        vm.oneStarFilterObserver.onNext(Unit)
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
    }

    @Test
    fun filterName() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)
        assertEquals(str, vm.userFilterChoices.name)
    }

    @Test
    fun clearFilters() {
        val ogResponse = fakeFilteredResponse()
        vm.originalResponse = ogResponse

        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)

        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        vm.vipFilteredObserver.onNext(true)

        vm.userFilterChoices.minPrice = 20
        vm.userFilterChoices.maxPrice = 50

        var neighborhood = HotelSearchResponse.Neighborhood()
        neighborhood.name = "Civic Center"
        vm.selectNeighborhood.onNext(neighborhood)

        vm.clearObservable.onNext(Unit)

        assertTrue(vm.userFilterChoices.name.isBlank())
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
        assertTrue(vm.userFilterChoices.neighborhoods.isEmpty())
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
        vm.filterHotelNameObserver.onNext(str)
        assertEquals(1, vm.filteredResponse.hotelList.size)

        str = "junk"
        vm.filterHotelNameObserver.onNext(str)
        assertEquals(0, vm.filteredResponse.hotelList.size)

        str = ""
        vm.filterHotelNameObserver.onNext(str)
        assertEquals(3, vm.filteredResponse.hotelList.size)

        vm.userFilterChoices.hotelStarRating.three = false
        vm.threeStarFilterObserver.onNext(Unit)
        assertEquals(1, vm.filteredResponse.hotelList.size)

        vm.clearObservable.onNext(Unit)
        vm.vipFilteredObserver.onNext(true)
        assertEquals(1, vm.filteredResponse.hotelList.size)

        vm.clearObservable.onNext(Unit)
        vm.priceRangeChangedObserver.onNext(Pair(0, 10))
        assertEquals(1, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun filterAmenity() {
        var amenityId = 16
        vm.originalResponse = fakeFilteredResponse()
        vm.selectAmenity.onNext(amenityId)
        assertEquals(1, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun filterNeighborhood() {
        var neighborhood = HotelSearchResponse.Neighborhood()
        neighborhood.name = "Civic Center"
        vm.originalResponse = fakeFilteredResponse()
        vm.selectNeighborhood.onNext(neighborhood)
        assertEquals(neighborhood.name, vm.filteredResponse.hotelList.first().locationDescription)
        assertTrue(vm.filteredResponse.hotelList.size == 1)

        vm.deselectNeighborhood.onNext(neighborhood)
        assertTrue(vm.filteredResponse.hotelList.size == vm.originalResponse!!.hotelList.size)
    }

    @Test
    fun emptyFilters() {
        vm.doneObservable.onNext(Unit)
        assertEquals(0, vm.filteredResponse.hotelList.size)
    }

    @Test
    fun sortByPopular() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.RECOMMENDED)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).sortIndex
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).sortIndex
            assertTrue(current >= previous)
        }
    }

    @Test
    fun sortByPrice() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.PRICE)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.priceToShowUsers
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.priceToShowUsers
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByDeals() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.DEALS)
        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val currentDeals = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.discountPercent
            val previousDeals = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.discountPercent
            assertTrue(currentDeals >= previousDeals, "Expected $currentDeals >= $previousDeals")
        }
    }

    @Test
    fun sortByPackageDiscount() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.PACKAGE_DISCOUNT)
        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val currentDeals = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.discountPercent
            val previousDeals = vm.filteredResponse.hotelList.elementAt(i - 1).lowRateInfo.discountPercent
            assertTrue(currentDeals >= previousDeals, "Expected $currentDeals >= $previousDeals")
        }
    }

    @Test
    fun sortByRating() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.RATING)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).hotelGuestRating
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).hotelGuestRating
            assertTrue(current <= previous, "Expected $current <= $previous")
        }
    }

    @Test
    fun sortByDistance() {
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortByObservable.onNext(Sort.DISTANCE)

        for (i in 1..vm.filteredResponse.hotelList.size - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).proximityDistanceInMiles
            val previous = vm.filteredResponse.hotelList.elementAt(i - 1).proximityDistanceInMiles
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun filterCount() {
        vm.originalResponse = fakeFilteredResponse()
        var str = "Hil"
        vm.filterHotelNameObserver.onNext(str)
        assertTrue(vm.filterCountObservable.value == 1)

        vm.userFilterChoices.hotelStarRating.three = false
        vm.threeStarFilterObserver.onNext(Unit)
        assertTrue(vm.filterCountObservable.value == 2)

        vm.userFilterChoices.hotelStarRating.four = false
        vm.fourStarFilterObserver.onNext(Unit)
        assertTrue(vm.filterCountObservable.value == 3)

        vm.fourStarFilterObserver.onNext(Unit)
        assertTrue(vm.filterCountObservable.value == 2)

        vm.selectAmenity.onNext(16)
        assertTrue(vm.filterCountObservable.value == 3)

        vm.selectAmenity.onNext(1)
        assertTrue(vm.filterCountObservable.value == 4)

        vm.selectAmenity.onNext(16)
        assertTrue(vm.filterCountObservable.value == 3)

        vm.vipFilteredObserver.onNext(true)
        assertTrue(vm.filterCountObservable.value == 4)

        var neighborhood1 = HotelSearchResponse.Neighborhood()
        neighborhood1.name = "Civic Center"
        var neighborhood2 = HotelSearchResponse.Neighborhood()
        neighborhood2.name = "Fisherman's Wharf"
        vm.selectNeighborhood.onNext(neighborhood1)
        assertTrue(vm.filterCountObservable.value == 5)

        vm.selectNeighborhood.onNext(neighborhood2)
        assertTrue(vm.filterCountObservable.value == 6)

        vm.deselectNeighborhood.onNext(neighborhood2)
        assertTrue(vm.filterCountObservable.value == 5)

        vm.deselectNeighborhood.onNext(neighborhood1)
        assertTrue(vm.filterCountObservable.value == 4)
    }

    @Test
    fun filterFavorite() {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelFavoriteTest,
                AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        Db.setAbacusResponse(abacusResponse)
        val ogResponse = fakeFilteredResponse()
        val hotel = ogResponse.hotelList[0]
        hotel.isSponsoredListing = true
        hotel.hotelId = "abc"
        vm.userFilterChoices.favorites = true

        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(vm.context)
        sharedPreference.edit().clear().apply()
        HotelFavoriteHelper.toggleHotelFavoriteState(vm.context, hotel.hotelId)

        assertFalse(vm.filterFavorites(hotel))

        hotel.isSponsoredListing = false
        assertTrue(vm.filterFavorites(hotel))
    }

    private fun fakeFilteredResponse(): HotelSearchResponse {
        val response = HotelSearchResponse()
        response.priceOptions = listOf(HotelSearchResponse.PriceOption(), HotelSearchResponse.PriceOption())
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
        var amenities1 = ArrayList<Hotel.HotelAmenity>()
        var amenity1 = Hotel.HotelAmenity()
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
        var amenities2 = ArrayList<Hotel.HotelAmenity>()
        var amenity2 = Hotel.HotelAmenity()
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
        vm.setHotelList(generateHotelSearchResponse())
        val expectedList = arrayListOf<List<String>>(
                // List Sorted by PRICE
                arrayListOf("happypath", "Non Merchant Hotel", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "no_guest_review", "vip_hotel", "air_attached_hotel", "hotel_price_change"),
                // List Sorted by DEALS
                arrayListOf("happypath", "air_attached_hotel", "Non Merchant Hotel", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "no_guest_review", "vip_hotel", "hotel_price_change"),
                // List Sorted by RATING
                arrayListOf("happypath", "vip_hotel", "error_room_unavailable", "error_create_trip", "error_checkout_session_timeout", "error_checkout_card_limit_exceeded", "Sold_out_hotel", "valid_forms_of_payment", "hotel_coupon_errors", "hotel_etp_renovation_resort", "hotel_etp_renovation_resort_with_free_cancellation", "hotel_non_etp_with_free_cancellation", "error_checkout_card", "error_checkout_traveller_info", "error_checkout_unknown", "error_checkout_trip_already_booked", "hotel_email_opt_in", "tealeaf_id", "sold_out_hotel_with_2_rooms", "happypath_pwp", "visa_not_supported", "happypath_with_loyalty_points", "air_attached_hotel", "hotel_price_change", "Non Merchant Hotel", "no_guest_review"))
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
        vm.sortByObservable.onNext(Sort.PRICE)
        vm.sortByObservable.onNext(Sort.DEALS)
        vm.sortByObservable.onNext(Sort.RATING)
        assertEquals(expectedList, resultsList)
    }

    private fun generateHotelSearchResponse(): HotelSearchResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/search/happy.json")
        val response = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return response
    }
}
