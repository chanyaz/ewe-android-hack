package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Strings
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.regex.Pattern

open class HotelClientFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {
    var filteredResponse: HotelSearchResponse = HotelSearchResponse()

    private var previousSortType = Sort.RECOMMENDED

    private val sortObserver = endlessObserver<Sort> { sort ->
        if (sort != previousSortType) {
            previousSortType = sort
            val hotels: List<Hotel> = filteredResponse.hotelList

            when (sort) {
                Sort.RECOMMENDED -> Collections.sort(hotels, popular_comparator)
                Sort.PRICE -> Collections.sort(hotels, price_comparator)
                Sort.RATING -> Collections.sort(hotels, rating_comparator_fallback_price)
                Sort.DEALS -> Collections.sort(hotels, deals_comparator)
                Sort.PACKAGE_DISCOUNT -> Collections.sort(hotels, package_discount_comparator)
                Sort.DISTANCE -> Collections.sort(hotels, distance_comparator_fallback_name)
            }
            setFilteredHotelListAndRetainLoyaltyInformation(HotelServices.putSponsoredItemsInCorrectPlaces(hotels))

            val sortByString: String = Strings.capitalizeFirstLetter(sort.toString())
            trackHotelSortBy(sortByString)
        }

        if (filteredResponse.hotelList != null && filteredResponse.hotelList.isNotEmpty()) {
            filteredResponse.isFilteredResponse = true
            filterObservable.onNext(filteredResponse)
        } else {
            filteredZeroResultObservable.onNext(Unit)
        }
    }

    init {
        sortByObservable.subscribe(sortObserver)

        doneObservable.subscribe {
            sortByObservable.onNext(userFilterChoices.userSort)
        }

        clearObservable.subscribe {
            setFilteredHotelListAndRetainLoyaltyInformation(originalResponse?.hotelList.orEmpty())
            previousSortType = Sort.RECOMMENDED   //the original response is always sorted by Recommended
        }
    }

    override fun setHotelList(response: HotelSearchResponse) {
        setFilteredHotelListAndRetainLoyaltyInformation(ArrayList(response.hotelList))
        filteredResponse.userPriceType = response.userPriceType
        previousSortType = Sort.RECOMMENDED
        super.setHotelList(response)
    }

    override fun handleFiltering() {
        setFilteredHotelListAndRetainLoyaltyInformation(originalResponse?.hotelList.orEmpty().filter { hotel -> isAllowed(hotel) })

        val filterCount = userFilterChoices.filterCount()
        val dynamicFeedbackWidgetCount = if (filterCount > 0) filteredResponse.hotelList.size else -1
        updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)

