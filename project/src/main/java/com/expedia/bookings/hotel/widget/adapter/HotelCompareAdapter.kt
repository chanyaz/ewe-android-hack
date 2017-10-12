package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.bindView

class HotelCompareAdapter(private val context: Context)  : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class Type {
        HEADER,
        LOADING_VIEW,
        HOTEL_INFO
    }

    private var listData: List<HotelOffersResponse> = emptyList()

    fun updateHotels(list: List<HotelOffersResponse>) {
        this.listData = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return Type.HEADER.ordinal
        }
        else {
            return Type.HOTEL_INFO.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HotelCompareAdapter.Type.HEADER.ordinal) {
            val view = LayoutInflater.from(context).inflate(R.layout.hotel_compare_list_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.hotel_compare_list_info_cell, parent, false)
            //todo add click functionality
            return  HotelInfoHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is HotelInfoHolder) {
            val hotelInfo = listData[position - 1]
            if (hotelInfo != null) {
                holder.bind(hotelInfo)
            }
        }
    }


    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

    class HotelInfoHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hotelName: TextView by bindView(R.id.hotel_info_name)
        val guestRating: TextView by bindView(R.id.hotel_info_guest_rating)

        fun bind(offersResponse: HotelOffersResponse) {
            hotelName.text = offersResponse.hotelName
            guestRating.text = offersResponse.hotelGuestRating.toString()
        }
    }
}