package com.expedia.hackathon

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.DownloadPrompt
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LXDataUtils
import com.mobiata.android.util.AndroidUtils

class LXCrossSellAdapter(private val lxList: List<LXActivity>) : RecyclerView.Adapter<LXCrossSellAdapter.MyViewHolder>() {

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView
        var name: TextView
        var price: TextView
//        var time: TextView

        init {
            imageView = view.findViewById(R.id.image_view) as ImageView
            name = view.findViewById(R.id.name) as TextView
            price = view.findViewById(R.id.price) as TextView
            view.setOnClickListener {
                val intent = Intent(view.context, DownloadPrompt::class.java)
                view.context.startActivity(intent)
            }
//            time = view.findViewById(R.id.time) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.lx_cross_sell_snippet, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val activity = lxList[position]

        holder.name.text = activity.title
        LXDataUtils.bindOriginalPrice(holder.view.context, activity.price, holder.price)
//        LXDataUtils.bindDuration(holder.view.context, activity.duration, activity.isMultiDuration, holder.time)

        val imageURLs = Images
                .getLXImageURLBasedOnWidth(activity.getImages(), AndroidUtils.getDisplaySize(holder.view.context).x)
        PicassoHelper.Builder(holder.imageView)
                .setPlaceholder(R.drawable.room_fallback)
                .build()
                .load(imageURLs)
    }

    override fun getItemCount(): Int {
        return lxList.size
    }
}