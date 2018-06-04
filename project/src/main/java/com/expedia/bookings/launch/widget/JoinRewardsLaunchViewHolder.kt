package com.expedia.bookings.launch.widget

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.mobiata.android.Log

class JoinRewardsLaunchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val join_button: CardView by bindView(R.id.join_rewards_button)

    init {
        join_button.setOnClickListener {
            Log.d("Join Rewards Clicked")
            OmnitureTracking.trackTapJoinRewardsLaunchTile()
        }
    }
}