        doneButtonEnableObservable.onNext(filteredResponse.hotelList.size > 0)
    }

    override fun isFilteredToZeroResults(): Boolean {
        return userFilterChoices.filterCount() > 0 && filteredResponse.hotelList.isEmpty()
    }

    override fun isClientSideFiltering(): Boolean {
        return true
    }

    override fun showHotelFavorite(): Boolean {
        return true
    }

    private fun setFilteredHotelListAndRetainLoyaltyInformation(hotelList: List<Hotel>) {
        filteredResponse.hotelList = hotelList
        filteredResponse.setHasLoyaltyInformation()
    }

    private fun isAllowed(hotel: Hotel): Boolean {
        return filterIsVipAccess(hotel)
                && filterHotelStarRating(hotel)
                && filterName(hotel)
                && filterPriceRange(hotel)
                && filterAmenity(hotel)
                && filterNeighborhood(hotel)
                && filterFavorites(hotel)
    }

    private fun filterIsVipAccess(hotel: Hotel): Boolean {
        if (userFilterChoices.isVipOnlyAccess == false) return true
        return userFilterChoices.isVipOnlyAccess == hotel.isVipAccess
    }

    private fun filterHotelStarRating(hotel: Hotel): Boolean {
        if (!userFilterChoices.hotelStarRating.one &&
                !userFilterChoices.hotelStarRating.two &&
                !userFilterChoices.hotelStarRating.three &&
                !userFilterChoices.hotelStarRating.four && !userFilterChoices.hotelStarRating.five) return true

        return (1.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.one) ||
                (2.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.two) ||
                (3.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.three) ||
                (4.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.four) ||
                (5.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.five)
    }

    private fun filterName(hotel: Hotel): Boolean {
        val name = userFilterChoices.name
        if (name.isBlank()) return true
        val namePattern = Pattern.compile(".*" + userFilterChoices.name + ".*", Pattern.CASE_INSENSITIVE)
        return namePattern.matcher(hotel.localizedName).find()
    }

    private fun filterPriceRange(hotel: Hotel): Boolean {
        if (hotel.isSoldOut) {
            //Check if price filters have not been changed
            return userFilterChoices.minPrice == 0 && userFilterChoices.maxPrice == 0;
        }
        val price = hotel.lowRateInfo.priceToShowUsers
        return (userFilterChoices.minPrice == 0 && price < 0) || (userFilterChoices.minPrice <= price &&
                (userFilterChoices.maxPrice == 0 || price <= userFilterChoices.maxPrice))
    }

    private fun filterAmenity(hotel: Hotel): Boolean {
        if (userFilterChoices.amenity.isEmpty()) return true
        if (hotel.amenities == null) return false
        if (hotel.amenityFilterIdList == null) {
            hotel.amenityFilterIdList = mapAmenitiesToFilterId(hotel.amenities)
        }

        for (i in userFilterChoices.amenity) {
            if (!hotel.amenityFilterIdList.contains(i)) {
                return false
            }
        }
        return true
    }

    private fun mapAmenitiesToFilterId(amenities: List<Hotel.HotelAmenity>): List<Int> {
        var list = ArrayList<Int>()
        for (amenity in amenities) {
            list.add(FilterAmenity.amenityIdToFilterId(amenity.id.toInt()))
        }
        return list
    }

    private fun filterNeighborhood(hotel: Hotel): Boolean {
        if (userFilterChoices.neighborhoods.isEmpty()) return true
        return userFilterChoices.neighborhoods.contains(hotel.locationDescription)
    }

    fun filterFavorites(hotel: Hotel): Boolean {
        if (!userFilterChoices.favorites) return true
        return HotelFavoriteHelper.isHotelFavorite(context, hotel.hotelId) && !hotel.isSponsoredListing
    }

    private val popular_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel1.sortIndex.compareTo(hotel2.sortIndex)
    }

    private val name_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel1.localizedName.compareTo(hotel2.localizedName)
    }

    private val price_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val lowRate1 = hotel1.lowRateInfo?.priceToShowUsers
        val lowRate2 = hotel2.lowRateInfo?.priceToShowUsers

        if (lowRate1 == null && lowRate2 == null) {
            return@Comparator name_comparator.compare(hotel1, hotel2)
        } else if (lowRate1 == null) {
            return@Comparator 1
        } else if (lowRate2 == null) {
            return@Comparator -1
        }

        lowRate1.compareTo(lowRate2)
    }

    private val deals_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val discountPercent1 = hotel1.lowRateInfo?.discountPercent
        val discountPercent2 = hotel2.lowRateInfo?.discountPercent

        if (discountPercent1 == null && discountPercent2 == null) {
            return@Comparator name_comparator.compare(hotel1, hotel2)
        } else if (discountPercent1 == null) {
            return@Comparator 1
        } else if (discountPercent2 == null) {
            return@Comparator -1
        }

        discountPercent1.compareTo(discountPercent2)
    }

    private val package_discount_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel2.packageOfferModel.price.tripSavings.amount.compareTo(hotel1.packageOfferModel.price.tripSavings.amount)
    }

    private val rating_comparator_fallback_price: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val comparison = hotel2.hotelGuestRating.compareTo(hotel1.hotelGuestRating)
        if (comparison != 0) comparison else price_comparator.compare(hotel1, hotel2)
    }

    private val distance_comparator_fallback_name: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val distance1 = hotel1.proximityDistanceInMiles
        val distance2 = hotel2.proximityDistanceInMiles

        val cmp = distance1.compareTo(distance2)
        if (cmp == 0) {
            name_comparator.compare(hotel1, hotel2)
        } else {
            cmp
        }
    }
}
