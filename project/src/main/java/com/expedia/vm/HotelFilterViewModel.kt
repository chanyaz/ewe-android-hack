package com.expedia.vm

import android.content.Context
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.Collections
import java.util.Comparator
import java.util.ArrayList
import java.util.regex.Pattern
import java.util.HashSet

class HotelFilterViewModel(val context: Context) {
    val doneObservable = PublishSubject.create<Unit>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<List<Hotel>>()

    var originalResponse : HotelSearchResponse? = null
    var filteredResponse : HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val finishClear = BehaviorSubject.create<Unit>()

    data class StarRatings(var one: Boolean = false, var two: Boolean = false, var three: Boolean = false, var four: Boolean = false, var five: Boolean = false)
    data class UserFilterChoices(var userSort: Sort = Sort.POPULAR, var isVipAccess : Boolean? = null, var hotelStarRating : StarRatings = StarRatings(), var name : String? = null, var price : Float? = null, var neighborhoods : HashSet<String> = HashSet<String>())

    val userFilterChoices = UserFilterChoices()
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()

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
    }

    fun resetUserFilters() {
        userFilterChoices.isVipAccess = null
        userFilterChoices.hotelStarRating = StarRatings()
        userFilterChoices.name = null
        userFilterChoices.price = null
        userFilterChoices.neighborhoods = HashSet<String>()
    }

    fun processFilters(hotel : Hotel) {
        if (filterIsVipAccess(hotel) && filterHotelStarRating(hotel) && filterName(hotel) && filterPrice(hotel) && filterNeighborhood(hotel)) {
            filteredResponse.hotelList.add(hotel)
        }
    }

    fun filterIsVipAccess(hotel : Hotel) : Boolean {
        if (userFilterChoices.isVipAccess == null) return true
        return userFilterChoices.isVipAccess == hotel.isVipAccess
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

    fun filterNeighborhood(hotel: Hotel) : Boolean {
        if (userFilterChoices.neighborhoods.isEmpty()) return true
        return userFilterChoices.neighborhoods.contains(hotel.locationDescription)
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver {
        userFilterChoices.isVipAccess = it
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
    }

    public enum class Sort private constructor(public val descriptionResId: Int) {
        POPULAR(R.string.sort_description_popular),
        PRICE(R.string.sort_description_price),
        DEALS(R.string.sort_description_deals),
        RATING(R.string.sort_description_rating),
        DISTANCE(R.string.sort_description_distance)
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
            val deal1 = hotel1.lowRateInfo
            val deal2 = hotel2.lowRateInfo

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

            if (distance1 == null && distance2 == null) {
                return price_comparator.compare(hotel1, hotel2)
            } else if (distance1 == null) {
                return -1
            } else if (distance2 == null) {
                return 1
            }

            val cmp = distance1!!.compareTo(distance2)
            if (cmp == 0) {
                return name_comparator.compare(hotel1, hotel2)
            } else {
                return cmp
            }
        }
    }

    val selectNeighborhood = endlessObserver<String> { region ->
        if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(region)) {
            userFilterChoices.neighborhoods.add(region)
        } else {
            userFilterChoices.neighborhoods.remove(region)
        }

        handleFiltering()
    }

}


