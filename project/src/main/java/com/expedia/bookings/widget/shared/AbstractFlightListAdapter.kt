package com.expedia.bookings.widget.shared

import android.content.Context
import android.support.annotation.UiThread
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.FlightAirlineWidget
import com.expedia.bookings.widget.packages.FlightLayoverWidget
import com.expedia.vm.AbstractFlightViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Locale

abstract class AbstractFlightListAdapter(val context: Context, val flightSelectedSubject: PublishSubject<FlightLeg>, var isRoundTripSearch: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NUMBER_LOADING_TILES = 5
    private var loadingState = true
    private var maxFlightDuration = 0
    protected var flights: List<FlightLeg> = emptyList()

    enum class ViewTypes {
        PRICING_STRUCTURE_HEADER_VIEW,
        ALL_FLIGHTS_HEADER_VIEW,
        LOADING_FLIGHTS_VIEW,
        LOADING_FLIGHTS_HEADER_VIEW,
        BEST_FLIGHT_VIEW,
        FLIGHT_CELL_VIEW
    }

    constructor(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, isRoundTripSearchSubject: BehaviorSubject<Boolean>): this(context, flightSelectedSubject, true) {
        isRoundTripSearchSubject.subscribe({ isRoundTripSearch = it })
    }

    abstract protected fun shouldAdjustPricingMessagingForAirlinePaymentMethodFee(): Boolean
    abstract protected fun showAllFlightsHeader(): Boolean
    abstract fun adjustPosition(): Int

    @UiThread
    open fun setNewFlights(flights: List<FlightLeg>) {
        loadingState = false
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

    override fun getItemCount(): Int {
        return flights.size + adjustPosition()
    }

    abstract fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): AbstractFlightViewModel

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is FlightViewHolder -> {
                holder.bind(makeFlightViewModel(holder.itemView.context, flights[position - adjustPosition()]))
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
            ViewTypes.LOADING_FLIGHTS_VIEW.ordinal-> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_results_loading_tile_widget, parent, false)
                return LoadingViewHolder(view)
            }
            ViewTypes.FLIGHT_CELL_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_cell, parent, false)
                return FlightViewHolder(view as ViewGroup, parent.width)
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
            else -> ViewTypes.FLIGHT_CELL_VIEW.ordinal
        }
    }

    inner class HeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        val title: TextView by bindView(R.id.flight_results_price_header)

        fun bind(isRoundTripSearch: Boolean) {
            val roundTripStringResId = if (shouldAdjustPricingMessagingForAirlinePaymentMethodFee()) R.string.prices_roundtrip_minimum_label else R.string.prices_roundtrip_label
            val oneWayStringResId = if (shouldAdjustPricingMessagingForAirlinePaymentMethodFee()) R.string.prices_oneway_minimum_label else R.string.prices_oneway_label
            title.text = context.resources.getText(if (isRoundTripSearch) roundTripStringResId else oneWayStringResId)
        }
    }

    inner class AllFlightsHeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root)

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

    inner open class FlightViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val cardView: CardView by root.bindView(R.id.card_view)
        val flightTimeTextView: TextView by root.bindView(R.id.flight_time_detail_text_view)
        val priceTextView: TextView by root.bindView(R.id.price_text_view)
        val flightDurationTextView: TextView by root.bindView(R.id.flight_duration_text_view)
        val flightLayoverWidget: FlightLayoverWidget by root.bindView(R.id.custom_flight_layover_widget)
        val flightAirlineWidget: FlightAirlineWidget by root.bindView(R.id.flight_airline_widget)
        val bestFlightView: ViewGroup by root.bindView(R.id.package_best_flight)

        init {
            itemView.setOnClickListener(this)
            bestFlightView.visibility = View.GONE
        }

        override fun onClick(view: View) {
            val flight: FlightLeg = getFlight(adapterPosition)
            flightSelectedSubject.onNext(flight)
        }

        fun bind(viewModel: AbstractFlightViewModel) {
            flightTimeTextView.text = viewModel.flightTime
            priceTextView.text = viewModel.price()
            flightDurationTextView.text = viewModel.duration
            val flight = viewModel.layover
            flightLayoverWidget.update(flight.flightSegments, flight.durationHour, flight.durationMinute, maxFlightDuration)
            flightAirlineWidget.update(viewModel.airline)
            cardView.contentDescription = viewModel.contentDescription
        }
    }

    private fun getFlight(rawAdapterPosition: Int): FlightLeg {
        return flights[rawAdapterPosition - adjustPosition()]
    }
}
