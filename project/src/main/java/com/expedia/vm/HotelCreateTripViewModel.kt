package com.expedia.vm

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class HotelCreateTripViewModel(val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>?) {

    // input
    val tripParams = PublishSubject.create<HotelCreateTripParams>()

    // output
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        tripParams.subscribe { params ->
            hotelServices.createTrip(params, PointOfSale.getPointOfSale().isPwPEnabledForHotels,  getCreateTripResponseObserver())
        }
    }

    open fun getCreateTripResponseObserver(): Observer<HotelCreateTripResponse> {
        return object : Observer<HotelCreateTripResponse> {
            override fun onNext(t: HotelCreateTripResponse) {
                if (t.hasErrors()) {
                    if (t.firstError.errorInfo.field == "productKey") {
                        errorObservable.onNext(ApiError(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY))
                    } else if (t.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                        errorObservable.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
                    } else {
                        errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                } else {
                    // TODO: Move away from using DB. observers should react on fresh createTrip response
                    Db.getTripBucket().clearHotelV2()
                    Db.getTripBucket().add(TripBucketItemHotelV2(t))
                    // TODO: populate hotelCreateTripResponseData with response data
                    tripResponseObservable.onNext(t)
                    paymentModel?.createTripSubject?.onNext(t)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    noResponseObservable.onNext(Unit)
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}