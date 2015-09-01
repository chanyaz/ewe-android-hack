package com.expedia.vm


import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashMap

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices) {

    val applyObservable = PublishSubject.create<String>()
    val couponObservable = PublishSubject.create<HotelCreateTripResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val errorMessageObservable = PublishSubject.create<String>()
    val couponParamsObservable = BehaviorSubject.create<HotelApplyCouponParams>()

    init {
        couponParamsObservable.subscribe { params ->
            applyObservable.onNext(params.tripId)
            hotelServices.applyCoupon(params, getCouponObserver())
        }
    }

    fun getCouponObserver(): Observer<HotelCreateTripResponse> {
        return object: Observer<HotelCreateTripResponse> {
            override fun onNext(trip: HotelCreateTripResponse) {
                if (trip.hasErrors()) {
                    val error = trip.getFirstError()
                    val text = context.getResources().getString(couponErrorMap.get(error.errorInfo.couponErrorType))
                    errorMessageObservable.onNext(text)
                    errorObservable.onNext(error)
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

    private val couponErrorMap = object : HashMap<String, Int>() {
        init {
            put("Duplicate", R.string.coupon_error_duplicate)
            put("Expired", R.string.coupon_error_expired)
            put("FallBack", R.string.coupon_error_fallback)
            put("HotelExcluded", R.string.coupon_error_hotel_excluded)
            put("InvalidHotel", R.string.coupon_error_invalid_hotel)
            put("InvalidProduct", R.string.coupon_error_invalid_product)
            put("InvalidRegion", R.string.coupon_error_invalid_region)
            put("InvalidTravelDates", R.string.coupon_error_invalid_travel_dates)
            put("MinPurchaseAmountNotMet", R.string.coupon_error_min_purchase_amount_not_met)
            put("MinStayNotMet", R.string.coupon_error_min_stay_not_met)
            put("NotRedeemed", R.string.coupon_error_not_redeemed)
            put("PriceChange", R.string.coupon_error_price_change)
            put("ServiceDown", R.string.coupon_error_service_down)
            put("Unrecognized", R.string.coupon_error_unrecognized)
            put("InvalidAveragePrice", R.string.coupon_error_invalid_average_price)
            put("InvalidStayDates", R.string.coupon_error_invalid_stay_dates)
            put("ExceededEarnLimit", R.string.coupon_error_exceeded_earn_limit)
        }
    }

}
