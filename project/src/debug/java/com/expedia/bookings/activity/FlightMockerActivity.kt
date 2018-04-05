package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.flight.details.FlightItinDetailsActivity
import com.expedia.bookings.server.TripParser
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mobiata.android.json.JSONUtils
import com.mobiata.flightlib.data.Seat
import kotlinx.android.synthetic.debug.activity_flight_mocker.*
import org.json.JSONObject
import java.io.InputStreamReader

class FlightMockerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var card: ItinCardDataFlight

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_mocker)
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
            R.id.bonus_traveler_cb -> bonusSelected()
        }
    }

    private fun bonusSelected() {
        if (bonus_traveler_cb.getIsChecked()) {
            email_cb.setIsEnabled(true)
            phone_cb.setIsEnabled(true)
            redress_cb.setIsEnabled(true)
            traveler_known_cb.setIsEnabled(true)
            baby_in_lap_cb.setIsEnabled(true)
            assistance_cb.setIsEnabled(true)
            frequent_flyer_cb.setIsEnabled(true)
        } else {
            frequent_flyer_cb.setIsEnabled(false)
            email_cb.setIsEnabled(false)
            phone_cb.setIsEnabled(false)
            redress_cb.setIsEnabled(false)
            traveler_known_cb.setIsEnabled(false)
            baby_in_lap_cb.setIsEnabled(false)
            assistance_cb.setIsEnabled(false)
        }
    }

    private fun setupViews() {
        nameWidgets()
        confirmation_status_spinner.setSpinnerList(makeList())
        multi_segment_cb.setOnClickListener(this)
        seating_available_cb.setOnClickListener(this)
        bonus_traveler_cb.setOnClickListener(this)
        seats_selected_cb.setOnClickListener(this)
        flight_mock_build_btn.setOnClickListener(this)
        flight_status_spinner.setSpinnerList(makeFlightStatusList())
        bonusSelected()
    }

    private fun nameWidgets() {
        confirmation_status_spinner.setText("Status")
        multi_confirmation_cb.setText("Multi Confirmation")
        red_eye_cb.setText("Red eye")
        layover_24_hours_cb.setText("Layover over 24 hours")
        layover_24_hours_cb.setIsEnabled(false)
        multi_segment_cb.setText("Multi segment")
        seating_available_cb.setText("Seating Available")
        seating_available_cb.setIsChecked(true)
        seats_selected_cb.setText("Seats selected")
        multi_seats_cb.setText("Multi seats")
        multi_seats_cb.setIsEnabled(false)
        arrival_terminal_cb.setText("arrival terminal")
        arrival_terminal_cb.setIsChecked(true)
        depart_terminal_cb.setText("departure terminal")
        depart_terminal_cb.setIsChecked(true)
        operated_by_cb.setText("Has Opperated by Carrier")
        flight_status_spinner.setText("Flight's Status")
        bonus_traveler_cb.setText("Add bonus traveler?")
        email_cb.setText("Has email?")
        phone_cb.setText("Has phone?")
        traveler_known_cb.setText("Has known traveler number?")
        redress_cb.setText("Has redress number?")
        assistance_cb.setText("Has assistance request?")
        frequent_flyer_cb.setText("Has frequent flyer?")
        baby_in_lap_cb.setText("Baby in lap?")
    }

    private fun makeFlightStatusList(): List<String> {
        return arrayListOf("Pre 24 hours", "On time", "Late", "Cancelled", "Early")
    }

    private fun seatsSelected() = if (seats_selected_cb.getIsChecked()) {
        multi_seats_cb.setIsEnabled(true)
    } else {
        multi_seats_cb.setIsEnabled(false)
        multi_seats_cb.setIsChecked(false)
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
        when (flight_status_spinner.getSelectedItem()) {
            "Pre 24 hours" -> flight.mFlightHistoryId = -1
            "Cancelled" -> flight.mFlightHistoryId = -91
            "On time" -> flight.mFlightHistoryId = -92
            "Late" -> flight.mFlightHistoryId = -93
            "Early" -> flight.mFlightHistoryId = -94
        }
    }

    private fun makeList(): List<String> {
        return arrayListOf("Complete", "Inprogress", "Cancelled", "Voided", "None")
    }

    private fun multiSegOnClick() = if (multi_segment_cb.getIsChecked()) {
        layover_24_hours_cb.setIsEnabled(true)
    } else {
        layover_24_hours_cb.setIsEnabled(false)
        layover_24_hours_cb.setIsChecked(false)
    }

    private fun seatingOnClick() = if (seating_available_cb.getIsChecked()) {
        seats_selected_cb.setIsEnabled(true)
        multi_seats_cb.setIsEnabled(true)
    } else {
        seats_selected_cb.setIsEnabled(false)
        seats_selected_cb.setIsChecked(false)
        multi_seats_cb.setIsEnabled(false)
        multi_seats_cb.setIsChecked(false)
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
        if (!multi_confirmation_cb.getIsChecked()) {
            val confirmationArr = flight.getJSONArray("confirmationNumbers")
            confirmationArr.remove(1)
            confirmationArr.remove(1)
        }
        var mTicketingStatus = TicketingStatus.COMPLETE
        when (confirmation_status_spinner.getSelectedItem()) {
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
        if (!red_eye_cb.getIsChecked()) {
            firstSeg.getJSONObject("departureTime").put("epochSeconds", 1513247940)
        }
        if (multi_segment_cb.getIsChecked()) {
            if (layover_24_hours_cb.getIsChecked()) {
                secondSeg.getJSONObject("departureTime").put("epochSeconds", 1513347600)
                secondSeg.getJSONObject("arrivalTime").put("epochSeconds", 1513358340)
                firstSeg.put("layoverDuration", "PT25H21M")
                leg.put("duration", "PT33H40M")
            }
        } else {
            segments.remove(1)
            firstSeg.remove("layoverDuration")
        }

        if (!arrival_terminal_cb.getIsChecked()) {
            firstSeg.remove("arrivalTerminal")
        }

        if (!depart_terminal_cb.getIsChecked()) {
            firstSeg.remove("departureTerminal")
        }

        firstSeg.put("isSeatMapAvailable", seating_available_cb.getIsChecked())

        if (seats_selected_cb.getIsChecked()) {
            val seatList = ArrayList<Seat>()
            val seatA = Seat(assigned = "22A", passenger = "Bob Burger")
            seatList.add(seatA)
            if (multi_seats_cb.getIsChecked()) {
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

        if (operated_by_cb.getIsChecked()) {
            firstSeg.put("operatedByAirCarrierName", "AirNeo Enterprise Express")
        }
        val travelers = flight.getJSONArray("passengers")
        val traveler = travelers.getJSONObject(1)
        if (bonus_traveler_cb.getIsChecked()) {
            if (!email_cb.getIsChecked()) traveler.remove("emailAddress")
            if (!phone_cb.getIsChecked()) {
                traveler.getJSONArray("phoneNumbers").remove(0)
            }
            if (!traveler_known_cb.getIsChecked()) traveler.put("TSAKnownTravelerNumber", "")
            if (!redress_cb.getIsChecked()) traveler.put("TSARedressNumber", "")
            if (!assistance_cb.getIsChecked()) traveler.remove("specialAssistanceOptions")
            if (baby_in_lap_cb.getIsChecked()) traveler.put("typeCode", "INFANT_IN_LAP")
            if (!frequent_flyer_cb.getIsChecked()) traveler.getJSONArray("frequentFlyerPlans").remove(0)
        } else travelers.remove(1)

        val tripObj = tripParser.parseTrip(obj)
        val tripComponent = tripObj.tripComponents[0]
        return tripComponent as? TripFlight
    }
}
