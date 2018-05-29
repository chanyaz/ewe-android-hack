package com.expedia.bookings.hotel.widget.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.hotel.widget.viewholder.HotelFavoritesItemViewHolder
import io.reactivex.subjects.PublishSubject

class HotelFavoritesRecyclerViewAdapter(private var favoritesList: ArrayList<HotelShortlistItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val hotelSelectedSubject = PublishSubject.create<HotelShortlistItem>()

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val hotelViewHolder = holder as HotelFavoritesItemViewHolder
        hotelViewHolder.bind(favoritesList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = HotelFavoritesItemViewHolder.create(parent)
        holder.hotelClickedSubject.subscribe { position ->
            hotelSelected(position)
        }

        return holder
    }

    private fun hotelSelected(position: Int) {
        if (position < 0 || position >= favoritesList.size) {
            return
        }
        hotelSelectedSubject.onNext(favoritesList[position])
    }
}
