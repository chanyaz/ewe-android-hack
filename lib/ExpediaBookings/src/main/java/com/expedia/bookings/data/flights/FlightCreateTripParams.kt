package com.expedia.bookings.data.flights

import java.math.BigDecimal
import java.util.HashMap

class FlightCreateTripParams(val productKey: String, val flexEnabled: Boolean, val featureOverride: String?,
                            val fareFamilyCode: String?, val fareFamilyTotalPrice: BigDecimal?, private val numberOfAdultTravelers: Int?,
                             val childTravelerAge: List<Int>?, val infantSeatingInLap: Boolean?) {

    class Builder {
        private var productKey: String? = null
        private var isFlexEnabled: Boolean = false
        private var featureOverride: String? = null
        private var fareFamilyCode: String? = null
        private var fareFamilyTotalPrice: BigDecimal? = null
        private var numberOfAdultTravelers: Int? = null
        private var childTravelerAge: List<Int>? = emptyList()
        private var infantSeatingInLap: Boolean? = null

        fun build(): FlightCreateTripParams {
            return FlightCreateTripParams(productKey ?: throw IllegalArgumentException(), isFlexEnabled, featureOverride, fareFamilyCode, fareFamilyTotalPrice,
                    numberOfAdultTravelers, childTravelerAge, infantSeatingInLap)
        }

        fun productKey(productKey: String?): Builder {
            this.productKey = productKey
            return this
        }

        fun setFeatureOverride(newFeatureOverride: String): Builder {
            if (featureOverride.isNullOrBlank()) {
                featureOverride = newFeatureOverride
            } else {
                val builder = StringBuilder(featureOverride)
                builder.append(",")
                builder.append(newFeatureOverride)
                featureOverride = builder.toString()
            }
            return this
        }

        fun setFlexEnabled(isFlexEnabled: Boolean): Builder {
            this.isFlexEnabled = isFlexEnabled
            return this
        }

        fun fareFamilyCode(fareFamilyCode: String): Builder {
            this.fareFamilyCode = fareFamilyCode
            return this
        }

        fun fareFamilyTotalPrice(fareFamilyTotalPrice: BigDecimal): Builder {
            this.fareFamilyTotalPrice = fareFamilyTotalPrice
            return this
        }

        fun setNumberOfAdultTravelers(numberOfAdultTravelers: Int): Builder {
            this.numberOfAdultTravelers = numberOfAdultTravelers
            return this
        }

        fun setChildTravelerAge(childTravelerAge: List<Int>): Builder {
            this.childTravelerAge = childTravelerAge
            return this
        }

        fun setInfantSeatingInLap(infantSeatingInLap: Boolean): Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }
    }

    private fun commonQueryParamsForCreateTrip(): HashMap<String, Any> {
        val params = HashMap<String, Any>()
        params["productKey"] = productKey
        return params
    }

    fun queryParamsForOldCreateTrip(): Map<String, Any> {
        return commonQueryParamsForCreateTrip()
    }

    fun queryParamsForNewCreateTrip(): Map<String, Any> {
        var params = HashMap<String, Any>()
        params = commonQueryParamsForCreateTrip()
        params["numberOfAdultTravelers"] = numberOfAdultTravelers!!
        params["infantSeatingInLap"] = infantSeatingInLap!!
        return params
    }
}
