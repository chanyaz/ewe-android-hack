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
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

public class HotelListAdapter(val hotels: List<Hotel>, val hotelSubject: PublishSubject<Hotel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return hotels.size()
    }

    override fun onBindViewHolder(given: RecyclerView.ViewHolder?, position: Int) {
        val holder: HotelViewHolder = given as HotelViewHolder
        holder.bind(hotels.get(position))
        holder.itemView.setOnClickListener(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_cell, parent, false)
        return HotelViewHolder(view as ViewGroup, parent.getWidth())
    }

    public inner class HotelViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), HeaderBitmapDrawable.CallbackListener, View.OnClickListener {

        val PICASSO_TAG = "HOTEL_RESULTS_LIST"

        val resources: Resources by Delegates.lazy {
            itemView.getResources()
        }

        val imageView: ImageView by root.bindView(R.id.background)
        val hotelName: TextView by root.bindView(R.id.hotel_name_text_view)
        val pricePerNight: TextView by root.bindView(R.id.price_per_night)
        val strikeThroughPricePerNight: TextView by root.bindView(R.id.strike_through_price)
        val guestRatingPercentage: TextView by root.bindView(R.id.guest_rating_percentage)
        val topAmenityTitle: TextView by root.bindView(R.id.top_amenity_title)
        val starRating: RatingBar by root.bindView(R.id.hotel_rating_bar)

        public fun bind(hotel: Hotel) {
            val url = Images.getNearbyHotelImage(hotel)
            val drawable = Images.makeHotelBitmapDrawable(itemView.getContext(), this, width, url, PICASSO_TAG)
            imageView.setImageDrawable(drawable)
            hotelName.setText(hotel.name)

            pricePerNight.setText(Phrase.from(resources, R.string.per_nt_TEMPLATE).put("price", hotel.lowRateInfo.nightlyRateTotal.toString()).format())
            guestRatingPercentage.setText(Phrase.from(resources, R.string.customer_rating_percent_Template).put("rating", hotel.percentRecommended.toInt()).put("percent", "%").format())
            topAmenityTitle.setText(if (hotel.hasFreeCancellation) resources.getString(R.string.free_cancellation) else "")
            strikeThroughPricePerNight.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
            starRating.setRating(hotel.hotelStarRating)
        }

        override fun onClick(view: View) {
            val hotel: Hotel = hotels.get(getAdapterPosition())
            hotelSubject.onNext(hotel)
        }

        override fun onBitmapLoaded() {

        }

        override fun onBitmapFailed() {

        }

        override fun onPrepareLoad() {

        }
    }

}
