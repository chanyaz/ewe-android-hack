package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.account.HotelRewardSectionRowView
import com.expedia.bookings.account.MoneyRewardSectionRowView
import com.expedia.bookings.account.vm.RewardSectionViewModel
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.bindView
import rx.subjects.BehaviorSubject

class RewardSectionFragment: Fragment() {

    lateinit var user: User
    lateinit var rewardSectionView: View

    val rewardSectionTitle: TextView by bindView(R.id.reward_section_title)
    val rewardSectionSubtitle: TextView by bindView(R.id.reward_section_subtitle)

    private val hotelNightRowView: HotelRewardSectionRowView by bindView(R.id.hotel_night_row)
    private val moneySpentRowView: MoneyRewardSectionRowView by bindView(R.id.amount_spent_row)

    val rewardSectionAnimationSubject = BehaviorSubject.create<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rewardSectionView = inflater.inflate(R.layout.fragment_reward_section, null)
        return rewardSectionView
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hotelNightRowView.rowTitle.text = context.resources.getString(R.string.reward_hotel_night_title)
        moneySpentRowView.rowTitle.text = context.resources.getString(R.string.reward_amount_spent_title)
        rewardSectionAnimationSubject.subscribe {
            if(rewardSectionView.visibility == View.VISIBLE) {
                bindRewardInfo()
                animateProgressBar()
            }
        }
    }

    fun setVisibility(visibility: Int) {
        rewardSectionView.visibility = visibility
    }

    private fun bindRewardInfo() {
        if (User.isLoggedIn(context) && Db.getUser() != null) {
            val userLoyaltyInfo = Db.getUser().loyaltyMembershipInformation
            if (userLoyaltyInfo?.currentTierCredits != null && userLoyaltyInfo?.reqUpgradeCredits != null) {
                val vm = RewardSectionViewModel(context, userLoyaltyInfo)
                rewardSectionTitle.text = vm.title
                rewardSectionSubtitle.text = vm.subtitle
                hotelNightRowView.bind(vm)
                moneySpentRowView.bind(vm)
            }
        }
    }

    private fun animateProgressBar() {
        hotelNightRowView.progressBarAnimate()
        moneySpentRowView.progressBarAnimate()
    }

    fun resetProgressBar() {
        hotelNightRowView.progressBar.progress = 0
        moneySpentRowView.progressBar.progress = 0
    }
}
