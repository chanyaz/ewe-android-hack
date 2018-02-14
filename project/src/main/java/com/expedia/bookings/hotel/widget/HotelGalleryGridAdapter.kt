package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.bookings.data.HotelMedia
import io.reactivex.subjects.PublishSubject

class HotelGalleryGridAdapter : RecyclerView.Adapter<HotelGalleryGridViewHolder>() {
    val imageAtPositionSelected = PublishSubject.create<Int>()

    private var mediaList: List<HotelMedia> = emptyList()

    fun setMedia(mediaList: List<HotelMedia>) {
        this.mediaList = mediaList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun onBindViewHolder(holder: HotelGalleryGridViewHolder, position: Int) {
        holder.bind(mediaList[position])
        holder.root.setOnClickListener {
            imageAtPositionSelected.onNext(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelGalleryGridViewHolder {
        val viewHolder = HotelGalleryGridViewHolder.create(parent)
        return viewHolder
    }
}