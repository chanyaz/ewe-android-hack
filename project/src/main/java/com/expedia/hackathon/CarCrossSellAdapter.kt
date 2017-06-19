package com.expedia.hackathon

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.cars.CategorizedCarOffers
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LXDataUtils
import com.mobiata.android.util.AndroidUtils

class CarCrossSellAdapter(private val lxList: List<CategorizedCarOffers>) : RecyclerView.Adapter<CarCrossSellAdapter.MyViewHolder>() {

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.findViewById(R.id.image_view) as ImageView
        var name: TextView = view.findViewById(R.id.name) as TextView
        var price: TextView = view.findViewById(R.id.price) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.lx_cross_sell_snippet, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val car = lxList[position]

        val vehicleInfo = car.lowestTotalPriceOffer.vehicleInfo
        holder.name.text = vehicleInfo.carCategoryDisplayLabel

        val lowestFare = car.lowestTotalPriceOffer.fare
        LXDataUtils.bindOriginalPrice(holder.view.context, lowestFare.total, holder.price)
        val url = Images.getCarRental(car.category, car.lowestTotalPriceOffer.vehicleInfo.type,
                holder.view.resources.getDimension(R.dimen.car_image_width))
        PicassoHelper.Builder(holder.imageView)
                .setPlaceholder(R.drawable.room_fallback)
                .build()
                .load(url)
    }

    override fun getItemCount(): Int {
        return lxList.size
    }
}