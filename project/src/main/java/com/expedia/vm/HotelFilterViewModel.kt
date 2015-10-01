package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.regex.Pattern
import java.util.HashMap

class HotelFilterViewModel(val context: Context) {
    val doneObservable = PublishSubject.create<Unit>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<List<Hotel>>()

    var originalResponse : HotelSearchResponse? = null
    var filteredResponse : HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()

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

    init {
        doneObservable.subscribe { params ->
            if (userFilterChoices.userSort != Sort.POPULAR) {
                sortObserver.onNext(userFilterChoices.userSort)
            }
            if (filteredResponse.hotelList == null) {
                filterObservable.onNext(originalResponse?.hotelList)
            } else {
                if (userFilterChoices.userSort != Sort.POPULAR) {
                    sortObserver.onNext(userFilterChoices.userSort)
                }
                filterObservable.onNext(filteredResponse.hotelList)
            }
        }

        clearObservable.subscribe {params ->
            resetUserFilters()
            handleFiltering()
            finishClear.onNext(Unit)
        }
    }

    fun handleFiltering() {
        filteredResponse.hotelList = ArrayList<Hotel>()
        filteredResponse.allNeighborhoodsInSearchRegion = originalResponse?.allNeighborhoodsInSearchRegion

        for (hotel in originalResponse?.hotelList.orEmpty()) {
            processFilters(hotel)
        }

        updateDynamicFeedbackWidget.onNext(filteredResponse.hotelList.size())
        filterCountObservable.onNext(getFilterCount())
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

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        handleFiltering()
    }

    fun setHotelList(response : HotelSearchResponse) {
        originalResponse = response
        neighborhoodListObservable.onNext(response.allNeighborhoodsInSearchRegion)
        if (response.amenityFilterOptions != null) {
            amenityOptionsObservable.onNext(response.amenityFilterOptions)
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
        var preSortHotelList = if (filteredResponse.hotelList == null) originalResponse?.hotelList else filteredResponse.hotelList

        when (sort) {
            Sort.PRICE -> Collections.sort(preSortHotelList, price_comparator)
            Sort.RATING -> Collections.sort(preSortHotelList, rating_comparator_fallback_price)
            Sort.DEALS -> Collections.sort(preSortHotelList, deals_comparator)
            Sort.DISTANCE -> Collections.sort(preSortHotelList, distance_comparator_fallback_name)
        }
    }


    private val name_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            return hotel1.localizedName.compareTo(hotel2.localizedName)
        }
    }

    private val price_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val lowRate1 = hotel1.lowRateInfo?.getDisplayTotalPrice()
            val lowRate2 = hotel2.lowRateInfo?.getDisplayTotalPrice()

            if (lowRate1 == null && lowRate2 == null) {
                return name_comparator.compare(hotel1, hotel2)
            } else if (lowRate1 == null) {
                return -1
            } else if (lowRate2 == null) {
                return 1
            }

            // Compare rates
            return lowRate1.getAmount().compareTo(lowRate2.getAmount())
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

    fun getFilterCount() : Int {
        var count = 0
        if (userFilterChoices.hotelStarRating.one ||
                userFilterChoices.hotelStarRating.two ||
                userFilterChoices.hotelStarRating.three ||
                userFilterChoices.hotelStarRating.four || userFilterChoices.hotelStarRating.five) count++
        if (userFilterChoices.isVipOnlyAccess == true) count++
        if (!userFilterChoices.name.isNullOrEmpty()) count++
        if (userFilterChoices.neighborhoods.isNotEmpty()) count++
        if (userFilterChoices.price != null) count++
        return count
    }
}


