package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.IProductFlavorFeatureConfiguration
import com.squareup.phrase.Phrase

object RewardsUtil {

    fun buildRewardText(context: Context, rewardPoints: String, configuration: IProductFlavorFeatureConfiguration): String {
        var rewardsPointsText = ""
        if (Strings.isNotEmpty(rewardPoints) && rewardPoints.toFloat() > 0) {
            var rewardPointValue = rewardPoints
            if (configuration.isRewardProgramPointsType) {
                rewardPointValue = StrUtils.roundOff(rewardPoints.toFloat(), 0)
            }
            rewardsPointsText = Phrase.from(context, R.string.confirmation_reward_points_TEMPLATE)
                    .put("rewardpoints", rewardPointValue)
                    .put("brand_reward_name", context.getString(R.string.brand_reward_name))
                    .format().toString()
        }
        return rewardsPointsText
    }
}


