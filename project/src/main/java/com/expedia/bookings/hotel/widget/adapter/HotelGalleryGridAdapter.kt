package com.expedia.bookings.hotel.widget.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.widget.HotelGalleryGridViewHolder
import io.reactivex.subjects.PublishSubject

class HotelGalleryGridAdapter(var lowMemoryMode: Boolean = false) : RecyclerView.Adapter<HotelGalleryGridViewHolder>() {
    val selectedImagePosition = PublishSubject.create<Int>()

    private var mediaList: List<HotelMedia> = emptyList()

    fun setMedia(mediaList: List<HotelMedia>) {
        this.mediaList = mediaList
        notifyDataSetChanged()
    }

    fun forceLowMemory() {
        this.lowMemoryMode = true
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun onBindViewHolder(holder: HotelGalleryGridViewHolder, position: Int) {
        holder.bind(mediaList[position], lowMemoryMode)
        holder.root.setOnClickListener {
            selectedImagePosition.onNext(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelGalleryGridViewHolder {
        val viewHolder = HotelGalleryGridViewHolder.create(parent)
        return viewHolder
    }
}
