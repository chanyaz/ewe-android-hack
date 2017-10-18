package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import rx.subjects.PublishSubject

class KrazyglueWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    fun isWidgetEnabled() = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightsKrazyglue, R.string.preference_enable_krazy_glue_on_flights_confirmation)

    val viewModel by lazy {
        val krazyGlueWidgetViewModel = KrazyglueWidgetViewModel(context)
        krazyGlueWidgetViewModel.hotelsObservable.subscribe {
            if (it.isEmpty() || !isWidgetEnabled()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
            }
        }
        krazyGlueWidgetViewModel
    }
    val headerText: TextView by bindView(R.id.header_text_view)
    val hotelsRecyclerView: KrazyglueHotelRecyclerView by bindView(R.id.hotels_recycler_view)

    //TODO DUMMY_CITY and DUMMY_HOTELS need to be replaced when plugging in real data
    private val DUMMY_CITY = "San Francisco"
    private val DUMMY_HOTELS = arrayListOf(getDummyHotel(1), getDummyHotel(2), getDummyHotel(3))

    private fun getDummyHotel(index: Int): Hotel {
        val hotel = Hotel()
        hotel.address = "America"
        hotel.airportCode = "AME"
        hotel.city = "City"
        hotel.localizedName = "Hotel with ${index} stars"
        return hotel
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isWidgetEnabled()) {
            visibility = View.GONE
        } else {
            hotelsRecyclerView.adapter = KrazyglueHotelsListAdapter(viewModel.hotelsObservable)
            hotelsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

            viewModel.headerTextObservable.subscribeText(headerText)
            viewModel.cityObservable.onNext(DUMMY_CITY)
            viewModel.hotelsObservable.onNext(DUMMY_HOTELS)
        }
    }
}