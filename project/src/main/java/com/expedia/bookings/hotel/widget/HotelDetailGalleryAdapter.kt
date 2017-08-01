package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import io.reactivex.subjects.PublishSubject

class HotelDetailGalleryAdapter : RecyclerView.Adapter<HotelDetailGalleryViewHolder>() {
    val galleryItemClickedSubject = PublishSubject.create<Unit>()

    private var mediaList: List<HotelMedia> = emptyList()
    private var soldOut = false

    fun setMedia(mediaList: List<HotelMedia>) {
        this.mediaList = mediaList
        notifyDataSetChanged()
    }

    fun updateSoldOut(soldOut: Boolean) {
        if (this.soldOut != soldOut) {
            this.soldOut = soldOut
            notifyItemRangeChanged(0, itemCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelDetailGalleryViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.hotel_detail_gallery_view_holder, parent, false)
        val vh = HotelDetailGalleryViewHolder(root)
        root.setOnClickListener { galleryItemClickedSubject.onNext(Unit) }
        return vh
    }

    override fun onBindViewHolder(holder: HotelDetailGalleryViewHolder?, position: Int) {
        val media = mediaList[position]
        holder?.bind(media, soldOut, position, itemCount)
    }

    override fun getItemCount(): Int {
       return mediaList.size
    }
}