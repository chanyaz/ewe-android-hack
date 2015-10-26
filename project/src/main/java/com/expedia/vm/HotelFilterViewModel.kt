package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
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

class HotelFilterViewModel(val context: Context) {
    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<List<Hotel>>()

    var originalResponse : HotelSearchResponse? = null
    var filteredResponse : HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val neighborhoodExpandObserable = BehaviorSubject.create<Boolean>()
    val sortContainerObservable = BehaviorSubject.create<Boolean>()

    data class StarRatings(var one: Boolean = false, var two: Boolean = false, var three: Boolean = false, var four: Boolean = false, var five: Boolean = false)

    data class UserFilterChoices(var userSort: Sort = Sort.POPULAR,
                                 var isVipOnlyAccess: Boolean? = false,
                                 var hotelStarRating: StarRatings = StarRatings(),
                                 var name: String? = null,
                                 var price: Float? = null,
                                 var amenity: HashSet<Int> = HashSet<Int>(),
                                 var neighborhoods: HashSet<String> = HashSet<String>())

    val userFilterChoices = UserFilterChoices()
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()
    val amenityOptionsObservable = PublishSubject.create<Map<String, HotelSearchResponse.AmenityOptions>>()
    val amenityMapObservable = BehaviorSubject.create<Map<FilterAmenity, Int>>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    var didFilter = false
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

