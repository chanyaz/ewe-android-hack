package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.widget.holder.HotelCompareCellHolder
import rx.subjects.PublishSubject

class HotelCompareAdapter(private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSelectedSubject = PublishSubject.create<String>()

    private val hotels : ArrayList<HotelOffersResponse> = ArrayList()

    fun addHotel(offer: HotelOffersResponse) {
        hotels.add(offer)
        notifyItemInserted(hotels.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.hotel_detailed_compare_cell, parent, false)

        return HotelCompareCellHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is HotelCompareCellHolder) {
            holder.bind(hotels[position])
            holder.selectRoomClickSubject.subscribe {
                hotelSelectedSubject.onNext(hotels[holder.adapterPosition].hotelId)
            }
        }
    }

    override fun getItemCount(): Int {
        return hotels.size
    }
}