package com.expedia.bookings.data

import java.util.ArrayList

data class BaseCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>, val cvv: String) {

    class Builder() {
        private var billingInfo: BillingInfo? = null
        private var travelers: ArrayList<Traveler> = ArrayList()
        private var cvv: String? = null

        fun billingInfo(billingInfo: BillingInfo?): Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun travelers(travelers: List<Traveler>): Builder {
            this.travelers.clear()
            this.travelers.addAll(travelers)
            return this
        }

        fun cvv(cvv: String?): Builder {
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

        fun hasValidTravelerAndBillingInfo(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null
        }

        fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() &&
                    billingInfo != null &&
                    !cvv.isNullOrEmpty()
        }
    }
}