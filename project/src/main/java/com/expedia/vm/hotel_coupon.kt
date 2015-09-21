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
                val errorType = trip.firstError.errorInfo.couponErrorType
                val stringId = couponErrorMap.get(errorType) ?: R.string.coupon_error_fallback
                val text = context.resources.getString(stringId)

                errorMessageObservable.onNext(text)
                errorObservable.onNext(trip.firstError)
            } else {
                couponObservable.onNext(trip)
            }
        })
    }

    private val couponErrorMap: Map<String, Int> = mapOf(
            "Duplicate" to R.string.coupon_error_duplicate,
            "Expired" to R.string.coupon_error_expired,
            "FallBack" to R.string.coupon_error_fallback,
            "HotelExcluded" to R.string.coupon_error_hotel_excluded,
            "InvalidHotel" to R.string.coupon_error_invalid_hotel,
            "InvalidProduct" to R.string.coupon_error_invalid_product,
            "InvalidRegion" to R.string.coupon_error_invalid_region,
            "InvalidTravelDates" to R.string.coupon_error_invalid_travel_dates,
            "MinPurchaseAmountNotMet" to R.string.coupon_error_min_purchase_amount_not_met,
            "MinStayNotMet" to R.string.coupon_error_min_stay_not_met,
            "NotRedeemed" to R.string.coupon_error_not_redeemed,
            "PriceChange" to R.string.coupon_error_price_change,
            "ServiceDown" to R.string.coupon_error_service_down,
            "Unrecognized" to R.string.coupon_error_unrecognized,
            "InvalidAveragePrice" to R.string.coupon_error_invalid_average_price,
            "InvalidStayDates" to R.string.coupon_error_invalid_stay_dates,
            "ExceededEarnLimit" to R.string.coupon_error_exceeded_earn_limit,
            "NotActive" to R.string.coupon_error_not_active,
            "DoesNotExist" to R.string.coupon_error_unknown,
            "CampaignIsNotConfigured" to R.string.coupon_error_unknown,
            "PackageProductMissing" to R.string.coupon_error_invalid_booking
    )
}
