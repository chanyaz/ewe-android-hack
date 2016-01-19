package com.expedia.vm

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.ValidPayment
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.collections.isNotEmpty

public class BaseCheckoutViewModel(val context: Context) {
    val builder = BaseCheckoutParams.Builder()

    // Inputs
    val validPayments = PublishSubject.create<List<ValidPayment>>()
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<Traveler>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val legalText = PublishSubject.create<SpannableStringBuilder>()
    val depositPolicyText = PublishSubject.create<Spanned>()

    // Outputs
    val infoCompleted = PublishSubject.create<Boolean>()
    val checkout = PublishSubject.create<BaseCheckoutParams>()

    init {
        travelerCompleted.subscribe {
            builder.travelers(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())

        }

        paymentCompleted.subscribe {
            builder.billingInfo(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        cvvCompleted.subscribe {
            builder.cvv(it)
            if (builder.hasValidParams()) {
              checkout.onNext(builder.build())
            }
        }
    }

}

public data class BaseCheckoutParams(val travelers: ArrayList<Traveler>, val billingInfo: BillingInfo, val cvv: String) {

    class Builder() {
        private var travelers: ArrayList<Traveler> = ArrayList()
        private var billingInfo: BillingInfo? = null
        private var cvv: String? = null

        fun travelers(traveler: Traveler): BaseCheckoutParams.Builder {
            this.travelers.add(traveler)
            return this
        }

        fun billingInfo(billingInfo: BillingInfo?): BaseCheckoutParams.Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun cvv(cvv: String?): BaseCheckoutParams.Builder {
            this.cvv = cvv
            return this
        }

        fun build(): BaseCheckoutParams {
            if (travelers.isEmpty()) {
                throw IllegalArgumentException()
            }
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val cvv = cvv ?: throw IllegalArgumentException()

            return BaseCheckoutParams(travelers, billingInfo, cvv)
        }

        public fun hasValidTravelerAndBillingInfo(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null
        }

        public fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null && cvv != null
        }

    }
}