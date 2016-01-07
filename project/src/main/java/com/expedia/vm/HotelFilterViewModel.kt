package com.expedia.vm

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Strings
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.regex.Pattern
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.orEmpty
import kotlin.text.isBlank
import kotlin.text.isNotEmpty
import kotlin.text.toInt

class HotelFilterViewModel() {
    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<HotelSearchResponse>()

    var originalResponse: HotelSearchResponse? = null
    var filteredResponse: HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val neighborhoodExpandObservable = BehaviorSubject.create<Boolean>()
    val sortContainerObservable = BehaviorSubject.create<Boolean>()

    data class StarRatings(var one: Boolean = false, var two: Boolean = false, var three: Boolean = false, var four: Boolean = false, var five: Boolean = false)

    data class UserFilterChoices(var userSort: Sort = Sort.POPULAR,
                                 var isVipOnlyAccess: Boolean = false,
                                 var hotelStarRating: StarRatings = StarRatings(),
                                 var name: String = "",
                                 var minPrice: Int = 0,
                                 var maxPrice: Int = 0,
                                 var amenity: HashSet<Int> = HashSet<Int>(),
                                 var neighborhoods: HashSet<String> = HashSet<String>()) {

        fun filterCount(): Int {
            var count = 0
            if (hotelStarRating.one) count++
            if (hotelStarRating.two) count++
            if (hotelStarRating.three) count++
            if (hotelStarRating.four) count++
            if (hotelStarRating.five) count++
            if (isVipOnlyAccess == true) count++
            if (name.isNotEmpty()) count++
            if (neighborhoods.isNotEmpty()) count += neighborhoods.size
            if (amenity.isNotEmpty()) count += amenity.size
            if (minPrice != 0 || maxPrice != 0) count++
            return count
        }
    }

    data class PriceRange(val currencyCode: String, val minPrice: Int, val maxPrice: Int) {
        val notches = 30
        val defaultMinPriceText = formatValue(toValue(minPrice))
        val defaultMaxPriceTest = formatValue(toValue(maxPrice))

        private fun toValue(price: Int): Int = (((price.toFloat() - minPrice) / maxPrice) * notches).toInt()
        private fun toPrice(value: Int): Int = ((value.toFloat() / notches) * (maxPrice - minPrice) + minPrice).toInt()
        fun formatValue(value: Int): String {
            val price = toPrice(value)
            val str = Money(toPrice(value), currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
            if (price == maxPrice) {
                return str + "+"
            } else {
                return str
            }
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxPrice = toPrice(maxValue)
            return Pair(toPrice(minValue), if (newMaxPrice == maxPrice) 0 else newMaxPrice)
        }
    }

    val userFilterChoices = UserFilterChoices()
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()
    val amenityOptionsObservable = PublishSubject.create<Map<String, HotelSearchResponse.AmenityOptions>>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val amenityMapObservable = BehaviorSubject.create<Map<FilterAmenity, Int>>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    var previousSort = Sort.POPULAR
    var isNeighborhoodExpanded = false

    init {
        doneObservable.subscribe { params ->
            //if previousSort and userSort is both by popular(default), no need to call sort method. Otherwise, always do sort.
            if (userFilterChoices.userSort != Sort.POPULAR || previousSort != Sort.POPULAR) {
                previousSort = userFilterChoices.userSort
                sortObserver.onNext(userFilterChoices.userSort)
                HotelV2Tracking().trackHotelV2SortBy(Strings.capitalizeFirstLetter(userFilterChoices.userSort.toString()))
            }

            if (filteredResponse.hotelList != null && filteredResponse.hotelList.isNotEmpty()) {
                filteredResponse.isFilteredResponse = true
                filterObservable.onNext(filteredResponse)
            } else {
                filteredZeroResultObservable.onNext(Unit)
            }
        }

        clearObservable.subscribe { params ->
            resetUserFilters()
            filteredResponse.hotelList = originalResponse?.hotelList.orEmpty()
            doneButtonEnableObservable.onNext(true)
            filterCountObservable.onNext(userFilterChoices.filterCount())
            finishClear.onNext(Unit)
            sendNewPriceRange()
        }
    }

