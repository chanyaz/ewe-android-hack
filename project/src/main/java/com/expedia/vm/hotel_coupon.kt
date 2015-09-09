package com.expedia.vm


import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.util.endlessObserver
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashMap

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices) {

    val applyObservable = PublishSubject.create<String>()
    val couponObservable = PublishSubject.create<HotelCreateTripResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val errorMessageObservable = PublishSubject.create<String>()
    val couponParamsObservable = BehaviorSubject.create<HotelApplyCouponParams>()

    private val createTripDownloadsObservable = PublishSubject.create<Observable<HotelCreateTripResponse>>()
    private val createTripObservable = Observable.concat(createTripDownloadsObservable)

    init {
        couponParamsObservable.subscribe { params ->
            applyObservable.onNext(params.tripId)
            val observable = hotelServices.applyCoupon(params)
            createTripDownloadsObservable.onNext(observable)
        }
        createTripObservable.subscribe(endlessObserver { trip ->
            if (trip.hasErrors()) {
                val error = trip.getFirstError()
                val text = context.getResources().getString(couponErrorMap.get(error.errorInfo.couponErrorType))
                errorMessageObservable.onNext(text)
                errorObservable.onNext(error)
            } else {
                couponObservable.onNext(trip)
            }
        })
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
            put("NotActive", R.string.coupon_error_not_active)
            put("DoesNotExist", R.string.coupon_error_unknown)
            put("CampaignIsNotConfigured", R.string.coupon_error_unknown)
            put("PackageProductMissing", R.string.coupon_error_invalid_booking)
        }
    }

}
