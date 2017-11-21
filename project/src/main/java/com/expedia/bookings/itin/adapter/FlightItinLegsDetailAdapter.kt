package com.expedia.bookings.itin.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase


class FlightItinLegsDetailAdapter(val context: Context, val legsDetailList: ArrayList<FlightItinLegsDetailData>) : RecyclerView.Adapter<FlightItinLegsDetailAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        PicassoHelper.Builder(holder?.image).build().load(legsDetailList.get(position).imagePath)
        holder?.title?.text = getTitle(position)
        val subtitleText = getSubTitle(position)
        holder?.subtitle?.text = subtitleText
        holder?.subtitle?.contentDescription = getContentDescriptionForSubTitle(subtitleText)
        if (position == legsDetailList.size - 1)
            holder?.divider?.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return legsDetailList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context)
                .inflate(R.layout.flight_itin_leg_view, parent, false)
        return ViewHolder(itemView)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView by bindView<ImageView>(R.id.flight_itin_leg_view_icon)
        val title: TextView by bindView<TextView>(R.id.flight_itin_leg_view_title)
        val subtitle: TextView by bindView<TextView>(R.id.flight_itin_leg_view_subtitle)
        val divider: View by bindView<View>(R.id.flight_itin_leg_view_divider_line)
    }

    private fun getTitle(position: Int): String {
        val departureAirportCode = legsDetailList.get(position).departureAirportCode
        val arrivalAirportCode = legsDetailList.get(position).arrivalAirportCode
        val title = Phrase.from(context, R.string.itin_flight_leg_detail_widget_title_TEMPLATE).put("departure", departureAirportCode).put("arrival", arrivalAirportCode).format().toString()
        return title
    }

    private fun getSubTitle(position: Int): String {
        val departureTime = legsDetailList.get(position).departureTime
        val departureDate = legsDetailList.get(position).departureMonthDay
        val arrivalTime = legsDetailList.get(position).arrivalTime
        val stops = Integer.parseInt(legsDetailList.get(position).stopNumber)
        val numberOfStops = if (isZero(legsDetailList.get(position).stopNumber)) context.getString(R.string.itin_flight_leg_detail_widget_nonstop) else context.resources.getQuantityString(R.plurals.itin_flight_leg_detail_widget_stops_TEMPLATE, stops, stops)
        val departureDateTime = Phrase.from(context, R.string.itin_flight_leg_detail_widget_sub_title_date_time_TEMPLATE).put("date", departureDate).put("time", departureTime).format().toString()
        val subtitle = Phrase.from(context, R.string.itin_flight_leg_detail_widget_sub_title_TEMPLATE).put("departure_date_time", departureDateTime).put("arrival_date_time", arrivalTime).put("stops", numberOfStops).format().toString()
        return subtitle
    }

    private fun isZero(value: String?): Boolean {
        if (value != null && value.isNotEmpty()) {
            try {
                return if (Integer.parseInt(value) == 0) true else false
            } catch (ex: NumberFormatException) {
                return false
            }
        }
        return false
    }

    private fun getContentDescriptionForSubTitle(subtitleText: String): String {
        return subtitleText.replace(context.getString(R.string.itin_flight_leg_detail_widget_hyphen_text), context.getString(R.string.itin_flight_leg_detail_widget_to_text))
    }

}