package com.expedia.bookings.data

import java.util.ArrayList

open class BaseCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>, val cvv: String) {

    open class Builder() {
        protected var billingInfo: BillingInfo? = null
        protected var travelers: ArrayList<Traveler> = ArrayList()
        protected var cvv: String? = null

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


        open fun build(): BaseCheckoutParams {
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

        open fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() &&
                    billingInfo != null &&
                    !cvv.isNullOrEmpty()
        }
    }
}