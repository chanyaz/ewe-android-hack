package com.expedia.bookings.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.widget.TextView
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mobiata.android.json.JSONUtils
import com.mobiata.flightlib.data.Seat
import org.json.JSONObject
import java.io.InputStreamReader

class FlightMockerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var statusSpinner: Spinner
    private lateinit var multiConfCB: CheckBox
    private lateinit var redEyeCB: CheckBox
    private lateinit var multiSegCB: CheckBox
    private lateinit var layoverCB: CheckBox
    private lateinit var seatingAvailableCB: CheckBox
    private lateinit var seatingSelectedCB: CheckBox
    private lateinit var multiSeatingCB: CheckBox
    private lateinit var arrivalTerminalCB: CheckBox
    private lateinit var departureTerminalCB: CheckBox
    private lateinit var buildBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_mocker)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        getViews()
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

    private fun getViews() {
        statusSpinner = findViewById(R.id.confirmation_status_spinner) as Spinner
        statusSpinner.adapter = makeList()
        multiConfCB = findViewById(R.id.multi_confirmation_cb) as CheckBox
        redEyeCB = findViewById(R.id.red_eye_cb) as CheckBox
        multiSegCB = findViewById(R.id.multi_segment_cb) as CheckBox
        multiSegCB.setOnClickListener(this)
        layoverCB = findViewById(R.id.layover_24_hours_cb) as CheckBox
        seatingAvailableCB = findViewById(R.id.seating_available_cb) as CheckBox
        seatingAvailableCB.setOnClickListener(this)
        seatingSelectedCB = findViewById(R.id.seats_selected_cb) as CheckBox
        seatingSelectedCB.setOnClickListener(this)
        multiSeatingCB = findViewById(R.id.multi_seats_cb) as CheckBox
        arrivalTerminalCB = findViewById(R.id.arrival_terminal_cb) as CheckBox
        departureTerminalCB = findViewById(R.id.depart_terminal_cb) as CheckBox
        buildBtn = findViewById(R.id.flight_mock_build_btn) as TextView
        buildBtn.setOnClickListener(this)
    }

    private fun seatsSelected() = if (seatingSelectedCB.isChecked) {
        multiSeatingCB.isEnabled = true
    } else {
        multiSeatingCB.isEnabled = false
        multiSeatingCB.isChecked = false
    }

    private fun buildAndLaunch() {
        val card = ItinCardDataFlight(fetchTripFlight(), 0)
        val manager = ItineraryManager.getInstance()
        card.id = "flightMock"
        manager.itinCardData.add(card)
        this.startActivity(FlightItinDetailsActivity.createIntent(this, card.id),
                ActivityOptionsCompat
                        .makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                        .toBundle())
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
                seatList.add(seatC)
            }
            JSONUtils.putJSONableList(firstSeg, "seatList", seatList)
        }

        JSONUtils.putEnum(flight, "ticketingStatus", mTicketingStatus)
        val tripObj = tripParser.parseTrip(obj)
        val tripComponent = tripObj.tripComponents[0]
        return if (tripComponent is TripFlight) {
            tripComponent
        } else {
            null
        }
    }
}
