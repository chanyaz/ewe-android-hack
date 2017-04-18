package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.TextView
import com.expedia.vm.AbstractFlightViewModel

class FlightCellWidget(context: Context, val isRoundTripSearch: Boolean, val maxFlightDuration: Int, showPrice: Boolean = true) : FrameLayout(context) {
    val cardView: CardView by bindView(R.id.card_view)
    val flightTimeTextView: TextView by bindView(R.id.flight_time_detail_text_view)
    val priceTextView: TextView by bindView(R.id.price_text_view)
    val flightDurationTextView: TextView by bindView(R.id.flight_duration_text_view)
    val flightLayoverWidget: FlightLayoverWidget by bindView(R.id.custom_flight_layover_widget)
    val flightAirlineWidget: FlightAirlineWidget by bindView(R.id.flight_airline_widget)
    val bestFlightView: ViewGroup by bindView(R.id.package_best_flight)
    val flightEarnMessage: TextView by bindView(R.id.flight_earn_message_text_view)
    val flightCabinCodeTextView: TextView by bindView(R.id.flight_class_text_view)
    val urgencyMessageTextView: TextView by bindView(R.id.urgency_message)
    val urgencyMessageContainer: LinearLayout by bindView(R.id.urgency_message_layout)
    val roundTripTextView: TextView by bindView(R.id.trip_type_text_view)
    val flightEarnMessageWithoutRoundTrip: TextView by bindView(R.id.flight_earn_message_text_view_without_roundtrip)
    val flightToggleIcon: ImageView by bindView(R.id.flight_overview_expand_icon)

    init {
        View.inflate(context, R.layout.flight_cell, this)
        bestFlightView.visibility = View.GONE
        priceTextView.visibility = if (showPrice) View.VISIBLE else View.GONE
        flightToggleIcon.visibility = if (showPrice) View.GONE else View.VISIBLE
    }

    fun bind(viewModel: AbstractFlightViewModel) {
        if (isRoundTripSearch && viewModel.getRoundTripMessageVisibilty()) {
            roundTripTextView.visibility = View.VISIBLE
        } else {
            roundTripTextView.visibility = View.GONE
        }
        flightTimeTextView.text = viewModel.flightTime
        priceTextView.text = viewModel.price()
        flightDurationTextView.text = viewModel.duration
        val flight = viewModel.layover
        flightLayoverWidget.update(flight.flightSegments, flight.durationHour, flight.durationMinute, maxFlightDuration)
        flightAirlineWidget.update(viewModel.airline, isRoundTripSearch && viewModel.getRoundTripMessageVisibilty(), viewModel.isEarnMessageVisible(viewModel.earnMessage))
        if (viewModel.getFlightCabinPreferenceVisibility() && Strings.isNotEmpty(viewModel.flightCabinPreferences)) {
            flightCabinCodeTextView.visibility = View.VISIBLE
            flightCabinCodeTextView.text = viewModel.flightCabinPreferences
        } else {
            flightCabinCodeTextView.visibility = View.GONE
        }
        if (viewModel.getUrgencyMessageVisibility(viewModel.seatsLeftUrgencyMessage)) {
            urgencyMessageContainer.visibility = View.VISIBLE
            urgencyMessageTextView.text = viewModel.seatsLeftUrgencyMessage
        } else {
            urgencyMessageContainer.visibility = View.GONE
        }
        if (viewModel.isEarnMessageVisible(viewModel.earnMessage)) {
            if (roundTripTextView.visibility == View.VISIBLE) {
                flightEarnMessage.text = viewModel.earnMessage
                flightEarnMessage.visibility = View.VISIBLE
                flightEarnMessageWithoutRoundTrip.visibility = View.GONE
            } else {
                flightEarnMessageWithoutRoundTrip.text = viewModel.earnMessage
                flightEarnMessageWithoutRoundTrip.visibility = View.VISIBLE
                flightEarnMessage.visibility = View.GONE
            }
        }
        cardView.contentDescription = viewModel.contentDescription
    }

    fun setMargins() {
        val paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        val paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, resources.displayMetrics).toInt()
        val newParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        newParams.setMargins(paddingSide, paddingTop, paddingSide, 0)
        cardView.layoutParams = newParams
    }
}