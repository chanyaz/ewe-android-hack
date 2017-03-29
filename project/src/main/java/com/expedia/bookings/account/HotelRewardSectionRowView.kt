package com.expedia.bookings.account

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.account.vm.RewardSectionViewModel
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.squareup.phrase.Phrase

class HotelRewardSectionRowView(context: Context, attrs: AttributeSet) : RewardSectionRowView(context, attrs){

    init {
        inflate(getContext(), R.layout.reward_row_layout, this)

    }

    fun bind(vm: RewardSectionViewModel) {
        currentAmount.text = Phrase.from(context, R.string.reward_progress_current_night).put("number", vm.currentNight.toString()).format().toString()
        totalAmount.text = Phrase.from(context, R.string.reward_progress_total_night).put("number", vm.totalNight.toString()).format().toString()
        leftAmount.text = Phrase.from(context, R.string.reward_progress_left_night).put("number", vm.leftNight.toString()).format().toString()
        targetProgressBarValue = vm.progressNight ?: 0
        progressBar.progressDrawable =
                when(vm.userMembershipTier) {
                    LoyaltyMembershipTier.BASE -> context.getDrawable(R.drawable.reward_row_progressbar_blue)
                    LoyaltyMembershipTier.MIDDLE -> context.getDrawable(R.drawable.reward_row_progressbar_silver)
                    LoyaltyMembershipTier.TOP -> context.getDrawable(R.drawable.reward_row_progressbar_gold)
                    else -> context.getDrawable(R.drawable.reward_row_progressbar_blue)
                }
    }

}