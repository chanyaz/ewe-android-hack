package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class HotelPoiDistanceAdapter(val context: Context) : RecyclerView.Adapter<HotelPoiDistanceAdapter.POIViewHolder>() {
    class POIDataItem(val poiName: String, val poiDistance: String, val drawableRes: Int? = null)

    private var data = emptyList<POIDataItem>()

    fun updateData(data: List<POIDataItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: POIViewHolder, position: Int) {
        val item = data[position]
        val distanceText = Phrase.from(context, R.string.poi_distance_TEMPLATE)
                .put("distance", item.poiDistance)
                .format().toString()
        holder.bind(item.poiName, distanceText, item.drawableRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POIViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_poi_distance_item, parent, false)
        return POIViewHolder(view)
    }

    class POIViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val poiIcon by lazy { root.findViewById<ImageView>(R.id.poi_item_icon) }
        private val poiName by lazy { root.findViewById<TextView>(R.id.poi_name) }
        private val poiDistance by lazy { root.findViewById<TextView>(R.id.poi_distance) }

        fun bind(name: String, distance: String, drawableRes: Int? = null) {
            drawableRes?.let { poiIcon.setImageResource(it) }
            poiName.text = name
            poiDistance.text = distance
        }
    }
}
