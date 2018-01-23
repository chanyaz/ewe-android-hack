package com.expedia.vm

import com.expedia.bookings.data.ApiError
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class HotelPresenterViewModel(createTripViewModel: HotelCreateTripViewModel, checkoutViewModel: HotelCheckoutViewModel?, detailViewModel: BaseHotelDetailViewModel) {

    val didLastCreateTripOrCheckoutResultInRoomSoldOut = BehaviorSubject.createDefault<Boolean>(false)

    val selectedRoomSoldOut: Observable<Unit> = {
        val selectedRoomSoldOut =
                if (checkoutViewModel?.errorObservable != null) {
                    Observable.merge(createTripViewModel.errorObservable, checkoutViewModel.errorObservable)
                } else {
                    createTripViewModel.errorObservable
                }

        selectedRoomSoldOut
                .filter { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE }
                .map { Unit }
    }()

    val hotelSoldOutWithHotelId = Observable.switchOnNext(detailViewModel.hotelOffersSubject.map
    { hotel ->
        detailViewModel.hotelSoldOut.filter { it == true }.delay(100L, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).map {
            hotel.hotelId
        }
    })

    init {
        if (checkoutViewModel != null) {
            Observable.mergeArray(createTripViewModel.errorObservable
                    .map { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE },
                    createTripViewModel.tripResponseObservable.map { false },
                    createTripViewModel.noResponseObservable.map { false },
                    checkoutViewModel.errorObservable.map { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE },
                    checkoutViewModel.priceChangeResponseObservable.map { false },
                    checkoutViewModel.checkoutResponseObservable.map { false },
                    checkoutViewModel.noResponseObservable.map { false }).subscribe(didLastCreateTripOrCheckoutResultInRoomSoldOut)
        } else {
            Observable.mergeArray(createTripViewModel.errorObservable
                    .map { it.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE },
                    createTripViewModel.tripResponseObservable.map { false },
                    createTripViewModel.noResponseObservable.map { false }).subscribe(didLastCreateTripOrCheckoutResultInRoomSoldOut)
        }
    }
}
