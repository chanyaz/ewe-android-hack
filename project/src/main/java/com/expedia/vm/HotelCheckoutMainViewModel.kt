package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable

public class HotelCheckoutMainViewModel(paymentModel: PaymentModel<HotelCreateTripResponse>) {
    //OUTLETS
    val updateEarnedRewards: Observable<Int> = paymentModel.paymentSplits.map { it.payingWithCards.points }
}