    fun handleFiltering() {
        filteredResponse.hotelList = originalResponse?.hotelList.orEmpty().filter { hotel -> isAllowed(hotel) }

        val filterCount = userFilterChoices.filterCount()
        val dynamicFeedbackWidgetCount = if (filterCount > 0) filteredResponse.hotelList.size else -1
        updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)
        doneButtonEnableObservable.onNext(filteredResponse.hotelList.size > 0)
        filterCountObservable.onNext(filterCount)
    }

    fun resetUserFilters() {
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = StarRatings()
        userFilterChoices.name = ""
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.amenity = HashSet<Int>()
        userFilterChoices.neighborhoods = HashSet<String>()
    }

    fun isAllowed(hotel: Hotel): Boolean {
        return filterIsVipAccess(hotel)
                && filterHotelStarRating(hotel)
                && filterName(hotel)
                && filterPriceRange(hotel)
                && filterAmenity(hotel)
                && filterNeighborhood(hotel)
    }

    fun filterIsVipAccess(hotel: Hotel): Boolean {
        if (userFilterChoices.isVipOnlyAccess == false) return true
        return userFilterChoices.isVipOnlyAccess == hotel.isVipAccess
    }

    fun filterHotelStarRating(hotel: Hotel): Boolean {
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

    fun filterName(hotel: Hotel): Boolean {
        val name = userFilterChoices.name
        if (name.isBlank()) return true
        val namePattern = Pattern.compile(".*" + userFilterChoices.name + ".*", Pattern.CASE_INSENSITIVE)
        return namePattern.matcher(hotel.localizedName).find()
    }

    fun filterPriceRange(hotel: Hotel): Boolean {
        return userFilterChoices.minPrice <= hotel.lowRateInfo.priceToShowUsers &&
                (userFilterChoices.maxPrice == 0 || hotel.lowRateInfo.priceToShowUsers <= userFilterChoices.maxPrice)
    }

    fun filterAmenity(hotel: Hotel): Boolean {
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

    fun filterNeighborhood(hotel: Hotel): Boolean {
        if (userFilterChoices.neighborhoods.isEmpty()) return true
        return userFilterChoices.neighborhoods.contains(hotel.locationDescription)
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver {
        userFilterChoices.isVipOnlyAccess = it
        handleFiltering()
        HotelV2Tracking().trackLinkHotelV2FilterVip(it)
    }

    val oneStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.one) {
            userFilterChoices.hotelStarRating.one = true
            hotelStarRatingBar.onNext(1)
        } else {
            userFilterChoices.hotelStarRating.one = false
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.two) {
            userFilterChoices.hotelStarRating.two = true
            hotelStarRatingBar.onNext(2)
        } else {
            userFilterChoices.hotelStarRating.two = false
            hotelStarRatingBar.onNext(7)
        }

        handleFiltering()
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.three) {
            userFilterChoices.hotelStarRating.three = true
            hotelStarRatingBar.onNext(3)
        } else {
            userFilterChoices.hotelStarRating.three = false
            hotelStarRatingBar.onNext(8)
        }

        handleFiltering()
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.four) {
            userFilterChoices.hotelStarRating.four = true
            hotelStarRatingBar.onNext(4)
        } else {
            userFilterChoices.hotelStarRating.four = false
            hotelStarRatingBar.onNext(9)
        }

