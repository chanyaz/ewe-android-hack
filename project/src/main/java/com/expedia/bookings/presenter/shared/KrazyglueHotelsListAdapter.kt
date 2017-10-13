package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import rx.subjects.PublishSubject

class KrazyglueHotelsListAdapter(hotelsObservable: PublishSubject<List<Hotel>>) : RecyclerView.Adapter<KrazyglueHotelViewHolder>() {
    override fun onBindViewHolder(holder: KrazyglueHotelViewHolder, position: Int) {
        holder.bindData(hotels[position])
    }

    var hotels = arrayListOf<Hotel>()

    init {
        hotelsObservable.subscribe { newHotels ->
            hotels.clear()
            hotels.addAll(newHotels)
            notifyDataSetChanged()
        }
    }


    override fun getItemCount(): Int {
        return hotels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KrazyglueHotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_hotel_view, parent, false)
        return KrazyglueHotelViewHolder(view as ViewGroup)
    }

}
