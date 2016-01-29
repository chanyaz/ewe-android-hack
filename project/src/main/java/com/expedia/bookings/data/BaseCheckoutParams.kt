package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import java.util.ArrayList
import kotlin.collections.isNotEmpty
import kotlin.text.isNullOrEmpty

public data class BaseCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>, val cvv: String) {

    class Builder() {
        private var billingInfo: BillingInfo? = null
        private var travelers: ArrayList<Traveler> = ArrayList()
        private var cvv: String? = null

        fun billingInfo(billingInfo: BillingInfo?): BaseCheckoutParams.Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun travelers(traveler: Traveler?): BaseCheckoutParams.Builder {
            if (traveler != null) {
                this.travelers.add(traveler)
            }
            return this
        }

        fun cvv(cvv: String?): BaseCheckoutParams.Builder {
            this.cvv = cvv
            return this
        }


        fun build(): BaseCheckoutParams {
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val travelers = if (travelers.isEmpty()) throw IllegalArgumentException() else {
                travelers
            }
            val cvv = cvv ?: throw IllegalArgumentException()
            return BaseCheckoutParams(billingInfo, travelers, cvv)
        }

        public fun hasValidTravelerAndBillingInfo(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null
        }

        public fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() &&
                    billingInfo != null &&
                    !cvv.isNullOrEmpty()
        }
    }
}