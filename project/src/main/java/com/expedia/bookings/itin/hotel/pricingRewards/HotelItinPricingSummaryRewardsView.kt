package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.squareup.picasso.Picasso

class HotelItinPricingSummaryRewardsView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val rewardsTitleText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_title)

    val rewardsSection by bindView<CardView>(R.id.hotel_itin_pricing_rewards_section)
    val rewardsLogoView by bindView<ImageView>(R.id.hotel_itin_pricing_rewards_logo)
    val earnedPointsText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_points_earned)
    val basePointsText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_points_base)
    val bonusPointsContainer by bindView<LinearLayout>(R.id.hotel_itin_pricing_rewards_points_bonus_container)

    val rewardsButton by bindView<TextView>(R.id.hotel_itin_pricing_rewards_view_rewards_button)

    var viewModel: IHotelPricingRewardsViewModel by notNullAndObservable {
        it.hideWidgetSubject.subscribe {
            rewardsTitleText.visibility = View.GONE
            rewardsSection.visibility = View.GONE
            rewardsButton.visibility = View.GONE
        }

        it.logoSubject.subscribe {
            try {
                Picasso.with(context).load(it).into(rewardsLogoView)
                rewardsLogoView.visibility = View.VISIBLE
            } catch (e: Exception) {
                System.out.println("HOTEL ITIN PRICING AND REWARDS - Error loading logo: " + e.printStackTrace())
            }
        }

        it.earnedPointsSubject.subscribeTextAndVisibility(earnedPointsText)

        it.basePointsSubject.subscribeTextAndVisibility(basePointsText)

        it.bonusPointsSubject.subscribe { list ->
            bonusPointsContainer.visibility = View.VISIBLE
            bonusPointsContainer.removeAllViews()
            list.forEach {
                val bonusPointsView = BonusPointsView(context, null)
                bonusPointsView.text = it
                bonusPointsContainer.addView(bonusPointsView)
            }
        }

        rewardsButton.subscribeOnClick(it.rewardsButtonClickSubject)
    }

    init {
        View.inflate(context, R.layout.hotel_itin_pricing_rewards_section_view, this)
    }
}

class BonusPointsView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    init {
        this.textSize = 14.0f
        this.setTextColor(android.support.v4.content.ContextCompat.getColor(context, R.color.itin_price_summary_label_gray_light))
        FontCache.setTypeface(this, FontCache.Font.ROBOTO_REGULAR)
    }
}