        handleFiltering()
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.five) {
            userFilterChoices.hotelStarRating.five = true
            hotelStarRatingBar.onNext(5)
        } else {
            userFilterChoices.hotelStarRating.five = false
            hotelStarRatingBar.onNext(10)
        }

        handleFiltering()
    }

    val priceRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minPrice = p.first
        userFilterChoices.maxPrice = p.second
        HotelV2Tracking().trackHotelV2SortPriceSlider()
        handleFiltering()
    }

    var trackingDone = false

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        handleFiltering()
        if (s.length == 1 && !trackingDone) {
            trackingDone = true
            HotelV2Tracking().trackLinkHotelV2FilterByName()
        }
        if (s.length == 0) trackingDone = false
    }

    fun setHotelList(response: HotelSearchResponse) {
        originalResponse = response
        neighborhoodListObservable.onNext(response.allNeighborhoodsInSearchRegion)
        filteredResponse.hotelList = ArrayList(response.hotelList)
        filteredResponse.userPriceType = response.userPriceType
        //hide amenities
        //if (response.amenityFilterOptions != null) {
        //  amenityOptionsObservable.onNext(response.amenityFilterOptions)
        //}

        sendNewPriceRange()
        isNeighborhoodExpanded = false
        previousSort = Sort.POPULAR
    }

    private fun sendNewPriceRange() {
        val response = originalResponse
        if (response != null && response.priceOptions.isNotEmpty()) {
            val min = response.priceOptions.first().minPrice
            val max = response.priceOptions.last().minPrice
            val currency = response.hotelList.orEmpty().first().rateCurrencyCode
            newPriceRangeObservable.onNext(PriceRange(currency, min, max))
        }
    }

    public enum class Sort {
        POPULAR,
        PRICE,
        DEALS,
        RATING,
        DISTANCE,
    }

    val sortObserver = endlessObserver<Sort> { sort ->
        val hotels: List<Hotel> = filteredResponse.hotelList

        when (sort) {
            Sort.POPULAR -> Collections.sort(hotels, popular_comparator)
            Sort.PRICE -> Collections.sort(hotels, price_comparator)
            Sort.RATING -> Collections.sort(hotels, rating_comparator_fallback_price)
            Sort.DEALS -> Collections.sort(hotels, deals_comparator)
            Sort.DISTANCE -> Collections.sort(hotels, distance_comparator_fallback_name)
        }
        filteredResponse.hotelList = HotelServices.putSponsoredItemsInCorrectPlaces(hotels)
    }

    private val popular_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            return hotel1.sortIndex.compareTo(hotel2.sortIndex)
        }
    }

    private val name_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            return hotel1.localizedName.compareTo(hotel2.localizedName)
        }
    }

    private val price_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val lowRate1 = hotel1.lowRateInfo?.priceToShowUsers
            val lowRate2 = hotel2.lowRateInfo?.priceToShowUsers

            if (lowRate1 == null && lowRate2 == null) {
                return name_comparator.compare(hotel1, hotel2)
            } else if (lowRate1 == null) {
                return -1
            } else if (lowRate2 == null) {
                return 1
            }

            // Compare rates
            return lowRate1.compareTo(lowRate2)
        }
    }

    private val deals_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            return hotel1.lowRateInfo.discountPercent.compareTo(hotel2.lowRateInfo.discountPercent)
        }
    }

    private val rating_comparator_fallback_price: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val comparison = hotel2.hotelGuestRating.compareTo(hotel1.hotelGuestRating)
            return if (comparison != 0) comparison else price_comparator.compare(hotel1, hotel2)
        }
    }

    private val distance_comparator_fallback_name: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val distance1 = hotel1.proximityDistanceInMiles
            val distance2 = hotel2.proximityDistanceInMiles

            val cmp = distance1.compareTo(distance2)
            if (cmp == 0) {
                return name_comparator.compare(hotel1, hotel2)
            } else {
                return cmp
            }
        }
    }


    val selectAmenity: Observer<Int> = endlessObserver { amenityId ->
        if (userFilterChoices.amenity.isEmpty() || !userFilterChoices.amenity.contains(amenityId)) {
            userFilterChoices.amenity.add(amenityId)
        } else {
            userFilterChoices.amenity.remove(amenityId)
        }

        handleFiltering()
    }

    val selectNeighborhood = endlessObserver<String> { region ->
        if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(region)) {
            userFilterChoices.neighborhoods.add(region)
        } else {
            userFilterChoices.neighborhoods.remove(region)
        }

        handleFiltering()
    }

    val neighborhoodMoreLessObservable: Observer<Unit> = endlessObserver {
        if (!isNeighborhoodExpanded) {
            isNeighborhoodExpanded = true
        } else {
            isNeighborhoodExpanded = false
        }
        neighborhoodExpandObservable.onNext(isNeighborhoodExpanded)
    }

    fun isFilteredToZeroResults(): Boolean {
        return userFilterChoices.filterCount() > 0 && filteredResponse.hotelList.isEmpty()
    }
}

