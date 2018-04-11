package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.server.EndpointProvider
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class HotelWebCheckoutViewViewModel @Inject constructor(var context: Context, val endpointProvider: EndpointProvider) : WebCheckoutViewViewModel(context) {

    var hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    var offerObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val fireCreateTripObservable = PublishSubject.create<Unit>()

    var createTripViewModel by notNullAndObservable<HotelCreateTripViewModel> {
        it.tripResponseObservable.subscribe { createTripResponse ->
            webViewURLObservable.onNext( endpointProvider.getE3EndpointUrlWithPath("HotelCheckout?tripid=${createTripResponse.tripId}"))
        }
    }

    init {
        fireCreateTripObservable.subscribe { doCreateTrip() }
        offerObservable.map { Unit }.subscribe(fireCreateTripObservable)
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        val numberOfAdults = hotelSearchParamsObservable.value.adults
        val childAges = hotelSearchParamsObservable.value.children
        val qualifyAirAttach = false
        createTripViewModel.tripParams.onNext(HotelCreateTripParams(offerObservable.value.productKey, qualifyAirAttach, numberOfAdults, childAges))
    }
}
