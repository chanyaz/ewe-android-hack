package com.expedia.bookings.data

import com.expedia.bookings.data.cars.BaseApiResponse
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsDetails
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPaymentPreferences
import kotlin.collections.forEach
import kotlin.properties.Delegates

public abstract class TripResponse : BaseApiResponse() {
    lateinit var tripId: String
    var pointsDetails: List<PointsDetails>? = null
    var userPreferencePoints: UserPaymentPreferences? = null
    lateinit var validFormsOfPayment: List<ValidPayment>
    lateinit var expediaRewards: ExpediaRewards
    var guestUserPromoEmailOptInStatus: String? = null

    class ExpediaRewards {
        val totalPointsToEarn: Int = 0
        val isActiveRewardsMember: Boolean = false
        val rewardsMembershipTierName: String by Delegates.notNull()
        //Utility Member for local modifications in case we receive updated expedia rewards when we modify the Points to be burned. Not received by deserialization/server-response.
        private var updatedExpediaRewards: Int? = null

        fun setUpdatedExpediaRewards(points: Int) {
            updatedExpediaRewards = points
        }

        fun getUpdatedExpediaRewards(): Int? {
            return if (updatedExpediaRewards != null) updatedExpediaRewards else totalPointsToEarn
        }

    }

    fun getPointDetails(programName: ProgramName): PointsDetails? {
        pointsDetails?.forEach {
            if (it.programName == programName) {
                return it
            }
        }
        return null
    }

    abstract fun getTripTotal(): Money

    fun isExpediaRewardsRedeemable(): Boolean {
        val expediaRewardsPointsDetails = getPointDetails(ProgramName.ExpediaRewards)
        return if (expediaRewardsPointsDetails != null) expediaRewardsPointsDetails.isAllowedToRedeem else false
    }

    fun expediaRewardsUserAccountDetails(): PointsDetails {
        return getPointDetails(ProgramName.ExpediaRewards)!!
    }

    fun totalAvailableBurnAmount(programName: ProgramName): Money {
        return getPointDetails(programName)!!.totalAvailable.amount
    }

    fun maxPayableWithExpediaRewardPoints(): Money {
        return expediaRewardsUserAccountDetails().maxPayableWithPoints!!.amount
    }

    fun paymentSplitsWhenZeroPayableWithPoints(): PaymentSplits {
        val payingWithPoints = PointsAndCurrency(0, PointsType.BURN, Money("0", getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(expediaRewards.totalPointsToEarn, PointsType.EARN, getTripTotal())
        return PaymentSplits(payingWithPoints, payingWithCards)
    }

    fun paymentSplitsWhenMaxPayableWithPoints(): PaymentSplits {
        val expediaPointDetails = expediaRewardsUserAccountDetails()
        return PaymentSplits(expediaPointDetails.maxPayableWithPoints!!, expediaPointDetails.remainingPayableByCard!!)
    }

    fun paymentSplitsForPriceChange(): PaymentSplits {
        if (isExpediaRewardsRedeemable()) {
            if (userPreferencePoints != null)
                return PaymentSplits(userPreferencePoints!!.getUserPreference(ProgramName.ExpediaRewards)!!, userPreferencePoints!!.remainingPayableByCard)
            else
                return paymentSplitsWhenMaxPayableWithPoints()
        }
        else {
            return paymentSplitsWhenZeroPayableWithPoints()
        }
    }
}