package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.squareup.picasso.Picasso

class HotelItinPricingSummaryRewardsView(context: Context?, attrs: AttributeSet?) : CardView(context, attrs) {
    val rewardsLogoView by bindView<ImageView>(R.id.hotel_itin_pricing_rewards_logo)
    val earnedPointsText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_points_earned)
    val basePointsText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_points_base)
    val bonusPointsText by bindView<TextView>(R.id.hotel_itin_pricing_rewards_points_bonus)

    var viewModel: IHotelPricingRewardsViewModel by notNullAndObservable {
        it.logoSubject.subscribe {
            val endpointProvider = Ui.getApplication(context).appComponent().endpointProvider()
            val endPoint = endpointProvider.e3EndpointUrl.removeSuffix("/")
            Picasso.with(context).load(endPoint.plus(it)).into(rewardsLogoView)
            rewardsLogoView.visibility = View.VISIBLE
        }

        it.earnedPointsSubject.subscribe {
            earnedPointsText.visibility = View.VISIBLE
            earnedPointsText.text = it
        }

        it.basePointsSubject.subscribe {
            basePointsText.visibility = View.VISIBLE
            basePointsText.text = it
        }

        it.bonusPointsSubject.subscribe {
            bonusPointsText.visibility = View.VISIBLE
            bonusPointsText.text = it
        }
    }

    init {
        View.inflate(context, R.layout.hotel_itin_pricing_rewards_section_view, this)
    }
}