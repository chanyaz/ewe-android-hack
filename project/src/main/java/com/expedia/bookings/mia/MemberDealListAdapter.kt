package com.expedia.bookings.mia

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.data.sos.DealsResponse
import com.expedia.bookings.mia.activity.MemberDealsActivity
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.util.subscribeOnClick
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import java.util.ArrayList

class MemberDealListAdapter(private val context: Context, private val searchHotelsClickedObserver: Observer<Unit>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<DealsDestination> = emptyList()
    private var currency: String? = null
    private var loading = true

    val resultSubject = BehaviorSubject.create<DealsResponse>()

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

    enum class itemType {
        HEADER,
        LOADING_VIEW,
        DESTINATION_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == itemType.HEADER.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_header, parent, false)
            val holder = MemberDealHeaderViewHolder(view)
            holder.rootCardView.subscribeOnClick(searchHotelsClickedObserver)
            return MemberDealHeaderViewHolder(view)
        } else if (viewType == itemType.LOADING_VIEW.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.deal_loading_cell, parent, false)
            return LoadingViewHolder(view)
        } else if (viewType == itemType.DESTINATION_CARD.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.deals_card, parent, false)
            val holder = DealsDestinationViewHolder(view)
            view.setOnClickListener {
                val memberDealActivity = context as MemberDealsActivity
                val animOptions = AnimUtils.createActivityScaleBundle(memberDealActivity.currentFocus)
                HotelNavUtils.goToHotels(this.context, holder.searchParams, animOptions, NavUtils.MEMBER_ONLY_DEAL_SEARCH)
            }
            return holder
        } else {
            throw RuntimeException("Could not find view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is DealsDestinationViewHolder) {
            val destination = listData[position - 1]
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
        return listData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return itemType.HEADER.ordinal
        } else if (loading) {
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
