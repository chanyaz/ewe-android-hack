package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.squareup.picasso.Picasso

class HotelGalleryGridViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
    private val imageView by lazy { root.findViewById<ImageView>(R.id.hotel_gallery_grid_imageview) }

    fun bind(media: HotelMedia, lowMemoryMode: Boolean) {
        val mediaSize = if (lowMemoryMode) HotelMedia.Size.SMALL else HotelMedia.Size.getIdealGridSize()
        Picasso.with(root.context)
                .load(media.getUrl(mediaSize))
                .placeholder(R.color.gallery_grid_placeholder_color)
                .into(imageView)
    }

    companion object {
        fun create(parent: ViewGroup): HotelGalleryGridViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_gallery_grid_item, parent, false)
            return HotelGalleryGridViewHolder(view)
        }
    }
}
