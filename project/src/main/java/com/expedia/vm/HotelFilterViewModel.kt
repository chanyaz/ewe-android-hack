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
}
