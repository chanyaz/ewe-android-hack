package com.expedia.bookings.account

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.account.vm.RewardSectionViewModel
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

class MoneyRewardSectionRowView(context: Context, attrs: AttributeSet) : RewardSectionRowView(context, attrs){

    init {
        inflate(getContext(), R.layout.reward_row_layout, this)
    }

    fun bind(vm: RewardSectionViewModel) {
        currentAmount.text = Phrase.from(context, R.string.reward_progress_current_amount).put("amount", getPriceWithCurrency(vm.currencyCode, vm.currentMoney)).format().toString()
        totalAmount.text = Phrase.from(context, R.string.reward_progress_total_amount).put("amount", getPriceWithCurrency(vm.currencyCode, vm.totalMoney)).format().toString()
        leftAmount.text = Phrase.from(context, R.string.reward_progress_left_amount).put("amount", getPriceWithCurrency(vm.currencyCode, vm.leftMoney)).format().toString()
        targetProgressBarValue = vm.progressMoney?.toInt() ?: 0
        progressBar.progressDrawable =
                when(vm.userMembershipTier) {
                    LoyaltyMembershipTier.BASE -> context.getDrawable(R.drawable.reward_row_progressbar_blue)
                    LoyaltyMembershipTier.MIDDLE -> context.getDrawable(R.drawable.reward_row_progressbar_silver)
                    LoyaltyMembershipTier.TOP -> context.getDrawable(R.drawable.reward_row_progressbar_gold)
                    else -> context.getDrawable(R.drawable.reward_row_progressbar_blue)
                }
    }

    fun getPriceWithCurrency(currency: String?, amount: BigDecimal?): String {
        if (amount == null || currency == null) {
            return ""
        }
        return Money(amount, currency).getFormattedMoney(Money.F_NO_DECIMAL).toString()
    }
}
