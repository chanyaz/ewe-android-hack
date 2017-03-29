package com.expedia.bookings.mia

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.MemberDealDestination
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.mia.activity.MemberDealActivity
import com.expedia.bookings.mia.vm.MemberDealDestinationViewModel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class MemberDealListAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData: List<MemberDealDestination> = emptyList()
    private var currency: String? = null
    private var loading = true
    val resultSubject = BehaviorSubject.create<MemberDealResponse>()
    val headerTextChangeSubject = BehaviorSubject.create<String>()

    init {
        listData = generateLoadingCells(3)
        resultSubject.subscribe { response ->
            if (response != null && response.destinations != null) {
                loading = false
                headerTextChangeSubject.onNext(context.resources.getString(R.string.member_deal_landing_page_header))
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
            return holder
        }
        else if (viewType == itemType.LOADING_VIEW.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_loading_cell, parent, false)
            val holder = LoadingViewHolder(view)
            return holder
        }
        else if (viewType == itemType.DESTINATION_CARD.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.member_deal_card, parent, false)
            val holder = MemberDealDestinationViewHolder(view)

            view.setOnClickListener { v ->
                val memberDealActivity = context as MemberDealActivity
                val animOptions = AnimUtils.createActivityScaleBundle(memberDealActivity.currentFocus)
                NavUtils.goToHotels(this.context, holder.searchParams, animOptions, NavUtils.MEMBER_ONLY_DEAL_SEARCH)
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
        if (holder is MemberDealDestinationViewHolder) {
            val destination = listData[position - 1]
            val leadingHotel = destination.getLeadingHotel()
            if (leadingHotel != null) {
                val vm = MemberDealDestinationViewModel(context, leadingHotel, currency)
                holder.bind(vm)
            }
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

    private fun generateLoadingCells (count: Int): List<MemberDealDestination> {
        val listLoading = ArrayList<MemberDealDestination>()
        for (i in 1..count) {
            listLoading.add(MemberDealDestination())
        }
        return listLoading
    }
}
