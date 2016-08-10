package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RailViewModel
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class RailResultsAdapter(val context: Context, val legSelectedSubject: PublishSubject<RailLegOption>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val LEG_VIEW = 0

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = BehaviorSubject.create<RailSearchResponse>()

    private var legs: List<RailLegOption> = emptyList()

    init {
        resultsSubject.subscribe { response ->
            loading = false
            legs = response.legList[0].legOptionList
            notifyDataSetChanged()
        }
        loadingSubject.subscribe {
            loading = true
        }
    }

    fun isLoading(): Boolean {
        return loading
    }

    fun showLoading() {
        loadingSubject.onNext(Unit)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return legs.size
    }

    override fun getItemViewType(position: Int): Int {
        return LEG_VIEW //TODO - will add other types as we add headers/footers
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RailViewHolder -> holder.bind(legs[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rail_cell, parent, false)
        return RailViewHolder(view as ViewGroup)
    }

    inner class RailViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {
        var viewModel: RailViewModel by Delegates.notNull<RailViewModel>()

        val resources = root.resources

        val timesView: TextView by root.bindView(R.id.timesView)
        val priceView: TextView by root.bindView(R.id.priceView)
        val operatorTextView: TextView by root.bindView(R.id.trainOperator)
        val durationTextView: TextView by root.bindView(R.id.layoverView)
        val timelineView: RailResultsTimelineWidget by root.bindView(R.id.timeline_view)

        init {
            itemView.setOnClickListener(this)
            viewModel = RailViewModel(root.context)
        }

        fun bind(leg: RailLegOption) {
            viewModel.legOptionObservable.onNext(leg)

            viewModel.priceObservable.subscribeText(priceView)
            viewModel.operatorObservable.subscribeText(operatorTextView) //todo - revert
            viewModel.formattedStopsAndDurationObservable.subscribeText(durationTextView)
            timesView.text = DateTimeUtils.formatInterval(context, leg.getDepartureDateTime(), leg.getArrivalDateTime())
            timelineView.updateLeg(leg)
        }

        override fun onClick(v: View?) {
            legSelectedSubject.onNext(legs[adapterPosition])
        }
    }
}
