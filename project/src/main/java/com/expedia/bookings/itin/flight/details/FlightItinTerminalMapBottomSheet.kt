package com.expedia.bookings.itin.flight.details

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.ActivityOptionsCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.activity.TerminalMapActivity
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.TextView
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils
import com.squareup.phrase.Phrase

class FlightItinTerminalMapBottomSheet : BottomSheetDialogFragment() {

    companion object {
        val DEPARTURE_AIRPORT_CODE = "DEPARTURE_AIRPORT_CODE"
        val ARRIVAL_AIRPORT_CODE = "ARRIVAL_AIRPORT_CODE"

        @JvmStatic
        fun newInstance(departureCode: String?, arrivalCode: String?): FlightItinTerminalMapBottomSheet {
            val bottomSheet = FlightItinTerminalMapBottomSheet()
            val args = Bundle()
            args.putString(DEPARTURE_AIRPORT_CODE, departureCode)
            args.putString(ARRIVAL_AIRPORT_CODE, arrivalCode)
            bottomSheet.arguments = args
            return bottomSheet
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.flight_itin_terminal_map_bottom_sheet, null)

        val departureAirportCode = arguments.getString(DEPARTURE_AIRPORT_CODE, null)
        val arrivalAirportCode = arguments.getString(ARRIVAL_AIRPORT_CODE, null)

        val departureAirportText = view?.findViewById<TextView>(R.id.terminal_map_departure_airport)
        if (!departureAirportCode.isNullOrEmpty() && checkIfMapIsAvailable(departureAirportCode)) {
            departureAirportText?.text = Phrase.from(context, R.string.itin_flight_terminal_maps_bottom_sheet_airport_text_TEMPLATE).put("airport", departureAirportCode).format().toString()
            departureAirportText?.setOnClickListener(terminalMapClickListener(departureAirportCode))
        } else {
            departureAirportText?.visibility = View.GONE
        }

        val arrivalAirportText = view?.findViewById<TextView>(R.id.terminal_map_arrival_airport)
        if (!arrivalAirportCode.isNullOrEmpty() && checkIfMapIsAvailable(arrivalAirportCode)) {
            arrivalAirportText?.text = Phrase.from(context, R.string.itin_flight_terminal_maps_bottom_sheet_airport_text_TEMPLATE).put("airport", arrivalAirportCode).format().toString()
            arrivalAirportText?.setOnClickListener(terminalMapClickListener(arrivalAirportCode))
        } else {
            arrivalAirportText?.visibility = View.GONE
        }

        return view
    }

    fun terminalMapClickListener(airportCode: String?): View.OnClickListener {
        return View.OnClickListener {
            val terminalMapIntent = TerminalMapActivity.createIntent(context, airportCode)
            NavUtils.startActivitySafe(context, terminalMapIntent, ActivityOptionsCompat
                    .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                    .toBundle())
        }
    }

    fun checkIfMapIsAvailable(airportCode: String): Boolean {
        val airport = FlightStatsDbUtils.getAirport(airportCode)
        if (airport != null) {
            return airport.hasAirportMaps()
        }
        return false
    }
}
