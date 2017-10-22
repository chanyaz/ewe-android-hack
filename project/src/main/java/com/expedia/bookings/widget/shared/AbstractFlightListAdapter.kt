package com.expedia.bookings.widget.shared

import android.content.Context
import android.os.Handler
import android.support.annotation.UiThread
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.FlightCellWidget
import com.expedia.bookings.widget.packages.PackageBannerWidget
import com.expedia.vm.AbstractFlightViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Locale

abstract class AbstractFlightListAdapter(val context: Context, val flightSelectedSubject: PublishSubject<FlightLeg>, var isRoundTripSearch: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NUMBER_LOADING_TILES = 5
    private var loadingState = true
    protected var maxFlightDuration = 0
    protected var flights: List<FlightLeg> = emptyList()
    private var newResultsConsumed = false
    val allViewsLoadedTimeObservable = PublishSubject.create<Unit>()
    protected var isCrossSellPackageOnFSR = false

    enum class ViewTypes {
        PRICING_STRUCTURE_HEADER_VIEW,
        ALL_FLIGHTS_HEADER_VIEW,
        LOADING_FLIGHTS_VIEW,
        LOADING_FLIGHTS_HEADER_VIEW,
        PACKAGE_BANNER_VIEW,
        BEST_FLIGHT_VIEW,
        FLIGHT_CELL_VIEW
    }

    constructor(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, isRoundTripSearchSubject: BehaviorSubject<Boolean>) :
            this(context, flightSelectedSubject, isRoundTripSearchSubject.value) {
        isRoundTripSearchSubject.subscribe({ isRoundTripSearch = it })
    }

    abstract protected fun shouldAdjustPricingMessagingForAirlinePaymentMethodFee(): Boolean
    abstract protected fun showAllFlightsHeader(): Boolean
    abstract fun adjustPosition(): Int
    abstract protected fun showAdvanceSearchFilterHeader(): Boolean
    abstract protected fun isShowOnlyNonStopSearch(): Boolean
    abstract protected fun isShowOnlyRefundableSearch(): Boolean
    abstract protected fun getPriceDescriptorMessageIdForFSR(): Int?

    @UiThread
    open fun setNewFlights(flights: List<FlightLeg>) {
        loadingState = false
        newResultsConsumed = false
        maxFlightDuration = 0
        val newFlights = ArrayList(flights)
        for (flightLeg in newFlights) {
            if (flightLeg.durationHour * 60 + flightLeg.durationMinute > maxFlightDuration) {
                maxFlightDuration = flightLeg.durationHour * 60 + flightLeg.durationMinute
            }
        }
        this.flights = newFlights
        notifyDataSetChanged()
    }

    fun setLoadingState() {
        loadingState = true
        val mockFlights = ArrayList<FlightLeg>()
        val flightLeg = FlightLeg()
        var tileCount = 0
        while (tileCount < NUMBER_LOADING_TILES) {
            tileCount++
            mockFlights.add(flightLeg)
        }
        this.flights = mockFlights
        notifyDataSetChanged()
    }

    fun isLoadingState(): Boolean {
        return loadingState
    }

    override fun getItemCount(): Int {
        return flights.size + adjustPosition()
    }

    abstract fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): AbstractFlightViewModel

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is FlightViewHolder -> {
                if (!newResultsConsumed) {
                    newResultsConsumed = true
                    allViewsLoadedTimeObservable.onNext(Unit)
                }
                holder.bind(makeFlightViewModel(holder.itemView.context, flights[position - adjustPosition()]))
                selectFirstFlight(flights)
            }

            is LoadingViewHolder -> {
                val animation = AnimUtils.setupLoadingAnimation(holder.backgroundImageView, position % 2 == 0)
                holder.setAnimator(animation)
            }

            is LoadingFlightsHeaderViewHolder -> {
                holder.bind()
            }

            is HeaderViewHolder -> {
                holder.bind(isRoundTripSearch)
            }
        }
    }

    fun selectFirstFlight(allFlights: List<FlightLeg>) {
        val handler = Handler()
        val runnableCode = object : Runnable {
            override fun run() {
                flightSelectedSubject.onNext(allFlights[0])
            }
        }
        handler.postDelayed(runnableCode, 2000)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType) {
            ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_results_pricing_structure_header_cell, parent, false)
                return HeaderViewHolder(view as ViewGroup)
            }
            ViewTypes.ALL_FLIGHTS_HEADER_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.all_flights_header_cell, parent, false)
                return AllFlightsHeaderViewHolder(view as ViewGroup)
            }
            ViewTypes.LOADING_FLIGHTS_HEADER_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_results_loading_header_cell, parent, false)
                return LoadingFlightsHeaderViewHolder(view)
            }
            ViewTypes.LOADING_FLIGHTS_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_results_loading_tile_widget, parent, false)
                return LoadingViewHolder(view)
            }
            ViewTypes.PACKAGE_BANNER_VIEW.ordinal -> {
                val view = PackageBannerWidget(context)
                return PackageBannerHeaderViewHolder(view)
            }
            ViewTypes.FLIGHT_CELL_VIEW.ordinal -> {
                val view = FlightCellWidget(parent.context)
                return FlightViewHolder(view)
            }
            else -> {
                throw UnsupportedOperationException("Did not recognise the viewType")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadingState) {
            return if (position == 0) ViewTypes.LOADING_FLIGHTS_HEADER_VIEW.ordinal else ViewTypes.LOADING_FLIGHTS_VIEW.ordinal
        }

        return when (position) {
            0 -> ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal
            1 -> (if (isCrossSellPackageOnFSR) ViewTypes.PACKAGE_BANNER_VIEW.ordinal else ViewTypes.FLIGHT_CELL_VIEW.ordinal)
            else -> ViewTypes.FLIGHT_CELL_VIEW.ordinal
        }
    }

    inner class HeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val priceHeader: TextView by bindView(R.id.flight_results_price_header)
        val advanceSearchFilterHeader: TextView by bindView(R.id.flight_results_advance_search_filter_header)

        fun bind(isRoundTripSearch: Boolean) {
            val priceDescriptorInFSRMessageId = getPriceDescriptorMessageIdForFSR()
            if (priceDescriptorInFSRMessageId != null) {
                advanceSearchFilterHeader.visibility = View.GONE
                priceHeader.text = context.resources.getString(priceDescriptorInFSRMessageId)
                priceHeader.visibility = View.VISIBLE
            } else {
                val roundTripStringResId = if (shouldAdjustPricingMessagingForAirlinePaymentMethodFee()) R.string.prices_roundtrip_minimum_label else R.string.prices_roundtrip_label
                val oneWayStringResId = if (shouldAdjustPricingMessagingForAirlinePaymentMethodFee()) R.string.prices_oneway_minimum_label else R.string.prices_oneway_label
                val priceHeaderText = context.resources.getString(if (isRoundTripSearch) roundTripStringResId else oneWayStringResId)
                val advanceSearchFilterHeaderText = FlightV2Utils.getAdvanceSearchFilterHeaderString(context, isShowOnlyNonStopSearch(), isShowOnlyRefundableSearch(), priceHeaderText)
                if (showAdvanceSearchFilterHeader() && Strings.isNotEmpty(advanceSearchFilterHeaderText)) {
                    advanceSearchFilterHeader.visibility = View.VISIBLE
                    advanceSearchFilterHeader.text = advanceSearchFilterHeaderText
                    advanceSearchFilterHeader.contentDescription = advanceSearchFilterHeaderText
                    priceHeader.visibility = View.GONE
                } else {
                    advanceSearchFilterHeader.visibility = View.GONE
                    priceHeader.text = priceHeaderText
                    priceHeader.visibility = View.VISIBLE
                }
            }
        }
    }

    inner class AllFlightsHeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root)

    inner class PackageBannerHeaderViewHolder(root: ViewGroup) : RecyclerView.ViewHolder(root), View.OnClickListener {
        var packageBannerWidget = root as PackageBannerWidget

        init {
            packageBannerWidget.bind()
            packageBannerWidget.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            packageBannerWidget.navigateToPackages()
        }
    }

    inner class LoadingFlightsHeaderViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val title: TextView by bindView(R.id.title)

        fun bind() {
            val pointOfSale = PointOfSale.getPointOfSale()
            val twoLetterCountryCode = pointOfSale.twoLetterCountryCode
            val isPointOfSaleWithHundredsOfAirlines = !twoLetterCountryCode.toUpperCase(Locale.US).contains(Regex("PH|ID|KR"))
            title.text =
                    if (isPointOfSaleWithHundredsOfAirlines)
                        context.resources.getString(R.string.loading_flights_from_400_airlines)
                    else
                        context.resources.getString(R.string.loading_flights)
        }
    }

    inner open class FlightViewHolder(root: FlightCellWidget) : RecyclerView.ViewHolder(root), View.OnClickListener {
        var flightCell: FlightCellWidget

        init {
            itemView.setOnClickListener(this)
            flightCell = root
            flightCell.setMargins()
        }

        override fun onClick(view: View) {
            val flight: FlightLeg = getFlight(adapterPosition)
            flightSelectedSubject.onNext(flight)
        }

        fun bind(viewModel: AbstractFlightViewModel) {
            flightCell.bind(viewModel, maxFlightDuration)
        }
    }

    private fun getFlight(rawAdapterPosition: Int): FlightLeg {
        return flights[rawAdapterPosition - adjustPosition()]
    }
}
