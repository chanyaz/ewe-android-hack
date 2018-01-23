package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isKrazyglueOnFlightsConfirmationEnabled
import com.expedia.util.subscribeText
import android.support.v7.widget.PagerSnapHelper

class KrazyglueWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    fun isWidgetEnabled() = isKrazyglueOnFlightsConfirmationEnabled(context)

    val viewModel by lazy {
        val krazyglueWidgetViewModel = KrazyglueWidgetViewModel(context)
        krazyglueWidgetViewModel.hotelsObservable.subscribe {
            if (it.isEmpty() || !isWidgetEnabled()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                FlightsV2Tracking.trackKrazyglueExposure(it)
            }
        }
        krazyglueWidgetViewModel
    }
    val headerText: TextView by bindView(R.id.header_text_view)
    val hotelsRecyclerView: KrazyglueHotelRecyclerView by bindView(R.id.hotels_recycler_view)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isWidgetEnabled()) {
            visibility = View.GONE
        } else {
            hotelsRecyclerView.adapter = KrazyglueHotelsListAdapter(viewModel.hotelsObservable, viewModel.hotelSearchParamsObservable, viewModel.regionIdObservable, context)
            hotelsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(hotelsRecyclerView)
            viewModel.headerTextObservable.subscribeText(headerText)
        }
    }
}