            if (!didFilter) {
                filterObservable.onNext(originalResponse?.hotelList)
            } else if (filteredResponse.hotelList.isNotEmpty()) {
                filteredResponse.isFilteredResponse = true
                filterObservable.onNext(filteredResponse.hotelList)
            } else {
                filteredZeroResultObservable.onNext(Unit)
            }
        }

        clearObservable.subscribe {params ->
            if (filteredResponse.hotelList != null) {
                resetUserFilters()
                filteredResponse.hotelList.clear()
                doneButtonEnableObservable.onNext(true)
                filterCountObservable.onNext(0)
                finishClear.onNext(Unit)
                didFilter = false
                HotelV2Tracking().trackLinkHotelV2ClearFilter()
            }
        }
    }

    fun handleFiltering() {
        if (filteredResponse.hotelList == null) {
            filteredResponse.hotelList = ArrayList<Hotel>()
        } else {
            filteredResponse.hotelList.clear()
        }

        for (hotel in originalResponse?.hotelList.orEmpty()) {
            processFilters(hotel)
        }

        val filterCount = getFilterCount()
        val dynamicFeedbackWidgetCount = if (filterCount > 0) filteredResponse.hotelList.size() else -1
        updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)
        doneButtonEnableObservable.onNext(filteredResponse.hotelList.size() > 0)
        filterCountObservable.onNext(filterCount)
        didFilter = true
    }

    fun resetUserFilters() {
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = StarRatings()
        userFilterChoices.name = null
        userFilterChoices.price = null
        userFilterChoices.amenity = HashSet<Int>()
        userFilterChoices.neighborhoods = HashSet<String>()
    }

    fun processFilters(hotel : Hotel) {
        if (filterIsVipAccess(hotel) && filterHotelStarRating(hotel) && filterName(hotel) && filterPrice(hotel) && filterAmenity(hotel) && filterNeighborhood(hotel)) {
            filteredResponse.hotelList.add(hotel)
        }
    }

    fun filterIsVipAccess(hotel : Hotel) : Boolean {
        if (userFilterChoices.isVipOnlyAccess == false) return true
        return userFilterChoices.isVipOnlyAccess == hotel.isVipAccess
    }

    fun filterHotelStarRating(hotel: Hotel) : Boolean {
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

    fun filterName(hotel: Hotel) : Boolean {
        if (userFilterChoices.name.isNullOrEmpty()) return true
        var namePattern: Pattern? = null
        if (userFilterChoices.name != null) {
            namePattern = Pattern.compile(".*" + userFilterChoices.name + ".*", Pattern.CASE_INSENSITIVE)
        }
        return namePattern == null || namePattern.matcher(hotel.localizedName).find()
    }

    fun filterPrice(hotel: Hotel) : Boolean {
        if (userFilterChoices.price == null) return true
        return userFilterChoices.price == hotel.lowRateInfo.priceToShowUsers
    }

    fun filterAmenity(hotel: Hotel) : Boolean {
        if (userFilterChoices.amenity.isEmpty()) return true
        if (hotel.amenities == null) return false
        if (hotel.amenityFilterIdList == null) {
            hotel.amenityFilterIdList = mapAmenitiesToFilterId(hotel.amenities)
        }

        for (i in userFilterChoices.amenity) {
            if (!hotel.amenityFilterIdList.contains(i)){
                return false
            }
        }
        return true
    }

    private fun mapAmenitiesToFilterId(amenities: List<Hotel.HotelAmenity>) : List<Int> {
        var list = ArrayList<Int>()
        for (amenity in amenities) {
            list.add(FilterAmenity.amenityIdToFilterId(amenity.id.toInt()))
        }
        return list
    }

    fun filterNeighborhood(hotel: Hotel) : Boolean {
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

    var trackingDone = false

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        handleFiltering()
        if (s.length() == 1 && !trackingDone) {
            trackingDone = true
            HotelV2Tracking().trackLinkHotelV2FilterByName()
        }
        if(s.length() == 0) trackingDone = false
    }

    fun setHotelList(response : HotelSearchResponse) {
        originalResponse = response
        neighborhoodListObservable.onNext(response.allNeighborhoodsInSearchRegion)
        filteredResponse.userPriceType = response.userPriceType
//        hide amenities
//        if (response.amenityFilterOptions != null) {
//            amenityOptionsObservable.onNext(response.amenityFilterOptions)
//        }
        isNeighborhoodExpanded = false
        previousSort = Sort.POPULAR
    }

    public enum class Sort {
        POPULAR,
        PRICE,
        DEALS,
        RATING,
        DISTANCE,
    }

    val sortObserver = endlessObserver<Sort> { sort ->
        var preSortHotelList = if (!didFilter) originalResponse?.hotelList else filteredResponse.hotelList

        when (sort) {
            Sort.POPULAR -> Collections.sort(preSortHotelList, popular_comparator)
            Sort.PRICE -> Collections.sort(preSortHotelList, price_comparator)
            Sort.RATING -> Collections.sort(preSortHotelList, rating_comparator_fallback_price)
            Sort.DEALS -> Collections.sort(preSortHotelList, deals_comparator)
            Sort.DISTANCE -> Collections.sort(preSortHotelList, distance_comparator_fallback_name)
        }
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

    val neighborhoodMoreLessObserverable: Observer<Unit> = endlessObserver {
        if (!isNeighborhoodExpanded) {
            isNeighborhoodExpanded = true
        } else {
            isNeighborhoodExpanded = false
        }
        neighborhoodExpandObserable.onNext(isNeighborhoodExpanded)
    }

    fun getFilterCount() : Int {
        var count = 0
        if (userFilterChoices.hotelStarRating.one) count++
        if (userFilterChoices.hotelStarRating.two) count++
        if (userFilterChoices.hotelStarRating.three) count++
        if (userFilterChoices.hotelStarRating.four) count++
        if (userFilterChoices.hotelStarRating.five) count++
        if (userFilterChoices.isVipOnlyAccess == true) count++
        if (!userFilterChoices.name.isNullOrEmpty()) count++
        if (userFilterChoices.neighborhoods.isNotEmpty()) count += userFilterChoices.neighborhoods.size()
        if (userFilterChoices.amenity.isNotEmpty()) count += userFilterChoices.amenity.size()
        if (userFilterChoices.price != null) count++
        return count
    }

    fun isFilteredToZeroResults(): Boolean {
        return didFilter && filteredResponse.hotelList.isEmpty()
    }
}


