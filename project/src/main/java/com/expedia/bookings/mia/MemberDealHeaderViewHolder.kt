package com.expedia.bookings.mia

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class MemberDealHeaderViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    val rootCardView: CardView by bindView(R.id.search_for_hotel_deals_card_view)
    val headerText: TextView by bindView(R.id.member_deals_status_view)
}