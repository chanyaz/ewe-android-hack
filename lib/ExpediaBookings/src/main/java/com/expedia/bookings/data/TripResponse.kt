package com.expedia.bookings.data

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
    lateinit var validFormsOfPayment: List<ValidPayment>
    lateinit var expediaRewards: ExpediaRewards
    var guestUserPromoEmailOptInStatus: String? = null

    class ExpediaRewards {
        val totalPointsToEarn: Float = 0f
        val isActiveRewardsMember: Boolean = false
        val rewardsMembershipTierName: String by Delegates.notNull()
        //Utility Member for local modifications in case we receive updated expedia rewards when we modify the Points to be burned. Not received by deserialization/server-response.
        private var updatedExpediaRewards: Float? = null

        fun setUpdatedExpediaRewards(points: Float) {
            updatedExpediaRewards = points
        }

        fun getUpdatedExpediaRewards(): Float? {
            return if (updatedExpediaRewards != null) updatedExpediaRewards else totalPointsToEarn
        }

    }

    fun getPointDetails(): PointsDetails? {
        val programName = getProgramName()
        return pointsDetails?.firstOrNull { it.programName == programName } ?: null
    }

    fun getProgramName(): ProgramName? {
        return pointsDetails?.filter { it.programName == ProgramName.ExpediaRewards || it.programName == ProgramName.Orbucks }?.firstOrNull()?.programName ?: null
    }

    abstract fun getTripTotal(): Money

    /* Card details are not always required for booking.
       1) Cars : If create trip response has the flag checkoutRequiresCard.
       2) Hotels : In case booking with points only.
    */
    abstract fun isCardDetailsRequiredForBooking(): Boolean

    fun isRewardsRedeemable(): Boolean {
        val rewardsPointsDetails = getPointDetails()
        return if (rewardsPointsDetails != null) rewardsPointsDetails.isAllowedToRedeem else false
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
        val payingWithPoints = PointsAndCurrency(0f, PointsType.BURN, Money(BigDecimal.ZERO, getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(expediaRewards.totalPointsToEarn.toFloat(), PointsType.EARN, getTripTotal())
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
        else
            return paymentSplitsWhenMaxPayableWithPoints()
    }
}