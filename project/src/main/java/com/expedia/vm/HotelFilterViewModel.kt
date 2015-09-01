package com.expedia.vm

import android.content.Context
import android.text.TextUtils
//import android.view.View
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
    // Click
    val doneObservable = PublishSubject.create<Unit>()

    // Output
    val filterObservable = PublishSubject.create<List<Hotel>>()

    var originalResponse : HotelSearchResponse? = null
    var filteredResponse : HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()

    class FilterToggles(isVipAccess : Boolean?, hotelStarRating : Float?, name : String?, price : Float?, neighborhoods : List<String>?) {
        var isVipAccess = isVipAccess
        var hotelStarRating = hotelStarRating
        var name = name
        var price = price
        var neighborhoods = neighborhoods
    }

    val filterToggles = FilterToggles(null, null, null, null, null)

    init {
        doneObservable.subscribe { params ->
            filteredResponse.hotelList = ArrayList<Hotel>()
            filteredResponse.allNeighborhoodsInSearchRegion = originalResponse?.allNeighborhoodsInSearchRegion

            for (hotel in originalResponse?.hotelList.orEmpty()) {
                processFilters(hotel)
            }

            // Passes the new list to the results
            filterObservable.onNext(filteredResponse.hotelList)
        }
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
        return namePattern == null || namePattern.matcher(hotel.name).find()
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
    }

    val oneStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 1.0f) {
            filterToggles.hotelStarRating = 1.0f
            hotelStarRatingBar.onNext(1)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 2.0f) {
            filterToggles.hotelStarRating = 2.0f
            hotelStarRatingBar.onNext(2)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 3.0f) {
            filterToggles.hotelStarRating = 3.0f
            hotelStarRatingBar.onNext(3)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 4.0f) {
            filterToggles.hotelStarRating = 4.0f
            hotelStarRatingBar.onNext(4)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (filterToggles.hotelStarRating != 5.0f) {
            filterToggles.hotelStarRating = 5.0f
            hotelStarRatingBar.onNext(5)
        } else {
            filterToggles.hotelStarRating = null
            hotelStarRatingBar.onNext(6)
        }
    }

    fun setHotelList(response : HotelSearchResponse) {
        response.hotelList.remove(0)
        originalResponse = response
    }

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        filterToggles.name = s.toString()
    }

}
