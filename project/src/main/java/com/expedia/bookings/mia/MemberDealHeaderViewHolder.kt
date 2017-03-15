package com.expedia.bookings.mia

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class MemberDealHeaderViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    val headerText: TextView by bindView(R.id.member_deals_status_view)
}