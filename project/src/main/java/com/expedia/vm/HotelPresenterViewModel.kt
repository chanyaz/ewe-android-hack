package com.expedia.vm

import com.expedia.bookings.data.cars.ApiError
import rx.Observable
import rx.subjects.BehaviorSubject

class HotelPresenterViewModel(createTripViewModel: HotelCreateTripViewModel, checkoutViewModel: HotelCheckoutViewModel, detailViewModel: HotelDetailViewModel) {

    val didLastCreateTripOrCheckoutResultInRoomSoldOut = BehaviorSubject.create<Boolean>(false)

    val selectedRoomSoldOut = Observable.merge(createTripViewModel.errorObservable, checkoutViewModel.errorObservable)
            .filter { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE }
            .map { Unit }

    val hotelSoldOutWithHotelId = Observable.switchOnNext(detailViewModel.hotelOffersSubject.map
    { hotel ->
        detailViewModel.hotelSoldOut.filter { it == true }.map {
            hotel.hotelId
        }
    })

    init {
        Observable.merge(createTripViewModel.errorObservable
                .map { if (it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) true else false },
                createTripViewModel.tripResponseObservable.map { false },
                createTripViewModel.noResponseObservable.map { false },
                checkoutViewModel.errorObservable.map { if (it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) true else false },
                checkoutViewModel.priceChangeResponseObservable.map { false },
                checkoutViewModel.checkoutResponseObservable.map { false },
                checkoutViewModel.noResponseObservable.map { false }).subscribe(didLastCreateTripOrCheckoutResultInRoomSoldOut)
    }
}