package com.expedia.vm


import android.content.Context
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices?) {

    val applyObservable = PublishSubject.create<String>()
    val couponObservable = PublishSubject.create<HotelCreateTripResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val couponParamsObservable = BehaviorSubject.create<HotelApplyCouponParams>()

    init {
        couponParamsObservable.subscribe { params ->
            applyObservable.onNext(params.tripId)
            hotelServices?.applyCoupon(params, getCouponObserver())
        }
    }

    fun getCouponObserver(): Observer<HotelCreateTripResponse> {
        return object: Observer<HotelCreateTripResponse> {
            override fun onNext(trip: HotelCreateTripResponse) {
                if (trip.hasErrors()) {
                    errorObservable.onNext(trip.getFirstError())
                } else {
                    couponObservable.onNext(trip)
                }
            }

            override fun onError(e: Throwable?) {
                // FIXME
            }

            override fun onCompleted() {
                // ignore
            }
        }

    }
}
