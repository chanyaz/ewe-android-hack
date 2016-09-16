package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.RailViewModel
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.mobiata.flightlib.utils.DateTimeUtils
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.properties.Delegates

class RailResultsAdapter(val context: Context, val legSelectedSubject: PublishSubject<RailLegOption>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val resultsSubject = BehaviorSubject.create<RailSearchResponse>()
    val directionHeaderSubject = BehaviorSubject.create<CharSequence>()
    val priceHeaderSubject = BehaviorSubject.create<CharSequence>()
    private val NUMBER_LOADING_TILES = 5

    private var legs: List<RailLegOption> = emptyList()

    enum class ViewTypes {
        RESULTS_HEADER_VIEW,
        RAIL_CELL_VIEW,
        LOADING_VIEW
    }

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

    fun showLoading() {
        loadingSubject.onNext(Unit)
        loadMockLegs()
        notifyDataSetChanged()
    }

    private fun loadMockLegs() {
        val mockLegOptions = ArrayList<RailLegOption>()
        for (tileCount in 0..NUMBER_LOADING_TILES) {
            mockLegOptions.add(RailLegOption())
        }
        this.legs = mockLegOptions
    }

    override fun getItemCount(): Int {
        return legs.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RailViewHolder -> holder.bind(legs[position])
            is LoadingViewHolder -> holder.setAnimator(AnimUtils.setupLoadingAnimation(holder.backgroundImageView, position % 2 == 0))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType) {
            ViewTypes.RESULTS_HEADER_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_header, parent, false)
                return HeaderViewHolder(view as ViewGroup)
            }
            ViewTypes.LOADING_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.search_results_loading_tile_widget, parent, false)
                return LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rail_cell, parent, false)
                return RailViewHolder(view as ViewGroup)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return ViewTypes.RESULTS_HEADER_VIEW.ordinal
        } else if (loading) {
            return ViewTypes.LOADING_VIEW.ordinal
        } else {
            return ViewTypes.RAIL_CELL_VIEW.ordinal
        }
    }

    inner class HeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val direction: TextView by root.bindView(R.id.selectDirection)
        val oneWayOrRoundTrip: TextView by root.bindView(R.id.oneWayOrRoundTrip)

        init {
            directionHeaderSubject.subscribeText(direction)
            priceHeaderSubject.subscribeText(oneWayOrRoundTrip)
        }
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
            viewModel.formattedStopsAndDurationObservable.subscribeText(durationTextView)
            timesView.text = DateTimeUtils.formatInterval(context, leg.getDepartureDateTime(), leg.getArrivalDateTime())
            operatorTextView.text = leg.aggregatedOperatingCarrier
            timelineView.updateLeg(leg)
        }

        override fun onClick(v: View?) {
            legSelectedSubject.onNext(legs[adapterPosition])
        }
    }
}
