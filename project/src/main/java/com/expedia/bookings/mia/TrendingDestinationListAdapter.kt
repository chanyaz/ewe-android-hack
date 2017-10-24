package com.expedia.bookings.mia

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.TrendingDestinationResponse
import com.expedia.bookings.data.sos.TrendingLocation
import com.expedia.bookings.mia.activity.TrendingDestinationActivity
import com.expedia.bookings.mia.vm.TrendingDestinationsViewModel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.util.subscribeText
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class TrendingDestinationListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<TrendingLocation> = emptyList()
    private var loading = true
    val resultSubject = BehaviorSubject.create<TrendingDestinationResponse>()
    val headerTextChangeSubject = BehaviorSubject.create<String>()

    init {
        listData = generateLoadingCells(3)
        resultSubject.subscribe { response ->
            if (response != null && response.trendinglocations != null) {
                loading = false
                headerTextChangeSubject.onNext(context.resources.getString(R.string.trending_destination_landing_page_header))
                listData = response.trendinglocations!!
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
            val view = LayoutInflater.from(context).inflate(R.layout.trending_destination_header, parent, false)
            val holder = MemberDealHeaderViewHolder(view)
            return holder
        }
        else if (viewType == itemType.LOADING_VIEW.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_loading_cell, parent, false)
            val holder = LoadingViewHolder(view)
            return holder
        }
        else if (viewType == itemType.DESTINATION_CARD.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.trending_destination_card, parent, false)
            val holder = TrendingDestinationViewHolder(view)

            view.setOnClickListener { v ->
                val memberDealActivity = context as TrendingDestinationActivity
                val animOptions = AnimUtils.createActivityScaleBundle(memberDealActivity.currentFocus)
            }

            return holder
        }
        else {
            throw RuntimeException("Could not find view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MemberDealHeaderViewHolder) {
            headerTextChangeSubject.subscribeText(holder.headerText)
        }
        if (holder is TrendingDestinationViewHolder) {
            val destination = listData[position - 1]
            val vm = TrendingDestinationsViewModel(context, destination)
            holder.bind(vm)
        }
        if (holder is LoadingViewHolder) {
            holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, (position - 1) % 2 == 0))
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return listData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return itemType.HEADER.ordinal
        }
        else if (loading){
            return itemType.LOADING_VIEW.ordinal
        }
        else {
            return itemType.DESTINATION_CARD.ordinal
        }
    }

    private fun generateLoadingCells (count: Int): List<TrendingLocation> {
        val listLoading = ArrayList<TrendingLocation>()
        for (i in 1..count) {
            listLoading.add(TrendingLocation())
        }
        return listLoading
    }
}
