package com.expedia.vm

import com.expedia.bookings.data.ApiError
import com.expedia.vm.hotel.HotelDetailViewModel
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class HotelPresenterViewModel(createTripViewModel: HotelCreateTripViewModel, checkoutViewModel: HotelCheckoutViewModel?, detailViewModel: BaseHotelDetailViewModel) {

    val didLastCreateTripOrCheckoutResultInRoomSoldOut = BehaviorSubject.create<Boolean>(false)

    val selectedRoomSoldOut = Observable.merge(createTripViewModel.errorObservable, checkoutViewModel?.errorObservable)
            .filter { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE }
            .map { Unit }

    val hotelSoldOutWithHotelId = Observable.switchOnNext(detailViewModel.hotelOffersSubject.map
    { hotel ->
        detailViewModel.hotelSoldOut.filter { it == true }.delay(100L, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).map {
            hotel.hotelId
        }
    })

    init {
        Observable.merge(createTripViewModel.errorObservable
                .map { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE },
                createTripViewModel.tripResponseObservable.map { false },
                createTripViewModel.noResponseObservable.map { false },
                checkoutViewModel?.errorObservable?.map { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE },
                checkoutViewModel?.priceChangeResponseObservable?.map { false },
                checkoutViewModel?.checkoutResponseObservable?.map { false },
                checkoutViewModel?.noResponseObservable?.map { false }).subscribe(didLastCreateTripOrCheckoutResultInRoomSoldOut)
    }
}