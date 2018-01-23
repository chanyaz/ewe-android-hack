package com.expedia.bookings.widget.itin

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.ItinUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class FlightItinCard(context: Context, attributeSet: AttributeSet?) : ItinCard<ItinCardDataFlight>(context, attributeSet) {

    val mCheckInLayout: ViewGroup by bindView(R.id.checkin_layout)
    val mCheckInTextView: TextView by bindView(R.id.checkin_text_view)

    override fun bind(itinCardData: ItinCardDataFlight) {
        super.bind(itinCardData)
        showFlightCheckIn(itinCardData)
    }

    override fun getCollapsedHeight(): Int {
        var height = super.getCollapsedHeight()
        if (mCheckInLayout.visibility === View.VISIBLE) {
            height += mCheckInLayout.height
        }
        return height
    }

    override fun isTouchOnCheckInButton(childEvent: MotionEvent): Boolean {
        return isEventOnView(childEvent, mCheckInLayout)
    }

    private fun showFlightCheckIn(itinCardData: ItinCardDataFlight) {
        val shouldShowCheckInLink = shouldShowCheckInLink(itinCardData)
        if (shouldShowCheckInLink) {
            val flightLegNumber = (itinCardData).legNumber
            val userCheckedIn = (itinCardData.tripComponent as TripFlight).flightTrip
                    .getLeg(flightLegNumber).isUserCheckedIn
            if (userCheckedIn) {
                onCheckInLinkVisited(itinCardData)
            }
            mCheckInLayout.visibility = View.VISIBLE
            setShowSummary(true)
            mCheckInLayout.setOnClickListener {
                val userCheckedIn = (itinCardData.tripComponent as TripFlight).flightTrip
                        .getLeg(flightLegNumber).isUserCheckedIn
                if (userCheckedIn) {
                    OmnitureTracking.trackItinFlightVisitSite()
                } else {
                    val flightTrip = (itinCardData.tripComponent as TripFlight).flightTrip
                    val airlineCode = getAirlineCode(itinCardData) ?: ""
                    OmnitureTracking
                            .trackItinFlightCheckIn(airlineCode, flightTrip.isSplitTicket,
                                    flightTrip.legCount)
                }
                (itinCardData.tripComponent as TripFlight).flightTrip.getLeg(flightLegNumber).isUserCheckedIn = true
                showCheckInWebView(itinCardData)
                mCheckInTextView.postDelayed({ onCheckInLinkVisited(itinCardData) }, 5000)
            }
        } else {
            mCheckInLayout.visibility = View.GONE
            if (isCollapsed) {
                setShowSummary(false)
            }
        }
    }

    private fun shouldShowCheckInLink(itinCardData: ItinCardDataFlight): Boolean {
        return ItinUtils.shouldShowCheckInLink(context, type, itinCardData.startDate,
                mItinContentGenerator.checkInLink)
    }

    private fun onCheckInLinkVisited(itinCardData: ItinCardDataFlight) {
        val firstAirlineName = getAirlineName(itinCardData)
        mCheckInTextView.setBackgroundColor(Color.TRANSPARENT)
        mCheckInTextView.text = context.getString(R.string.itin_card_flight_checkin_details, firstAirlineName)
    }

    private fun getAirlineName(itinCardData: ItinCardDataFlight): String {
        val flightLegNumber = (itinCardData).legNumber
        return (itinCardData.tripComponent as TripFlight).flightTrip
                .getLeg(flightLegNumber)
                .primaryAirlineNamesFormatted
    }

    private fun getAirlineCode(itinCardData: ItinCardDataFlight): String? {
        val flightLegNumber = (itinCardData).legNumber
        return (itinCardData.tripComponent as TripFlight).flightTrip
                .getLeg(flightLegNumber)
                .firstAirlineCode
    }

    private fun showCheckInWebView(itinCardData: ItinCardDataFlight) {
        val builder = WebViewActivity.IntentBuilder(context)
        builder.setUrl(mItinContentGenerator.checkInLink)
        builder.setTitle(R.string.itin_card_flight_checkin_title)
        builder.setCheckInLink(true)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.intent.putExtra(Constants.ITIN_CHECK_IN_AIRLINE_CODE, getAirlineCode(itinCardData))
        builder.intent.putExtra(Constants.ITIN_CHECK_IN_AIRLINE_NAME, getAirlineName(itinCardData))
        builder.intent.putExtra(Constants.ITIN_IS_SPLIT_TICKET, (itinCardData.tripComponent as TripFlight).flightTrip.isSplitTicket)
        builder.intent.putExtra(Constants.ITIN_FLIGHT_TRIP_LEGS, (itinCardData.tripComponent as TripFlight).flightTrip.legCount)
        builder.intent.putExtra(Constants.ITIN_CHECK_IN_CONFIRMATION_CODE,
                mItinContentGenerator.summaryRightButton.text)
        (context as Activity)
                .startActivityForResult(builder.intent, Constants.ITIN_CHECK_IN_WEBPAGE_CODE)
    }
}
