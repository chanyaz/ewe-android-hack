package com.expedia.bookings.itin.flight.manageBooking

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class FlightItinLegsDetailAdapter(val context: Context, val legsDetailList: ArrayList<FlightItinLegsDetailData>) : RecyclerView.Adapter<FlightItinLegsDetailAdapter.FlightItinLegsDetailViewHolder>() {

    override fun onBindViewHolder(holder: FlightItinLegsDetailViewHolder, position: Int) {
        PicassoHelper.Builder(holder.image).build().load(legsDetailList[position].imagePath)

        val titleText = getTitle(position)
        if (titleText.isNotEmpty()) {
            holder.title.visibility = View.VISIBLE
            holder.title.text = titleText
        }
        val subtitleText = getSubTitle(position)
        if (subtitleText.isNotEmpty()) {
            holder.subtitle.visibility = View.VISIBLE
            holder.subtitle.text = subtitleText
            holder.subtitle.contentDescription = getContentDescriptionForSubTitle(subtitleText)
        }
        if (position == legsDetailList.size - 1)
            holder.divider.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return legsDetailList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightItinLegsDetailViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.flight_itin_leg_view, parent, false)
        return FlightItinLegsDetailViewHolder(itemView)
    }

    inner class FlightItinLegsDetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView by bindView(R.id.flight_itin_leg_view_icon)
        val title: TextView by bindView(R.id.flight_itin_leg_view_title)
        val subtitle: TextView by bindView(R.id.flight_itin_leg_view_subtitle)
        val divider: View by bindView(R.id.flight_itin_leg_view_divider_line)
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
        val departureTime = legsDetailList[position].departureTime
        val departureDate = legsDetailList[position].departureMonthDay
        val arrivalTime = legsDetailList[position].arrivalTime
        val stops = Integer.parseInt(legsDetailList[position].stopNumber)
        val numberOfStops = if (isZero(legsDetailList[position].stopNumber)) context.getString(R.string.itin_flight_leg_detail_widget_nonstop) else context.resources.getQuantityString(R.plurals.itin_flight_leg_detail_widget_stops_TEMPLATE, stops, stops)
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
                return Integer.parseInt(value) == 0
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
