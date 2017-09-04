package com.expedia.bookings.data.flights

import com.expedia.bookings.utils.Constants
import java.math.BigDecimal
import java.util.HashMap

class FlightCreateTripParams(val productKey: String, val flexEnabled: Boolean, val featureOverride: String?,
                            val fareFamilyCode: String?, val fareFamilyTotalPrice: BigDecimal?) {

    class Builder {
        private var productKey: String? = null
        private var isFlexEnabled: Boolean = false
        private var featureOverride: String? = null
        private var fareFamilyCode: String? = null
        private var fareFamilyTotalPrice: BigDecimal? = null

        fun build(): FlightCreateTripParams {
            return FlightCreateTripParams(productKey ?: throw IllegalArgumentException(), isFlexEnabled, featureOverride, fareFamilyCode, fareFamilyTotalPrice)
        }

        fun productKey(productKey: String?): Builder {
            this.productKey = productKey
            return this
        }

        fun enableSubPubFeature(): Builder {
            this.featureOverride = Constants.FEATURE_SUBPUB
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
    }

    fun toQueryMap(): Map<String, Any> {
        val params = HashMap<String, Any>()
        params.put("productKey", productKey)
        return params
    }
}
