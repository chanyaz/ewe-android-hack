package com.expedia.bookings.data

import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsDetails
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPaymentPreferences
import java.math.BigDecimal
import kotlin.properties.Delegates

abstract class TripResponse : BaseApiResponse() {
    lateinit var tripId: String
    var pointsDetails: List<PointsDetails>? = null
    var userPreferencePoints: UserPaymentPreferences? = null
    var rewards: RewardsInfo? = null
    var validFormsOfPayment: List<ValidFormOfPayment> = emptyList()
    var guestUserPromoEmailOptInStatus: String? = null

    fun getPointDetails(): PointsDetails? {
        val programName = getProgramName()
        return pointsDetails?.firstOrNull { it.programName == programName }
    }

    fun getProgramName(): ProgramName? = pointsDetails?.firstOrNull()?.programName

    abstract fun getTripTotalExcludingFee(): Money

    abstract fun tripTotalPayableIncludingFeeIfZeroPayableByPoints(): Money

    /* Card details are not always required for booking.
       1) Cars : If create trip response has the flag checkoutRequiresCard.
       2) Hotels : In case booking with points only.
    */
    abstract fun isCardDetailsRequiredForBooking(): Boolean

    abstract fun getOldPrice(): Money?

    var isRewardsEnabledForCurrentPOS: Boolean by Delegates.notNull()
    fun isRewardsRedeemable(): Boolean {
        if (!isRewardsEnabledForCurrentPOS) {
            return false
        }

        val rewardsPointsDetails = getPointDetails()
        return rewardsPointsDetails?.isAllowedToRedeem == true
    }

    fun rewardsUserAccountDetails(): PointsDetails {
        return getPointDetails()!!
    }

    fun totalAvailableBurnAmount(): Money {
        return getPointDetails()!!.totalAvailable.amount
    }

    fun maxPayableWithRewardPoints(): Money {
        return rewardsUserAccountDetails().maxPayableWithPoints!!.amount
    }

    fun paymentSplitsWhenZeroPayableWithPoints(): PaymentSplits {
        val payingWithPoints = PointsAndCurrency(0f, PointsType.BURN, Money(BigDecimal.ZERO, getTripTotalExcludingFee().currencyCode))
        val payingWithCards = PointsAndCurrency(rewards?.totalPointsToEarn ?: 0f, PointsType.EARN, getTripTotalExcludingFee())
        return PaymentSplits(payingWithPoints, payingWithCards)
    }

    fun paymentSplitsWhenMaxPayableWithPoints(): PaymentSplits {
        val pointDetails = rewardsUserAccountDetails()
        return PaymentSplits(pointDetails.maxPayableWithPoints!!, pointDetails.remainingPayableByCard!!)
    }

    //Note: Invoking this makes sense on the response received from Create-Trip only, so we are not dealing with
    fun paymentSplitsForNewCreateTrip(swpOpted: Boolean): PaymentSplits {
        return if (isRewardsRedeemable() && swpOpted) paymentSplitsWhenMaxPayableWithPoints() else paymentSplitsWhenZeroPayableWithPoints()
    }

    fun paymentSplitsSuggestionsForNewCreateTrip(): PaymentSplits {
        return if (isRewardsRedeemable()) paymentSplitsWhenMaxPayableWithPoints() else paymentSplitsWhenZeroPayableWithPoints()
    }

    fun getTripTotalIncludingFeeForCreateTrip(swpOpted: Boolean): Money {
        return if (isRewardsRedeemable() && swpOpted) rewardsUserAccountDetails().tripTotalPayable!!
        else tripTotalPayableIncludingFeeIfZeroPayableByPoints()
    }

    fun paymentSplitsForPriceChange(pwpOpted: Boolean): PaymentSplits {
        if (!pwpOpted) {
            return paymentSplitsWhenZeroPayableWithPoints()
        } else if (!isRewardsRedeemable()) {
            return paymentSplitsWhenZeroPayableWithPoints()
        } else if (userPreferencePoints != null) {
            return PaymentSplits(userPreferencePoints!!.getUserPreference(getProgramName()!!)!!, userPreferencePoints!!.remainingPayableByCard)
        } else {
            return paymentSplitsWhenMaxPayableWithPoints()
        }
    }

    fun paymentSplitsSuggestionsForPriceChange(pwpOpted: Boolean): PaymentSplits {
        if (!isRewardsRedeemable()) {
            return paymentSplitsWhenZeroPayableWithPoints()
        } else if (!pwpOpted) {
            return paymentSplitsWhenMaxPayableWithPoints()
        } else if (userPreferencePoints != null)
            return PaymentSplits(userPreferencePoints!!.getUserPreference(getProgramName()!!)!!, userPreferencePoints!!.remainingPayableByCard)
        else return paymentSplitsWhenMaxPayableWithPoints()
    }

    fun getTripTotalIncludingFeeForPriceChange(pwpOpted: Boolean): Money {
        if (!pwpOpted || !isRewardsRedeemable()) {
            return tripTotalPayableIncludingFeeIfZeroPayableByPoints()
        } else if (userPreferencePoints != null) {
            return userPreferencePoints!!.tripTotalPayable
        } else {
            return rewardsUserAccountDetails().tripTotalPayable!!
        }
    }

    open fun newPrice(): Money {
         return tripTotalPayableIncludingFeeIfZeroPayableByPoints()
    }
}
