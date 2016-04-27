package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BaseCheckoutViewModel(val context: Context) {
    val builder = BaseCheckoutParams.Builder()

    // Inputs
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<List<Traveler>>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo?>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val checkoutInfoCompleted = PublishSubject.create<BaseCheckoutParams>()

    // Outputs
    val infoCompleted = BehaviorSubject.create<Boolean>()

    init {
        travelerCompleted.subscribe {
            builder.travelers(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        paymentCompleted.subscribe { billingInfo ->
            builder.billingInfo(billingInfo)
            builder.cvv(billingInfo?.securityCode)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        cvvCompleted.subscribe {
            builder.cvv(it)
            if (builder.hasValidParams()) {
                checkoutInfoCompleted.onNext(builder.build())
            }
        }
    }
}