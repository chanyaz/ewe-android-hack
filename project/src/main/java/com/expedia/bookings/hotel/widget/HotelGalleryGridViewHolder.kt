package com.expedia.bookings.hotel.widget

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.squareup.picasso.Picasso

class HotelGalleryGridViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
    private val imageView by lazy { root.findViewById<ImageView>(R.id.hotel_gallery_grid_imageview) }
    private val backgroundColor = ContextCompat.getColor(root.context, R.color.gray300)

    fun bind(media: HotelMedia) {
        if (media.isPlaceHolder) {
            media.loadErrorImage(imageView, callback, media.fallbackImage)
        } else {
            media.loadImage(imageView, callback, 0)
        }
    }

    private var callback: PicassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            imageView.setBackgroundColor(Color.TRANSPARENT)
            imageView.setImageBitmap(bitmap)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)

            imageView.setBackgroundColor(backgroundColor)
            imageView.setImageBitmap(null)
        }
    }

    companion object {
        fun create(parent: ViewGroup): HotelGalleryGridViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_gallery_grid_item, parent, false)
            return HotelGalleryGridViewHolder(view)
        }
    }
}
