package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.KrazyglueResponse
import rx.subjects.PublishSubject

class KrazyglueHotelsListAdapter(hotelsObservable: PublishSubject<List<KrazyglueResponse.KrazyglueHotel>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val LOADING_VIEW = 0
    val KRAZYGLUE_HOTEL_VIEW = 1

    var loading = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is KrazyglueHotelViewHolder -> holder.viewModel.hotelObservable.onNext(hotels[position])
        }
    }

    var hotels = arrayListOf<KrazyglueResponse.KrazyglueHotel>()

    init {
        hotelsObservable.subscribe { newHotels ->
            loading = false
            hotels.clear()
            hotels.addAll(newHotels)
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loading) {
            return LOADING_VIEW
        } else {
            return KRAZYGLUE_HOTEL_VIEW
        }
    }

    override fun getItemCount(): Int {
        if (loading) {
            return 3
        }

        return hotels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == LOADING_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_placeholder_hotel_cell
                    , parent, false)
            return KrazyglueLoadingViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_hotel_view, parent, false)
            return KrazyglueHotelViewHolder(view as ViewGroup)
        }
    }

}
