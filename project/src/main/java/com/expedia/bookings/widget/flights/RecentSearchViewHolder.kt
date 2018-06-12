package com.expedia.bookings.widget.flights

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.flights.vm.RecentSearchViewHolderViewModel
import com.larvalabs.svgandroid.widget.SVGView

class RecentSearchViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val originLocation: TextView by bindView(R.id.recent_search_origin)
    private val destinationLocation: TextView by bindView(R.id.recent_search_destination)
    private val priceTextview: TextView by bindView(R.id.recent_search_price)
    private val dateRange: DateFormatterTextView by bindView(R.id.recent_search_date)
    private val priceSubtile: TextView by bindView(R.id.recent_search_price_subtitle)
    private val travelerCount: TextView by bindView(R.id.recent_search_traveler_count)
    private val flightClass: TextView by bindView(R.id.recent_search_class)
    private val arrowIcon: SVGView by bindView(R.id.recent_search_one_way_arrow)

    val viewModel: RecentSearchViewHolderViewModel by lazy {
        val vm = RecentSearchViewHolderViewModel(context)
        vm.originObservable.subscribeText(originLocation)
        vm.destinationObservable.subscribeText(destinationLocation)
        vm.priceObservable.subscribeText(priceTextview)
        vm.dateRangeObservable.subscribe {
            dateRange.setDate(it.first, it.second)
        }
        vm.searchDateObservable.subscribeText(priceSubtile)
        vm.travelerCountObservable.subscribeText(travelerCount)
        vm.classObservable.subscribeText(flightClass)
        vm.roundTripObservable.subscribe { isRoundTrip ->
            if (isRoundTrip) {
                LayoutUtils.setSVG(arrowIcon, R.raw.flight_recent_search_round_trip)
            } else {
                LayoutUtils.setSVG(arrowIcon, R.raw.flight_recent_search_one_way)
            }
        }
        vm.contentDescriptionObservable.subscribeContentDescription(itemView)
        vm
    }
}
