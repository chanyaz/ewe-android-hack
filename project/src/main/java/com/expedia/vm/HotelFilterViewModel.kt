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
    data class FilterToggles(var isVipAccess : Boolean? = null, var hotelStarRating : StarRatings = StarRatings(), var name : String? = null, var price : Float? = null, var neighborhoods : List<String>? = null)

    val filterToggles = FilterToggles()

    init {
        doneObservable.subscribe { params ->
            if (filteredResponse.hotelList == null) {
                filterObservable.onNext(originalResponse?.hotelList)
            } else {
                filterObservable.onNext(filteredResponse.hotelList)
            }
        }

        clearObservable.subscribe {params ->
            resetToggles()
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

    fun resetToggles() {
        filterToggles.isVipAccess = null
        filterToggles.hotelStarRating = StarRatings()
        filterToggles.name = null
        filterToggles.price = null
        filterToggles.neighborhoods = null
    }

    fun processFilters(hotel : Hotel) {
        if (filterIsVipAccess(hotel) && filterHotelStarRating(hotel) && filterName(hotel) && filterPrice(hotel)) {
            filteredResponse.hotelList.add(hotel)
        }
    }

    fun filterIsVipAccess(hotel : Hotel) : Boolean {
        if (filterToggles.isVipAccess == null) return true
        return filterToggles.isVipAccess == hotel.isVipAccess
    }

    fun filterHotelStarRating(hotel: Hotel) : Boolean {
        if (!filterToggles.hotelStarRating.one &&
                !filterToggles.hotelStarRating.two &&
                !filterToggles.hotelStarRating.three &&
                !filterToggles.hotelStarRating.four && !filterToggles.hotelStarRating.five) return true

        return (1.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && filterToggles.hotelStarRating.one) ||
                (2.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && filterToggles.hotelStarRating.two) ||
                (3.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && filterToggles.hotelStarRating.three) ||
                (4.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && filterToggles.hotelStarRating.four) ||
                (5.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && filterToggles.hotelStarRating.five)
    }

    fun filterName(hotel: Hotel) : Boolean {
        if (filterToggles.name.isNullOrEmpty()) return true
        var namePattern: Pattern? = null
        if (filterToggles.name != null) {
            namePattern = Pattern.compile(".*" + filterToggles.name + ".*", Pattern.CASE_INSENSITIVE)
        }
        return namePattern == null || namePattern.matcher(hotel.localizedName).find()
    }

    fun filterPrice(hotel: Hotel) : Boolean {
        if (filterToggles.price == null) return true
        return filterToggles.price == hotel.lowRateInfo.priceToShowUsers
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver {
        if (it) {
            filterToggles.isVipAccess = true
        } else {
            filterToggles.isVipAccess = false
        }

        handleFiltering()
    }

    val oneStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!filterToggles.hotelStarRating.one) {
            filterToggles.hotelStarRating.one = true
            hotelStarRatingBar.onNext(1)
        } else {
            filterToggles.hotelStarRating.one = false
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!filterToggles.hotelStarRating.two) {
            filterToggles.hotelStarRating.two = true
            hotelStarRatingBar.onNext(2)
        } else {
            filterToggles.hotelStarRating.two = false
            hotelStarRatingBar.onNext(7)
        }

        handleFiltering()
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!filterToggles.hotelStarRating.three) {
            filterToggles.hotelStarRating.three = true
            hotelStarRatingBar.onNext(3)
        } else {
            filterToggles.hotelStarRating.three = false
            hotelStarRatingBar.onNext(8)
        }

        handleFiltering()
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!filterToggles.hotelStarRating.four) {
            filterToggles.hotelStarRating.four = true
            hotelStarRatingBar.onNext(4)
        } else {
            filterToggles.hotelStarRating.four = false
            hotelStarRatingBar.onNext(9)
        }

        handleFiltering()
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!filterToggles.hotelStarRating.five) {
            filterToggles.hotelStarRating.five = true
            hotelStarRatingBar.onNext(5)
        } else {
            filterToggles.hotelStarRating.five = false
            hotelStarRatingBar.onNext(10)
        }

        handleFiltering()
    }

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        filterToggles.name = s.toString()
        handleFiltering()
    }

    fun setHotelList(response : HotelSearchResponse) {
        originalResponse = response
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
            Sort.RATING -> Collections.sort(preSortHotelList, rating_comparator)
            Sort.DEALS -> Collections.sort(preSortHotelList, deals_comparator)
            Sort.DISTANCE -> Collections.sort(preSortHotelList,distance_comparator)
            else -> Collections.sort(preSortHotelList, popular_comparator)

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

            if (deal1.discountPercent == deal2.discountPercent) {
                return 0
            } else if (deal1.discountPercent < deal2.discountPercent) {
                // We want to show larger percentage discounts first
                return -1
            } else {
                return 1
            }
        }
    }

    private val rating_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val rating1 = hotel1.hotelGuestRating
            val rating2 = hotel2.hotelGuestRating

            if (rating1 == rating2) {
                return price_comparator.compare(hotel1, hotel2)
            } else if (rating1 > rating2) {
                return -1
            } else {
                return 1
            }
        }
    }

    private val distance_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
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

    private val popular_comparator: Comparator<Hotel> = object : Comparator<Hotel> {
        override fun compare(hotel1: Hotel, hotel2: Hotel): Int {
            val index1 = hotel1.sortIndex
            val index2 = hotel2.sortIndex

            if (index1.toInt() < index2.toInt()) {
                return -1
            } else {
                return 1
            }
        }
    }

}


