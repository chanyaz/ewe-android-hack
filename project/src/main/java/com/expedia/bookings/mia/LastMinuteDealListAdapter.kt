package com.expedia.bookings.mia

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.data.sos.DealsResponse
import com.expedia.bookings.mia.activity.LastMinuteDealActivity
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.widget.LoadingViewHolder
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class LastMinuteDealListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<DealsDestination> = emptyList()
    val resultSubject = BehaviorSubject.create<DealsResponse>()
    private var currency: String? = null
    private var loading = true

    init {
        listData = generateLoadingCells(3)
        resultSubject.subscribe { response ->
            if (response != null && response.destinations != null) {
                loading = false
                currency = response.offerInfo?.currency
                listData = response.destinations!!
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is DealsDestinationViewHolder) {
            val destination = listData[position]
            val leadingHotel = destination.getLeadingHotel()
            if (leadingHotel != null) {
                val vm = DealsDestinationViewModel(context, leadingHotel, currency)
                holder.bind(vm)
            }
        }
        if (holder is LoadingViewHolder) {
            holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, (position - 1) % 2 == 0))
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == itemType.LOADING_VIEW.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.deal_loading_cell, parent, false)
            val holder = LoadingViewHolder(view)
            return holder
        } else if (viewType == itemType.DESTINATION_CARD.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.deals_card, parent, false)
            val holder = DealsDestinationViewHolder(view)
            view.setOnClickListener { v ->
                val lastMinuteDealActivity = context as LastMinuteDealActivity
                var animOptions: Bundle = Bundle.EMPTY
                if (lastMinuteDealActivity.currentFocus != null) {
                    animOptions = AnimUtils.createActivityScaleBundle(lastMinuteDealActivity.currentFocus)
                }
                HotelNavUtils.goToHotels(this.context, holder.searchParams, animOptions, 0)
                OmnitureTracking.trackLastMinuteDealDestinationTappedRank(holder.adapterPosition)
            }
            return holder
        } else {
            throw RuntimeException("Could not find view type")
        }
    }

    enum class itemType {
        LOADING_VIEW,
        DESTINATION_CARD
    }

    override fun getItemViewType(position: Int): Int {
        if (loading) {
            return itemType.LOADING_VIEW.ordinal
        } else {
            return itemType.DESTINATION_CARD.ordinal
        }
    }

    private fun generateLoadingCells(count: Int): List<DealsDestination> {
        val listLoading = ArrayList<DealsDestination>()
        for (i in 1..count) {
            listLoading.add(DealsDestination())
        }
        return listLoading
    }
}