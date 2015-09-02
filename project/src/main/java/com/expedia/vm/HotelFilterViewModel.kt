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

    data class FilterToggles(var isVipAccess : Boolean? = null, var hotelStarRating : Float? = null, var name : String? = null, var price : Float? = null, var neighborhoods : List<String>? = null)

    val filterToggles = FilterToggles(null, null, null, null, null)

    init {
        doneObservable.subscribe { params ->
            filteredResponse.hotelList = ArrayList<Hotel>()
            filteredResponse.allNeighborhoodsInSearchRegion = originalResponse?.allNeighborhoodsInSearchRegion
            filterObservable.onNext(filteredResponse.hotelList)
        }

        clearObservable.subscribe {params ->
            resetToggles()
            handleFiltering()
            finishClear.onNext(Unit)
        }
    }

    fun handleFiltering() {
        filteredResponse.hotelList = ArrayList<Hotel>()
        filteredResponse.hotelList.add(0, Hotel())
        filteredResponse.allNeighborhoodsInSearchRegion = originalResponse?.allNeighborhoodsInSearchRegion

        for (hotel in originalResponse?.hotelList.orEmpty()) {
            processFilters(hotel)
        }

        val size = filteredResponse.hotelList.size()-1
        updateDynamicFeedbackWidget.onNext(size)
    }

    fun resetToggles() {
        filterToggles.isVipAccess = null
        filterToggles.hotelStarRating = null
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
        if (filterToggles.hotelStarRating == null) return true
        return filterToggles.hotelStarRating == hotel.hotelStarRating
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
        if (filterToggles.hotelStarRating != 1.0f) {
            filterToggles.hotelStarRating = 1.0f
            hotelStarRatingBar.onNext(1)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 2.0f) {
            filterToggles.hotelStarRating = 2.0f
            hotelStarRatingBar.onNext(2)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 3.0f) {
            filterToggles.hotelStarRating = 3.0f
            hotelStarRatingBar.onNext(3)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 4.0f) {
            filterToggles.hotelStarRating = 4.0f
            hotelStarRatingBar.onNext(4)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 5.0f) {
            filterToggles.hotelStarRating = 5.0f
            hotelStarRatingBar.onNext(5)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        filterToggles.name = s.toString()
        handleFiltering()
    }

    fun setHotelList(response : HotelSearchResponse) {
        response.hotelList.remove(0)
        originalResponse = response
    }
}
