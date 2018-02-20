package com.expedia.bookings.itin.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class FlightItinLegsDetailAdapter(val context: Context, val legsDetailList: ArrayList<FlightItinLegsDetailData>) : RecyclerView.Adapter<FlightItinLegsDetailAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        PicassoHelper.Builder(holder?.image).build().load(legsDetailList.get(position).imagePath)

        val titleText = getTitle(position)
        if (titleText.isNotEmpty()) {
            holder?.title?.visibility = View.VISIBLE
            holder?.title?.text = titleText
        }
        val subtitleText = getSubTitle(position)
        if (subtitleText.isNotEmpty()) {
            holder?.subtitle?.visibility = View.VISIBLE
            holder?.subtitle?.text = subtitleText
            holder?.subtitle?.contentDescription = getContentDescriptionForSubTitle(subtitleText)
        }
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
        var title = ""
        val departureAirportCode = legsDetailList[position].departureAirportCode
        val arrivalAirportCode = legsDetailList[position].arrivalAirportCode
        if (departureAirportCode.isNotBlank() && arrivalAirportCode.isNotBlank()) {
            title = Phrase.from(context, R.string.itin_flight_leg_detail_widget_title_TEMPLATE).put("departure", departureAirportCode).put("arrival", arrivalAirportCode).format().toString()
        }
        return title
    }

    private fun getSubTitle(position: Int): String {
        var subtitle = ""
        val departureTime = legsDetailList.get(position).departureTime
        val departureDate = legsDetailList.get(position).departureMonthDay
        val arrivalTime = legsDetailList.get(position).arrivalTime
        val stops = Integer.parseInt(legsDetailList.get(position).stopNumber)
        val numberOfStops = getNumberOfStops(position, stops)
        if (departureTime.isNotBlank() && departureDate.isNotBlank() && arrivalTime.isNotBlank()) {
            val departureDateTime = Phrase.from(context, R.string.itin_flight_leg_detail_widget_sub_title_date_time_TEMPLATE).put("date", departureDate).put("time", departureTime).format().toString()
            subtitle = Phrase.from(context, R.string.itin_flight_leg_detail_widget_sub_title_TEMPLATE).put("departure_date_time", departureDateTime).put("arrival_date_time", arrivalTime).put("stops", numberOfStops).format().toString()
            return subtitle
        }
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

    private fun getNumberOfStops(position: Int, stops: Int): String {
        if (isZero(legsDetailList.get(position).stopNumber)) {
            return if (PointOfSale.getPointOfSale().shouldShowDirectText()) {
                context.getString(R.string.itin_flight_leg_detail_widget_direct)
            } else {
                context.getString(R.string.itin_flight_leg_detail_widget_nonstop)
            }
        } else {
            return if (PointOfSale.getPointOfSale().shouldShowDirectText()) {
                context.resources.getQuantityString(R.plurals.itin_flight_leg_detail_widget_stops_with_direct_TEMPLATE, stops, stops)
            } else {
                context.resources.getQuantityString(R.plurals.itin_flight_leg_detail_widget_stops_TEMPLATE, stops, stops)
            }
        }
    }
}
