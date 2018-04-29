package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isRichContentEnabled
import com.expedia.bookings.widget.TextView
import com.expedia.vm.AbstractFlightViewModel

class FlightCellWidget(context: Context, showPrice: Boolean = true) : FrameLayout(context) {
    val cardView: CardView by bindView(R.id.card_view)
    val flightTimeTextView: TextView by bindView(R.id.flight_time_detail_text_view)
    val priceTextView: TextView by bindView(R.id.price_text_view)
    val flightDurationTextView: TextView by bindView(R.id.flight_duration_text_view)
    val flightAirlineWidget: FlightAirlineWidget by bindView(R.id.flight_airline_widget)
    val bestFlightView: ViewGroup by bindView(R.id.package_best_flight)
    val flightEarnMessage: TextView by bindView(R.id.flight_earn_message_text_view)
    val flightCabinCodeTextView: TextView by bindView(R.id.flight_class_text_view)
    val urgencyMessageTextView: TextView by bindView(R.id.urgency_message)
    val flightMessageContainer: LinearLayout by bindView(R.id.flight_message_container)
    val flightToggleIcon: ImageView by bindView(R.id.flight_overview_expand_icon)
    val richContentDividerView: View by bindView(R.id.rich_content_divider)
    val richContentWifiView: ImageView by bindView(R.id.rich_content_wifi)
    val richContentEntertainmentView: ImageView by bindView(R.id.rich_content_entertainment)
    val richContentPowerView: ImageView by bindView(R.id.rich_content_power)
    val routeScoreTextView: TextView by bindView(R.id.textView_route_score)
    lateinit var viewModel: AbstractFlightViewModel
    private var urgencyMessageVisibility = false

    init {
        View.inflate(context, R.layout.flight_cell, this)
        bestFlightView.visibility = View.GONE
        priceTextView.visibility = if (showPrice) View.VISIBLE else View.GONE
        flightToggleIcon.visibility = if (showPrice) View.GONE else View.VISIBLE
    }

    fun bind(viewModel: AbstractFlightViewModel) {
        this.viewModel = viewModel
        flightTimeTextView.text = viewModel.flightTime
        priceTextView.text = viewModel.price()
        flightDurationTextView.text = viewModel.duration
        flightAirlineWidget.update(viewModel.airline, viewModel.isEarnMessageVisible(viewModel.earnMessage))
        viewModel.updateflightCabinPreferenceObservable.subscribe {
            if (viewModel.getFlightCabinPreferenceVisibility()) {
                flightCabinCodeTextView.visibility = View.VISIBLE
                flightCabinCodeTextView.text = it
            } else {
                flightCabinCodeTextView.visibility = View.GONE
            }
        }
        if (viewModel.getUrgencyMessageVisibility(viewModel.seatsLeftUrgencyMessage)) {
            urgencyMessageVisibility = true
            urgencyMessageTextView.text = viewModel.seatsLeftUrgencyMessage
        } else {
            urgencyMessageVisibility = false
            urgencyMessageTextView.text = ""
        }
        if (viewModel.isEarnMessageVisible(viewModel.earnMessage)) {
            flightEarnMessage.text = viewModel.earnMessage
            flightEarnMessage.visibility = View.VISIBLE
        }
        if (isRichContentEnabled(context)) {
            richContentDividerView.visibility = if (viewModel.richContentDividerViewVisibility) View.VISIBLE else View.GONE
            richContentWifiView.visibility = if (viewModel.richContentWifiViewVisibility) View.VISIBLE else View.GONE
            richContentEntertainmentView.visibility = if (viewModel.richContentEntertainmentViewVisibility) View.VISIBLE else View.GONE
            richContentPowerView.visibility = if (viewModel.richContentPowerViewVisibility) View.VISIBLE else View.GONE
            routeScoreTextView.text = if (viewModel.routeScoreViewVisibility) viewModel.routeScoreText else ""
        }
        flightMessageContainer.visibility = if (urgencyMessageVisibility || viewModel.routeScoreViewVisibility) View.VISIBLE else View.GONE
        cardView.contentDescription = viewModel.getFlightContentDesc(bestFlightView.visibility == View.VISIBLE)
    }

    fun setMargins() {
        val paddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        val paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, resources.displayMetrics).toInt()
        val newParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        newParams.setMargins(paddingSide, 0, paddingSide, paddingBottom)
        cardView.layoutParams = newParams
    }
}
