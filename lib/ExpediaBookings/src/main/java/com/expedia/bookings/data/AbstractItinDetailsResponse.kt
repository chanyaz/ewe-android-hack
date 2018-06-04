package com.expedia.bookings.data

import com.expedia.util.ifNotNull
import com.google.gson.annotations.SerializedName

abstract class AbstractItinDetailsResponse {
    var responseType: String? = null
    val errors: List<Error>? = null

    class Error

    open class ResponseData {
        var levelOfDetail: String? = null
        var tripId: String? = null
        var webDetailsURL: String? = null
        var tripNumber: Long? = null
        var orderNumber: Long? = null
        var title: String? = null
        var updateTripNameDescPathURL: String? = null
        val sharableDetailsURL: String? = null
        var rewardList = emptyList<Rewards>()
        val email: Email? = null

        lateinit var startTime: Time
        lateinit var endTime: Time

        var totalTripPrice: TotalTripPrice? = null
        var paymentSummary: PaymentSummary? = null

        fun getTotalPaidMoney(): Money? {
            ifNotNull(paymentSummary?.totalPaidPrice?.amount, paymentSummary?.totalPaidPrice?.currency?.currencyCode) { amount, currencyCode ->
                return Money(amount, currencyCode)
            }
            return null
        }

        data class PaymentSummary(val totalPaidPrice: TotalPaidPrice?)
        data class TotalPaidPrice(val amount: String?, val currency: Currency?)
        data class Currency(val currencyCode: String?)

        class TotalTripPrice {
            var currency: String? = null
            var total: String? = null
            var totalFormatted: String? = null
        }

        class Rewards {
            var totalPoints: Long? = null
        }

        class Email {
            @SerializedName("m_email")
            val address: String? = null
        }
    }

    class Time {
        lateinit var localizedShortDate: String
        lateinit var localizedMediumDate: String
        lateinit var localizedShortTime: String
        lateinit var localizedFullDate: String
        lateinit var raw: String
    }

    abstract fun getResponseDataForItin(): ResponseData?
}
