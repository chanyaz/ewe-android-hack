package com.expedia.bookings.mia

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.mia.activity.LastMinuteDealsActivity
import com.expedia.bookings.mia.vm.LastMinuteDealsCardViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.LoadingViewHolder
import java.util.ArrayList

class LastMinuteDealsListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<DealsDestination.Hotel> = emptyList()
    val responseObserver: LiveDataObserver<LastMinuteDealsResponse>
    private var currency: String? = null
    private var loading = true

    init {
        listData = generateLoadingCells(3)
        responseObserver = LiveDataObserver {
            response ->
                if (response != null) {
                    loading = false
                    currency = response.offerInfo?.currency
                    listData = response.offers.hotels
                    listData = sortHotelByDiscount(listData)
                    notifyDataSetChanged()
                }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is DealsCardViewHolder) {
            val lastMinuteHotel = listData[position]
            val vm = LastMinuteDealsCardViewModel(context, lastMinuteHotel, currency)
            holder.bind(vm)
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
            val holder = DealsCardViewHolder(view)
            view.setOnClickListener {
                val lastMinuteDealActivity = context as LastMinuteDealsActivity
                var animOptions: Bundle = Bundle.EMPTY
                if (lastMinuteDealActivity.currentFocus != null) {
                    animOptions = AnimUtils.createActivityScaleBundle(lastMinuteDealActivity.currentFocus)
                }
                HotelNavUtils.goToHotels(this.context, holder.searchParams, animOptions, NavUtils.DEAL_SEARCH)
                OmnitureTracking.trackLastMinuteDealDestinationTappedRank(holder.adapterPosition)
            }
            return holder
        } else {
            throw RuntimeException("Could not find view type")
        }
    }

    fun sortHotelByDiscount(hotels: List<DealsDestination.Hotel> ): List<DealsDestination.Hotel> {
        val discountedHotels = hotels.filter { it ->
            it.hotelPricingInfo?.hasDiscount() ?: false
        }
        return discountedHotels.sortedByDescending { it.hotelPricingInfo?.percentSavings }
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

    private fun generateLoadingCells(count: Int): List<DealsDestination.Hotel> {
        val listLoading = ArrayList<DealsDestination.Hotel>()
        for (i in 1..count) {
            listLoading.add(DealsDestination.Hotel())
        }
        return listLoading
    }
}
