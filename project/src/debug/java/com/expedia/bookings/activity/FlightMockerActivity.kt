package com.expedia.bookings.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mobiata.android.json.JSONUtils
import com.mobiata.flightlib.data.Seat
import org.json.JSONObject
import java.io.InputStreamReader

class FlightMockerActivity : AppCompatActivity(), View.OnClickListener {

    private val statusSpinner: Spinner by bindView(R.id.confirmation_status_spinner)
    private val multiConfCB: CheckBox by bindView(R.id.multi_confirmation_cb)
    private val redEyeCB: CheckBox by bindView(R.id.red_eye_cb)
    private val multiSegCB: CheckBox by bindView(R.id.multi_segment_cb)
    private val layoverCB: CheckBox by bindView(R.id.layover_24_hours_cb)
    private val seatingAvailableCB: CheckBox by bindView(R.id.seating_available_cb)
    private val seatingSelectedCB: CheckBox by bindView(R.id.seats_selected_cb)
    private val multiSeatingCB: CheckBox by bindView(R.id.multi_seats_cb)
    private val arrivalTerminalCB: CheckBox by bindView(R.id.arrival_terminal_cb)
    private val departureTerminalCB: CheckBox by bindView(R.id.depart_terminal_cb)
    private val buildBtn: TextView by bindView(R.id.flight_mock_build_btn)
    private val operatedBy: CheckBox by bindView(R.id.operated_by_cb)
    private val flightStatusSpinner: Spinner by bindView(R.id.flight_status_spinner)

