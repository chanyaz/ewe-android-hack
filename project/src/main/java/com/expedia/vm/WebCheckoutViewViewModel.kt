package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.util.notNullAndObservable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class WebCheckoutViewViewModel(val context: Context) : WebViewViewModel(), UserAccountRefresher.IUserAccountRefreshListener  {
    // var so that we can mock it for unit testing
    var userAccountRefresher: UserAccountRefresher = UserAccountRefresher(context, LineOfBusiness.PROFILE, this)

    val bookedTripIDObservable = BehaviorSubject.create<String>()
    val fetchItinObservable = PublishSubject.create<String>()
    val closeView = PublishSubject.create<Unit>()
    var offerObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    var hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    val fireCreateTripObservable = PublishSubject.create<Unit>()
    var createTripViewModel by notNullAndObservable<HotelCreateTripViewModel> {
        it.tripResponseObservable.subscribe { createTripResponse ->
            webViewURLObservable.onNext("${PointOfSale.getPointOfSale().hotelsWebCheckoutURL}?tripid=${createTripResponse.tripId}")
        }
    }

    init {
        offerObservable.map { Unit }.subscribe(fireCreateTripObservable)
        fireCreateTripObservable.subscribe { doCreateTrip() }
    }

    fun doCreateTrip() {
        val numberOfAdults = hotelSearchParamsObservable.value.adults
        val childAges = hotelSearchParamsObservable.value.children
        val qualifyAirAttach = false
        createTripViewModel.tripParams.onNext(HotelCreateTripParams(offerObservable.value.productKey, qualifyAirAttach, numberOfAdults, childAges))
    }

    override fun onUserAccountRefreshed() {
        User.addUserToAccountManager(context, Db.getUser())
        val bookTripId = bookedTripIDObservable.value
        if (Strings.isNotEmpty(bookTripId)) {
            fetchItinObservable.onNext(bookTripId)
        } else {
            closeView.onNext(Unit)
        }
    }

}
