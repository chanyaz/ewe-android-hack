package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso

class HotelCompareThumbnailAdapter(private val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var thumbnailsData: ArrayList<String> = ArrayList()

    fun updateThumbnails(data: List<HotelOffersResponse>) {
        thumbnailsData = ArrayList()
        for (hotelOffer in data) {
            val photo = hotelOffer.photos[0]
            thumbnailsData.add(HotelMedia(Images.getMediaHost()
                    + photo.url, photo.displayText).getUrl(HotelMedia.Size.G))
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ThumbnailViewHolder) {
            holder.bind(thumbnailsData[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.hotel_thumbnail_compare_cell, parent, false)

        return ThumbnailViewHolder(view)
    }

    override fun getItemCount(): Int {
        return thumbnailsData.size
    }

    class ThumbnailViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView by bindView(R.id.hotel_compare_thumbnail_image)

        fun bind(url : String) {
            val background = ContextCompat.getDrawable(view.context, R.drawable.confirmation_background)

            Picasso.with(view.context)
                    .load(url)
                    .placeholder(background)
                    .error(background)
                    .into(thumbnail)
        }
    }
}