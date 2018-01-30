package com.expedia.vm

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.hotels.HotelApplySavedCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.isHotelMaterialForms
import com.expedia.bookings.utils.isShowSavedCoupons
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    val errorShowDialogObservable = PublishSubject.create<ApiError>()
    val removeObservable = BehaviorSubject.createDefault<Boolean>(false)
    val errorRemoveCouponShowDialogObservable = PublishSubject.create<ApiError>()
    val discountObservable = PublishSubject.create<String>()
    val couponSubtitleObservable = PublishSubject.create<String>()
    val removeCouponSuccessObservable = PublishSubject.create<HotelCreateTripResponse>()

    val couponRemoveObservable = PublishSubject.create<String>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>()
    val enableSubmitButtonObservable = PublishSubject.create<Boolean>()
    val onMenuClickedMethod = PublishSubject.create<() -> Unit>()
    val storedCouponWidgetVisibilityObservable = PublishSubject.create<Boolean>()
    val expandedObservable = PublishSubject.create<Boolean>()
    val hasStoredCoupons = PublishSubject.create<Boolean>()
    val onCouponWidgetExpandSubject = PublishSubject.create<Boolean>()
    val networkErrorAlertDialogObservable = PublishSubject.create<Unit>()

    val applyCouponViewModel by lazy {
        val viewModel = ApplyCouponViewModel(context, hotelServices, paymentModel, couponErrorMap)
        viewModel.hasDiscountObservable.subscribe(hasDiscountObservable)
        viewModel.errorShowDialogObservable.subscribe(errorShowDialogObservable)
        viewModel.networkErrorAlertDialogObservable.subscribe(networkErrorAlertDialogObservable)
        viewModel.applyCouponSuccessObservable.subscribe { tripResponse ->
            couponChangeSuccess(tripResponse)
            //TODO: Handle Coupon Tracking for failure
//            HotelTracking.trackHotelCouponSuccess(couponParams.getTrackingString())
        }
        viewModel
    }

    val storedCouponViewModel by lazy {
        val viewModel = StoredCouponViewModel(context, hotelServices, paymentModel, couponErrorMap)
        viewModel.hasDiscountObservable.subscribe(hasDiscountObservable)
        viewModel.errorShowDialogObservable.subscribe(errorShowDialogObservable)
        viewModel.networkErrorAlertDialogObservable.subscribe(networkErrorAlertDialogObservable)
        viewModel.storedCouponSuccessObservable.subscribe { tripResponse ->
            couponChangeSuccess(tripResponse)
            //TODO: Handle Coupon Tracking for success
//            HotelTracking.trackHotelCouponSuccess(couponParams.getTrackingString())
        }
        viewModel
    }

    init {
        couponRemoveObservable.subscribe { tripId ->
            removeObservable.onNext(false)
            val observable = hotelServices.removeCoupon(tripId, PointOfSale.getPointOfSale().isPwPEnabledForHotels)
            observable.subscribe { trip ->
                if (trip.hasErrors()) {
                    errorRemoveCouponShowDialogObservable.onNext(trip.firstError)
                    // TODO Add omniture tracking for coupon removal failure
//                    HotelTracking.trackHotelCouponRemoveFailure(applyStoredCouponObservable.value.name, trip.firstError.errorInfo.couponErrorType)
                } else {
                    couponChangeSuccess(trip)
                    removeCouponSuccessObservable.onNext(trip)
                    // TODO Add omniture tracking for coupon removal success
                }
            }
        }

        ObservableOld.combineLatest(hasStoredCoupons, expandedObservable, { hasStoredCoupon, expanded ->
            storedCouponWidgetVisibilityObservable.onNext(hasStoredCoupon && expanded && isShowSavedCoupons(context))
        }).subscribe()
    }

    private fun couponChangeSuccess(trip: HotelCreateTripResponse) {
        val couponRate = trip.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getPriceAdjustments()
        val hasDiscount = couponRate != null && !couponRate.isZero
        if (hasDiscount) {
            setCouponAppliedSubtitle(trip, couponRate)
        }
        hasDiscountObservable.onNext(hasDiscount)
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))
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
        storedCouponViewModel.storedCouponActionParam.onNext(couponParams)
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