    private lateinit var card: ItinCardDataFlight

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_mocker)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        ItineraryManager.getInstance().removeItin("flightMock")
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.multi_segment_cb -> multiSegOnClick()
            R.id.seating_available_cb -> seatingOnClick()
            R.id.flight_mock_build_btn -> buildAndLaunch()
            R.id.seats_selected_cb -> seatsSelected()
        }
    }

    private fun setupViews() {
        statusSpinner.adapter = makeList()
        multiSegCB.setOnClickListener(this)
        seatingAvailableCB.setOnClickListener(this)
        seatingSelectedCB.setOnClickListener(this)
        buildBtn.setOnClickListener(this)
        flightStatusSpinner.adapter = makeFlightStatusList()
    }

    private fun makeFlightStatusList(): SpinnerAdapter {
        val list = ArrayList<String>()
        list.add("Pre 24 hours")
        list.add("On time")
        list.add("Late")
        list.add("Cancelled")
        list.add("Early")
        return ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
    }

    private fun seatsSelected() = if (seatingSelectedCB.isChecked) {
        multiSeatingCB.isEnabled = true
    } else {
        multiSeatingCB.isEnabled = false
        multiSeatingCB.isChecked = false
    }

    private fun buildAndLaunch() {
        card = ItinCardDataFlight(fetchTripFlight(), 0)
        //flight stats stuff need to be done on Json instead later
        flightStatsMagic()
        val manager = ItineraryManager.getInstance()
        card.id = "flightMock"
        manager.itinCardData.add(card)
        this.startActivity(FlightItinDetailsActivity.createIntent(this, card.id),
                ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                        .toBundle())
    }

    private fun flightStatsMagic() {
        val flight = card.flightLeg.getSegment(0)
        when (flightStatusSpinner.selectedItem.toString()) {
            "Pre 24 hours" -> flight.mFlightHistoryId = -1
            "Cancelled" -> flight.mFlightHistoryId = -91
            "On time" -> flight.mFlightHistoryId = -92
            "Late" -> flight.mFlightHistoryId = -93
            "Early" -> flight.mFlightHistoryId = -94
        }
    }


    private fun makeList(): ArrayAdapter<String> {
        val list = ArrayList<String>()
        list.add("Complete")
        list.add("Inprogress")
        list.add("Cancelled")
        list.add("Voided")
        list.add("None")
        return ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
    }

    private fun multiSegOnClick() = if (multiSegCB.isChecked) {
        layoverCB.isEnabled = true
    } else {
        layoverCB.isEnabled = false
        layoverCB.isChecked = false
    }

    private fun seatingOnClick() = if (seatingAvailableCB.isChecked) {
        seatingSelectedCB.isEnabled = true
    } else {
        seatingSelectedCB.isEnabled = false
        seatingSelectedCB.isChecked = false
        multiSeatingCB.isEnabled = false
        multiSeatingCB.isChecked = false
    }

    private fun fetchTripFlight(): TripFlight {
        val data = GsonBuilder().create().fromJson(InputStreamReader(this.assets.open("api/trips/flight_trip_details_mock_for_builder.json")), JsonObject::class.java)
        val jsonObject = JSONObject(data.toString())
        val jsonResponseData = jsonObject.getJSONObject("responseData")

        return getFlightTrip(jsonResponseData)!!
    }

    private fun getFlightTrip(obj: JSONObject): TripFlight? {
        val tripParser = TripParser()
        val flight = obj.getJSONArray("flights").getJSONObject(0)
        if (!multiConfCB.isChecked) {
            val confirmationArr = flight.getJSONArray("confirmationNumbers")
            confirmationArr.remove(1)
            confirmationArr.remove(1)
        }
        var mTicketingStatus = TicketingStatus.COMPLETE
        when (statusSpinner.selectedItem.toString()) {
            "Complete" -> mTicketingStatus = TicketingStatus.COMPLETE
            "Inprogress" -> mTicketingStatus = TicketingStatus.INPROGRESS
            "Voided" -> mTicketingStatus = TicketingStatus.VOIDED
            "Cancelled" -> mTicketingStatus = TicketingStatus.CANCELLED
            "None" -> mTicketingStatus = TicketingStatus.NONE
        }
        val leg = flight.getJSONArray("legs").getJSONObject(0)
        val segments = leg.getJSONArray("segments")
        val firstSeg = segments.getJSONObject(0)
        val secondSeg = segments.getJSONObject(1)
        if (!redEyeCB.isChecked) {
            firstSeg.getJSONObject("departureTime").put("epochSeconds", 1513247940)
        }
        if (multiSegCB.isChecked) {
            if (layoverCB.isChecked) {
                secondSeg.getJSONObject("departureTime").put("epochSeconds", 1513347600)
                secondSeg.getJSONObject("arrivalTime").put("epochSeconds", 1513358340)
                firstSeg.put("layoverDuration", "PT25H21M")
                leg.put("duration", "PT33H40M")
            }
        } else {
            segments.remove(1)
            firstSeg.remove("layoverDuration")
        }

        if (!arrivalTerminalCB.isChecked) {
            firstSeg.remove("arrivalTerminal")
        }

        if (!departureTerminalCB.isChecked) {
            firstSeg.remove("departureTerminal")
        }

        firstSeg.put("isSeatMapAvailable", seatingAvailableCB.isChecked)

        if (seatingSelectedCB.isChecked) {
            val seatList = ArrayList<Seat>()
            val seatA = Seat(assigned = "22A", passenger = "Bob Burger")
            seatList.add(seatA)
            if (multiSeatingCB.isChecked) {
                val seatB = Seat(assigned = "22B", passenger = "Bob Burger")
                seatList.add(seatB)
                val seatC = Seat(assigned = "22C")
                seatList.add(seatC)
                val seatD = Seat(assigned = "22D")
                seatList.add(seatD)
                val seatE = Seat(assigned = "22E")
                seatList.add(seatE)
                val seatA2 = Seat(assigned = "23A")
                seatList.add(seatA2)
                val seatB2 = Seat(assigned = "23B")
                seatList.add(seatB2)
                val seatC2 = Seat(assigned = "23C")
                seatList.add(seatC2)
                val seatD2 = Seat(assigned = "23D")
                seatList.add(seatD2)
                val seatE2 = Seat(assigned = "23E")
                seatList.add(seatE2)
            }
            JSONUtils.putJSONableList(firstSeg, "seatList", seatList)
        }

        JSONUtils.putEnum(flight, "ticketingStatus", mTicketingStatus)

        if (operatedBy.isChecked) {
            firstSeg.put("operatedByAirCarrierName", "AirNeo Enterprise Express")
        }

        val tripObj = tripParser.parseTrip(obj)
        val tripComponent = tripObj.tripComponents[0]
        return tripComponent as? TripFlight
    }
}
