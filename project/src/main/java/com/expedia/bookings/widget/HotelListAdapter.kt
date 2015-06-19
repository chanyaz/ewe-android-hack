package com.expedia.bookings.widget

import android.content.res.Resources
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.Images
import java.util.ArrayList
import kotlin.properties.Delegates

public  class HotelListAdapter(data : List<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val hotels = data

    override fun getItemCount(): Int {
       return hotels.size()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val holder : HotelViewHolder = holder as HotelViewHolder
        holder.bind(hotels.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_cell, parent, false)
        return HotelViewHolder(view, parent)
    }

    public class HotelViewHolder(view: View, parentView: ViewGroup) : RecyclerView.ViewHolder(view),  HeaderBitmapDrawable.CallbackListener {
        val PICASSO_TAG = "HOTEL_RESULTS_LIST"

        val parent = parentView

        val resources: Resources by Delegates.lazy {
            itemView.getResources()
        }

        val imageView: ImageView by Delegates.lazy {
            view.findViewById(R.id.background) as ImageView
        }

        val hotelName: TextView by Delegates.lazy {
            view.findViewById(R.id.hotel_name_text_view) as TextView
        }

        val pricePerNight: TextView by Delegates.lazy {
            view.findViewById(R.id.price_per_night) as TextView
        }

        val strikeThroughPricePerNight: TextView by Delegates.lazy {
            view.findViewById(R.id.strike_through_price) as TextView
        }

        val guestRatingPercentage: TextView by Delegates.lazy {
            view.findViewById(R.id.guest_rating_percentage) as TextView
        }

        val topAmenityTitle: TextView by Delegates.lazy {
            view.findViewById(R.id.top_amenity_title) as TextView
        }

        val starRating: RatingBar by Delegates.lazy {
            view.findViewById(R.id.hotel_rating_bar) as RatingBar
        }

        public fun bind(hotel : Hotel) {
            val url = Images.getNearbyHotelImage(hotel)
            val drawable = Images.makeHotelBitmapDrawable(itemView.getContext(), this, parent.getWidth(), url, PICASSO_TAG)
            imageView.setImageDrawable(drawable)
            hotelName.setText(hotel.name)

            pricePerNight.setText(resources.getString(R.string.per_nt_TEMPLATE, hotel.lowRateInfo.nightlyRateTotal.toString()))
            guestRatingPercentage.setText(resources.getString(R.string.customer_rating_percent, hotel.percentRecommended.toInt()))
            topAmenityTitle.setText(if (hotel.hasFreeCancellation)  resources.getString(R.string.free_cancellation) else "")
            strikeThroughPricePerNight.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
            starRating.setRating(hotel.hotelStarRating)
        }

        override fun onBitmapLoaded() {

        }

        override fun onBitmapFailed() {

        }

        override fun onPrepareLoad() {

        }
    }

}