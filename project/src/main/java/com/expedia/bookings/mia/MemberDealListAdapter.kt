package com.expedia.bookings.mia

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.MemberDealDestination
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.mia.vm.MemberDealDestinationViewModel

class MemberDealListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<MemberDealDestination> = emptyList()
    private var currency: String? = null


    enum class itemType {
        HEADER,
        DESTINATION_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == itemType.HEADER.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_header, parent, false)
            val holder = MemberDealHeaderViewHolder(view)
            return holder
        }
        else if (viewType == itemType.DESTINATION_CARD.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_card, parent, false)
            val holder = MemberDealDestinationViewHolder(view)
            return holder
        }
        else {
            throw RuntimeException("Could not find view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MemberDealDestinationViewHolder) {
            val destination = listData[position]
            val leadingHotel = destination.getLeadingHotel()
            if (leadingHotel != null) {
                val vm = MemberDealDestinationViewModel(context, leadingHotel, currency)
                holder.bind(vm)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return itemType.HEADER.ordinal
        } else {
            return itemType.DESTINATION_CARD.ordinal
        }
    }

    fun updateDealsList(listData: List<MemberDealDestination>) {
        this.listData = listData
        this.notifyDataSetChanged()
    }

    fun setCurrency(currency: String?) {
        this.currency = currency
    }
}
