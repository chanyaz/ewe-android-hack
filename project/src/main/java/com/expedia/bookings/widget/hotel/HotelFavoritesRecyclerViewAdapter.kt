package com.expedia.bookings.widget.hotel

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.bookings.data.hotelshortlist.HotelShortlistFetchResponse

class HotelFavoritesRecyclerViewAdapter(private var favoritesList: ArrayList<HotelShortlistFetchResponse.HotelShortlistItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val hotelViewHolder = holder as HotelFavoritesItemViewHolder
        hotelViewHolder.bind(favoritesList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HotelFavoritesItemViewHolder.create(parent)
    }
}
