package com.expedia.vm

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.presenter.shared.StoredCouponAdapter
import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class HotelCreateTripViewModel(val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>?) {

    // input
    val tripParams = PublishSubject.create<HotelCreateTripParams>()

    // output
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()

    init {
        tripParams.subscribe { params ->
            hotelServices.createTrip(params, PointOfSale.getPointOfSale().isPwPEnabledForHotels, getCreateTripResponseObserver())
        }
    }

    open fun getCreateTripResponseObserver(): Observer<HotelCreateTripResponse> {
        return object : Observer<HotelCreateTripResponse> {
            override fun onSubscribe(d: Disposable) {
                //ignore
            }

            override fun onNext(response: HotelCreateTripResponse) {
                if (response.hasErrors()) {
                    if (response.firstError.errorInfo.field == "productKey") {
                        errorObservable.onNext(ApiError(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY))
                    } else if (response.firstError.getErrorCode() == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                        errorObservable.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
                    } else {
                        errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    }
                } else {
                    // TODO: Move away from using DB. observers should react on fresh createTrip response
                    Db.getTripBucket().clearHotelV2()
                    Db.getTripBucket().add(TripBucketItemHotelV2(response))
                    // TODO: populate hotelCreateTripResponseData with response data
                    tripResponseObservable.onNext(response)
                    paymentModel?.createTripSubject?.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    noResponseObservable.onNext(Unit)
                }
            }

            override fun onComplete() {
                // ignore
            }
        }
    }

    fun convertUserCouponToStoredCouponAdapter(userCoupons: List<HotelCreateTripResponse.SavedCoupon>?): List<StoredCouponAdapter> {
        val appliedCouponInstanceId = tripResponseObservable.value.coupon?.instanceId
        if (appliedCouponInstanceId == null) {
            return userCoupons?.map { it -> StoredCouponAdapter(it, StoredCouponAppliedStatus.DEFAULT) } ?: emptyList()
        } else {
            return userCoupons?.map { it -> StoredCouponAdapter(it,
                    if (appliedCouponInstanceId == it.instanceId) StoredCouponAppliedStatus.SUCCESS else StoredCouponAppliedStatus.DEFAULT)
            } ?: emptyList()
        }
    }
}
