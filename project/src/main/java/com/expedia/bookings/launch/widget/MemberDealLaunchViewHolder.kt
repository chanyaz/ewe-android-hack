package com.expedia.bookings.launch.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class MemberDealLaunchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleView: TextView by bindView(R.id.deal_launch_cell_title)
    val subTitleView: TextView by bindView(R.id.deal_launch_cell_subtitle)
}
