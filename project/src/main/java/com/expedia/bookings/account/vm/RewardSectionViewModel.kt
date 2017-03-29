package com.expedia.bookings.account.vm

import android.content.Context
import android.text.SpannableStringBuilder
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.UserLoyaltyMembershipInformation
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import java.math.BigDecimal

class RewardSectionViewModel(val context: Context, userLoyaltyInfo: UserLoyaltyMembershipInformation?) {

    var userMembershipTier = userLoyaltyInfo?.loyaltyMembershipTier

    var currentNight = userLoyaltyInfo?.currentTierCredits?.hotelNights
    var leftNight = userLoyaltyInfo?.reqUpgradeCredits?.hotelNights
    var totalNight = if (leftNight != null) currentNight?.plus(leftNight!!) else 0
    var progressNight = if (totalNight != null && totalNight!! > 0) currentNight?.div(totalNight!!)?.times(1000) else 0

    var currencyCode = userLoyaltyInfo?.currentTierCredits?.amount?.currencyCode
    var currentMoney = userLoyaltyInfo?.currentTierCredits?.amount?.amount
    var leftMoney = userLoyaltyInfo?.reqUpgradeCredits?.amount?.amount
    var totalMoney = currentMoney?.add(leftMoney)
    var progressMoney = if (totalMoney != null && totalMoney!!.toInt() > 0) currentMoney?.div(totalMoney!!)?.times(BigDecimal(1000)) else BigDecimal.ZERO

    // TODO: waiting for adition params from API
    var expirationYear = 2017

    var title = Phrase.from(context, R.string.about_section_reward_progress_title).put("year", LocalDate.now().year).put("brand_reward_name", BuildConfig.brand).format().toString()
    var subtitle = getSubtitleBasedOnTier(userMembershipTier, expirationYear)

    fun getSubtitleBasedOnTier(tier: LoyaltyMembershipTier?, expirationYear: Int): SpannableStringBuilder {

        return when(tier) {
            LoyaltyMembershipTier.BASE -> {
                val nextTier = context.resources.getString(R.string.reward_middle_tier_name_short)
                StrUtils.makeSubstringBold(Phrase.from(context, R.string.about_section_reward_progress_subtitle_complete).put("tier", nextTier).format().toString(), nextTier)
            }
            LoyaltyMembershipTier.MIDDLE -> {
                if (LocalDate.now().year < expirationYear) {
                    val nextTier = context.resources.getString(R.string.reward_top_tier_name_short)
                    StrUtils.makeSubstringBold(Phrase.from(context, R.string.about_section_reward_progress_subtitle_complete).put("tier", nextTier).format().toString(), nextTier)
                }
                else {
                    SpannableStringBuilder(Phrase.from(context, R.string.about_section_reward_progress_subtitle_requalify).put("year", expirationYear+1).format().toString())
                }
            }
            LoyaltyMembershipTier.TOP -> {
                if (LocalDate.now().year < expirationYear) {
                    val currentTier = context.resources.getString(R.string.reward_top_tier_name_short)
                    StrUtils.makeSubstringBold(Phrase.from(context, R.string.about_section_reward_progress_subtitle_earned).put("tier", currentTier).format().toString(), currentTier)
                }
                else {
                    SpannableStringBuilder(Phrase.from(context, R.string.about_section_reward_progress_subtitle_requalify).put("year", expirationYear+1).format().toString())
                }
            }
            else -> SpannableStringBuilder("")
        }
    }
}
