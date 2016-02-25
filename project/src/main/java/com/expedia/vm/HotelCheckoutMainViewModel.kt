package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable
import rx.subjects.PublishSubject

public class HotelCheckoutMainViewModel(paymentModel: PaymentModel<HotelCreateTripResponse>) {
    val animateInSlideToPurchaseSubject = PublishSubject.create<Unit>()
    
    //OUTLETS
    val updateEarnedRewards: Observable<Int> = paymentModel.paymentSplits.map { it.payingWithCards.points }
    val animateSlideToPurchaseWithPaymentSplits = animateInSlideToPurchaseSubject
            .withLatestFrom(paymentModel.paymentSplits, { Unit, paymentSplits -> paymentSplits.paymentSplitsType() })
}