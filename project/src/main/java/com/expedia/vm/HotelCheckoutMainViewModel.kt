package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.enums.MerchandiseSpam
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelCheckoutMainViewModel(paymentModel: PaymentModel<HotelCreateTripResponse>, shopWithPointsViewModel: ShopWithPointsViewModel) {
    val animateInSlideToPurchaseSubject = PublishSubject.create<Unit>()
    val onLogoutButtonClicked = PublishSubject.create<Unit>()
    val userWithEffectiveSwPAvailableSignedOut = BehaviorSubject.create<Boolean>(false)
    val emailOptInStatus = PublishSubject.create<MerchandiseSpam>()

    //OUTLETS
    val updateEarnedRewards: Observable<PointsAndCurrency> = paymentModel.paymentSplits.map { it.payingWithCards }

    val animateSlideToPurchaseWithPaymentSplits = animateInSlideToPurchaseSubject
            .withLatestFrom(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse, { Unit, paymentSplitsAndLatestTrip ->
                if (paymentSplitsAndLatestTrip.tripResponse.isRewardsRedeemable()) paymentSplitsAndLatestTrip.paymentSplits.paymentSplitsType()
                else PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
            })

    init {
        /* To check whether user is logged in and has opted to shopWithPoints, we want last/stale value in shopWithPointsViewModel.swpEffectiveAvailability
        which has not been updated by the time onLogoutButtonClicked triggers */

        onLogoutButtonClicked.withLatestFrom(shopWithPointsViewModel.swpEffectiveAvailability, { Unit, isAvailable -> isAvailable }).subscribe(userWithEffectiveSwPAvailableSignedOut)
    }
}