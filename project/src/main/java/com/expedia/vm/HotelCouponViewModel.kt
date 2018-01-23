package com.expedia.vm

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.hotels.AbstractApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelApplySavedCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.isHotelMaterialForms
import com.expedia.bookings.utils.isShowSavedCoupons
import com.squareup.phrase.Phrase
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    val applyObservable = PublishSubject.create<String>()
    val removeObservable = BehaviorSubject.createDefault<Boolean>(false)
    val couponObservable = PublishSubject.create<HotelCreateTripResponse>()
    val storedCouponSuccessObservable = PublishSubject.create<HotelCreateTripResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val errorShowDialogObservable = PublishSubject.create<ApiError>()
    val errorRemoveCouponShowDialogObservable = PublishSubject.create<ApiError>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val discountObservable = PublishSubject.create<String>()
    val couponSubtitleObservable = PublishSubject.create<String>()
    val couponParamsObservable = BehaviorSubject.create<AbstractApplyCouponParameters>()
    val couponRemoveObservable = PublishSubject.create<String>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>()
    val enableSubmitButtonObservable = PublishSubject.create<Boolean>()
    val onMenuClickedMethod = PublishSubject.create<() -> Unit>()
    val storedCouponWidgetVisibilityObservable = PublishSubject.create<Boolean>()
    val expandedObservable = PublishSubject.create<Boolean>()
    val hasStoredCoupons = PublishSubject.create<Boolean>()
    val onCouponWidgetExpandSubject = PublishSubject.create<Boolean>()
    val storedCouponApplyObservable = PublishSubject.create<String>()
    val onCouponSubmitClicked = PublishSubject.create<Unit>()

    val createTripDownloadsObservable = PublishSubject.create<Observable<HotelCreateTripResponse>>()
    private val createTripObservable = Observable.concat(createTripDownloadsObservable)
    val networkErrorAlertDialogObservable = PublishSubject.create<Unit>()

    init {
        couponParamsObservable.subscribe { params ->
            if (params !is HotelApplySavedCodeParameters) {
                applyObservable.onNext(params.tripId)
            }
            val observable = hotelServices.applyCoupon(params, PointOfSale.getPointOfSale().isPwPEnabledForHotels)
            createTripDownloadsObservable.onNext(observable)
        }

        couponRemoveObservable.subscribe { tripId ->
            removeObservable.onNext(false)
            val observable = hotelServices.removeCoupon(tripId, PointOfSale.getPointOfSale().isPwPEnabledForHotels)
            observable.subscribe { trip ->
                if (trip.hasErrors()) {
                    errorRemoveCouponShowDialogObservable.onNext(trip.firstError)
                } else {
                    couponChangeSuccess(trip)
                    // TODO Add omniture tracking for coupon removal
                }
            }
        }

        createTripObservable.subscribe(couponEndlessObserver { trip ->
            enableSubmitButtonObservable.onNext(true)
            val couponParams = couponParamsObservable.value
            if (trip.hasErrors()) {
                val errorType = trip.firstError.errorInfo.couponErrorType
                val stringId = couponErrorMap[errorType] ?: R.string.coupon_error_fallback
                val text = context.resources.getString(stringId)
                hasDiscountObservable.onNext(false)
                errorMessageObservable.onNext(text)
                errorObservable.onNext(trip.firstError)
                if (couponParams.isFromNotSignedInToSignedIn) {
                    errorShowDialogObservable.onNext(trip.firstError)
                }
                HotelTracking.trackHotelCouponFail(couponParams.getTrackingString(), errorType)
            } else {
                if (couponParams is HotelApplySavedCodeParameters) {
                    couponChangeSuccess(trip, true)
                } else {
                    couponChangeSuccess(trip)
                }
                HotelTracking.trackHotelCouponSuccess(couponParams.getTrackingString())
            }
        })

        ObservableOld.combineLatest(hasStoredCoupons, expandedObservable, { hasStoredCoupon, expanded ->
            storedCouponWidgetVisibilityObservable.onNext(hasStoredCoupon && expanded && isShowSavedCoupons(context))
        }).subscribe()
    }

    private fun couponChangeSuccess(trip: HotelCreateTripResponse, responseFromStoredCouponApply: Boolean = false) {
        val couponRate = trip.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getPriceAdjustments()
        val hasDiscount = couponRate != null && !couponRate.isZero
        if (hasDiscount) {
            setCouponAppliedSubtitle(trip, couponRate)
        }
        hasDiscountObservable.onNext(hasDiscount)
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))

        if (!responseFromStoredCouponApply) {
            couponObservable.onNext(trip)
        } else {
            storedCouponSuccessObservable.onNext(trip)
        }
        paymentModel.couponChangeSubject.onNext(trip)
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

    fun <T> couponEndlessObserver(body: (T) -> Unit): Observer<T> {
        return object : DisposableObserver<T>() {
            override fun onNext(t: T) {
                body(t)
            }

            override fun onComplete() {
                throw OnErrorNotImplementedException(RuntimeException("Cannot call completed on endless observer " + body.javaClass))
            }

            override fun onError(e: Throwable) {
                raiseAlertDialog(e)
            }
        }
    }

    fun raiseAlertDialog(e: Throwable) {
        if (RetrofitUtils.isNetworkError(e)) {
            networkErrorAlertDialogObservable.onNext(Unit)
        }
    }

    fun submitStoredCoupon(paymentSplits: PaymentSplits, tripResponse: TripResponse, userStateManager: UserStateManager, couponInstanceId: String) {
        var userPointsPreference: List<UserPreferencePointsDetails> = emptyList()
        if (userStateManager.isUserAuthenticated() && tripResponse.isRewardsRedeemable()) {
            val payingWithPointsSplit = paymentSplits.payingWithPoints
            userPointsPreference = listOf(UserPreferencePointsDetails(tripResponse.getProgramName()!!, payingWithPointsSplit))
        }

        //TODO: Think about not signed in to signed in
        val couponParams = HotelApplySavedCodeParameters.Builder()
                .tripId(Db.getTripBucket().hotelV2.mHotelTripResponse.tripId)
                .instanceId(couponInstanceId)
                .isFromNotSignedInToSignedIn(false)
                .userPreferencePointsDetails(userPointsPreference)
                .build()
        couponParamsObservable.onNext(couponParams)
    }

    private fun setCouponAppliedSubtitle(trip: HotelCreateTripResponse, couponRate: Money) {
        if (isHotelMaterialForms(context)) {
            val couponName = trip.userCoupons.find { it.instanceId == trip.coupon.instanceId }?.name ?: ""
            val subtitle = Phrase.from(context, R.string.material_applied_coupon_subtitle_TEMPLATE)
                    .put("name", couponName)
                    .put("discount", couponRate.formattedMoney).format().toString()
            couponSubtitleObservable.onNext(subtitle)
        } else {
            discountObservable.onNext(couponRate.formattedMoney)
        }
    }
}
