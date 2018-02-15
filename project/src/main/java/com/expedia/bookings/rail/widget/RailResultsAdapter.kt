package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.TextView
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class RailResultsAdapter(val context: Context, val legSelectedSubject: PublishSubject<RailLegOption>, val inbound: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var loading = true
    val loadingSubject = BehaviorSubject.create<Unit>()
    val legOptionsAndCompareToPriceSubject = BehaviorSubject.create<Pair<List<RailLegOption>, Money?>>()
    val outboundOfferSubject = BehaviorSubject.create<Optional<RailOffer>>()

    val directionHeaderSubject = BehaviorSubject.create<CharSequence>()
    val priceHeaderSubject = BehaviorSubject.create<CharSequence>()
    private val numHeaderItemsInRailsList = 1
    private val NUMBER_LOADING_TILES = 5
    private var legOptions: List<RailLegOption> = emptyList()
    private var cheapestCompareToPrice: Money? = null
    private var selectedOutboundOffer: RailOffer? = null

    enum class ViewTypes {
        RESULTS_HEADER_VIEW,
        RAIL_CELL_VIEW,
        LOADING_VIEW
    }

    init {
        ObservableOld.combineLatest(legOptionsAndCompareToPriceSubject, outboundOfferSubject, { pair, offer ->
            loading = false
            legOptions = pair.first
            cheapestCompareToPrice = pair.second
            selectedOutboundOffer = offer.value
            notifyDataSetChanged()
        }).subscribe()

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
        this.legOptions = mockLegOptions
    }

    override fun getItemCount(): Int {
        return legOptions.size + numHeaderItemsInRailsList
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RailViewHolder -> holder.bind(legOptions[position - numHeaderItemsInRailsList])
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
        val direction: TextView by bindView(R.id.selectDirection)
        val oneWayOrRoundTrip: TextView by bindView(R.id.oneWayOrRoundTrip)

        init {
            directionHeaderSubject.subscribeText(direction)
            priceHeaderSubject.subscribeText(oneWayOrRoundTrip)
        }
    }

    inner class RailViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {
        var viewModel: RailLegOptionViewModel by notNullAndObservable { vm ->
            vm.priceObservable.subscribeText(priceView)
            vm.formattedStopsAndDurationObservable.subscribeText(durationTextView)
            vm.railCardAppliedObservable.subscribeVisibility(railCardImage)

            vm.formattedTimeSubject.subscribeText(timesView)
            vm.aggregatedOperatingCarrierSubject.subscribeText(operatorTextView)
            vm.legOptionObservable.subscribe { legOption ->
                timelineView.updateLeg(legOption)
            }

            vm.contentDescriptionObservable.subscribe { content ->
                cardView.contentDescription = content
            }
        }

        val resources = root.resources

        val cardView: CardView by bindView(R.id.rail_card_view)
        val timesView: TextView by bindView(R.id.timesView)
        val priceView: TextView by bindView(R.id.priceView)
        val operatorTextView: TextView by bindView(R.id.trainOperator)
        val durationTextView: TextView by bindView(R.id.layoverView)
        val timelineView: RailResultsTimelineWidget by bindView(R.id.timeline_view)
        val railCardImage: ImageView by bindView(R.id.rail_card_image)

        init {
            itemView.setOnClickListener(this)
            viewModel = RailLegOptionViewModel(root.context, inbound)
        }

        fun bind(legOption: RailLegOption) {
            viewModel.legOptionObservable.onNext(legOption)
            viewModel.cheapestLegPriceObservable.onNext(Optional(cheapestCompareToPrice))
            viewModel.offerSubject.onNext(Optional(selectedOutboundOffer))
        }

        override fun onClick(v: View?) {
            legSelectedSubject.onNext(legOptions[adapterPosition - numHeaderItemsInRailsList])
        }
    }
}
