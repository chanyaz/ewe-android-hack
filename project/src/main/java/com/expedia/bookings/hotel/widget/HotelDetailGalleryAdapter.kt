package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.vm.BaseHotelDetailViewModel
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelDetailGalleryAdapter : RecyclerView.Adapter<HotelDetailGalleryViewHolder>() {
    val galleryItemClickedSubject = PublishSubject.create<Unit>()

    private var mediaList: List<HotelMedia> = emptyList()
    private var dataList = ArrayList<HotelMedia>()
    private var filter: List<String> = emptyList()
    private var soldOut = false

    fun setMedia(mediaList: List<HotelMedia>) {
        this.mediaList = mediaList
        setFilters(emptyList())
    }

    fun setFilters(filters: List<String>){
        this.filter = filters
        updateDataListBasedOnFilters()
    }

    private fun updateDataListBasedOnFilters(){
        dataList.clear()

        if(filter.isEmpty()){
            dataList.addAll(mediaList)
        } else{
            for (hotel : HotelMedia in mediaList){
                if(filter.contains(hotel.description)){
                    dataList.add(hotel)
                }
            }
        }
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
        val media = dataList[position]
        holder?.bind(media, soldOut, position, itemCount)
    }

    override fun getItemCount(): Int {
       return dataList.size
    }
}