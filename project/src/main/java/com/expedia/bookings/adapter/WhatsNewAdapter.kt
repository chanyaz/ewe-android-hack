package com.expedia.bookings.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.widget.HotelReviewRowView
import com.expedia.bookings.widget.HotelReviewsLoadingWidget
import com.expedia.bookings.widget.HotelReviewsRecyclerView
import com.expedia.bookings.widget.HotelReviewsSummaryWidget
import com.expedia.model.CustomPojo
import java.util.ArrayList
import java.util.zip.Inflater

/**
 * Created by nbirla on 20/11/17.
 */

class WhatsNewAdapter : RecyclerView.Adapter<WhatsNewAdapter.WhatsNewViewHolder>() {

    val VIEW_TYPE_DATE = 0
    val VIEW_TYPE_HEADER = 1
    val VIEW_TYPE_DESC = 2

    private var data: ArrayList<CustomPojo> = arrayListOf()
    private var datePos: ArrayList<Int> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): WhatsNewViewHolder {
        val view: View
        when (viewType) {
            VIEW_TYPE_DATE -> view = View.inflate(parent!!.context, R.layout.row_item_date, null)
            VIEW_TYPE_HEADER -> view = View.inflate(parent!!.context, R.layout.row_item_header, null)
            else -> view = View.inflate(parent!!.context, R.layout.row_item, null)
        }
        return WhatsNewViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhatsNewViewHolder?, position: Int) {
        val viewType = getItemViewType(position)
        when(viewType){
            VIEW_TYPE_DATE -> {
                var pos = datePos.indexOf(position)
                val tv = holder!!.itemView.findViewById<TextView>(R.id.label12)

                tv.text = data.get(pos).monthAndYear
            }
            VIEW_TYPE_HEADER -> {
                var lastDatePos = 0

                for(item in datePos){
                    if(item < position){
                        lastDatePos = item
                    }
                }
                var pos = datePos.indexOf(lastDatePos)
                var item = data.get(pos)

                var diff = position - lastDatePos
                diff = diff/2
                val tv = holder!!.itemView.findViewById<TextView>(R.id.label12)

                tv.text = item.featureList.get(diff).featureName

            } else -> {
                val tv = holder!!.itemView.findViewById<TextView>(R.id.label_desc)
                val sep = holder!!.itemView.findViewById<LinearLayout>(R.id.vertical_sep)
                var lastDatePos = 0

                for(item in datePos){
                    if(item < position){
                        lastDatePos = item
                    }
                }
                var pos = datePos.indexOf(lastDatePos)
                var item = data.get(pos)

                var diff = position/2 - lastDatePos
                diff = diff/2
                tv.text = item.featureList.get(diff).featureDetails

                if(position == itemCount - 1){
                    sep.visibility = View.GONE
                } else{
                    sep.visibility = View.VISIBLE
                }
            }
        }
    }

    fun addData(addedReviews: List<CustomPojo>) {
        data.clear()
        data.addAll(addedReviews)

        var pos = 0;

        for(item in data){
            datePos.add(pos)

            pos = pos + item.featureList.size * 2 + 1
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        var nof = 0
        for(item in data){
            nof = nof + item.featureList.size
        }
        return data.size + 2*nof
    }

    override fun getItemViewType(position: Int): Int {

        if(datePos.contains(position))
            return VIEW_TYPE_DATE
        else{
            var lastDatePos = 0

            for(item in datePos){
                if(item < position){
                    lastDatePos = item
                }
            }
            var diff = position - lastDatePos
            if(diff %2 == 0) return VIEW_TYPE_DESC
            else return VIEW_TYPE_HEADER
        }
    }

    class WhatsNewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

