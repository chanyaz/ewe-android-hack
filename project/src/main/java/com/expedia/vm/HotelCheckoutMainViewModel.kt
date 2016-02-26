package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import rx.Observable
import rx.subjects.PublishSubject

class HotelCheckoutMainViewModel(paymentModel: PaymentModel<HotelCreateTripResponse>) {
    val animateInSlideToPurchaseSubject = PublishSubject.create<Unit>()

    //OUTLETS
    val updateEarnedRewards: Observable<Int> = paymentModel.paymentSplits.map { it.payingWithCards.points }

    val animateSlideToPurchaseWithPaymentSplits = animateInSlideToPurchaseSubject
            .withLatestFrom(paymentModel.paymentSplitsWithLatestTripResponse, { Unit, paymentSplitsAndLatestTrip ->
                if (paymentSplitsAndLatestTrip.tripResponse.isExpediaRewardsRedeemable()) paymentSplitsAndLatestTrip.paymentSplits.paymentSplitsType()
                else PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
            })

}