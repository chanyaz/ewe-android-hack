package com.expedia.bookings.hotel.widget

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.HotelMedia
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso

class HotelDetailGalleryViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
    private val progressBar by lazy { root.findViewById(R.id.gallery_item_progress_bar) as ProgressBar }
    private val imageView by lazy {root.findViewById(R.id.gallery_item_image_view) as ImageView }

    private lateinit var mediaItem: HotelMedia
    private var soldOut: Boolean = false
    private var itemPosition: Int = 0
    private var totalItemCount: Int = 0

    private val zeroSaturationSoldOutColorFilter: ColorMatrixColorFilter by lazy {
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f)
        ColorMatrixColorFilter(colorMatrix)
    }

    fun bind(media: HotelMedia, soldOut: Boolean, itemPosition: Int, totalItemCount: Int) {
        this.mediaItem = media
        this.itemPosition = itemPosition
        this.totalItemCount = totalItemCount
        this.soldOut = soldOut
        if (media.isPlaceHolder) {
            media.loadErrorImage(imageView, callback, media.fallbackImage)
        } else {
            media.loadImage(imageView, callback, 0)
        }
        progressBar.visibility = View.VISIBLE

        updateContDesc(useSimpleDescription = false)
    }

    fun updateContDesc(useSimpleDescription: Boolean) {
        val context = root.context
        if (useSimpleDescription) {
            itemView.contentDescription = context.getString(R.string.gallery_cont_desc)
        } else {
            val media = mediaItem
            val contDesc: String
            val imageDescription = media.description
            if (imageDescription != null) {
                contDesc = Phrase.from(context, R.string.gallery_photo_count_plus_description_cont_desc_TEMPLATE)
                        .put("index", (adapterPosition + 1).toString())
                        .put("count", totalItemCount.toString())
                        .put("api_description", imageDescription)
                        .format().toString()
            } else {
                contDesc = Phrase.from(context, R.string.gallery_photo_count_cont_desc_TEMPLATE)
                        .put("index", (adapterPosition + 1).toString())
                        .put("count", totalItemCount.toString())
                        .format().toString()
            }
            itemView.contentDescription = contDesc
        }
    }

    var callback: PicassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            imageView.setBackgroundColor(Color.TRANSPARENT)
            progressBar.visibility = View.GONE
            imageView.setImageBitmap(bitmap)
            imageView.colorFilter = if (soldOut) zeroSaturationSoldOutColorFilter else null
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
            progressBar.visibility = View.GONE
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)
            imageView.setImageBitmap(null)
        }
    }
}