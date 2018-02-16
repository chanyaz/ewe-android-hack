package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.server.TripParser
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_hotel_mocker.*
import org.json.JSONObject
import java.io.InputStreamReader

class HotelMocker : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_mocker)
    }

    override fun onResume() {
        super.onResume()

        setupMockButton()
        ItineraryManager.getInstance().removeItin("hotelMock")
    }

    private fun setupMockButton() {
        val a = AnimationUtils.loadAnimation(this, R.anim.scale)
        a.reset()
        mock_build_btn.clearAnimation()
        mock_build_btn.startAnimation(a)
        mock_build_btn.setOnClickListener {
            val card = ItinCardDataHotel(fetchTripHotel())
            val manager = ItineraryManager.getInstance()
            card.id = "hotelMock"
            manager.itinCardData.add(card)
            this.startActivity(HotelItinDetailsActivity.createIntent(this, card.id),
                    ActivityOptionsCompat
                            .makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                            .toBundle())
        }
    }

    private fun fetchTripHotel(): TripHotel {
        val data = GsonBuilder().create().fromJson(InputStreamReader(this.assets.open("api/trips/hotel_trip_details_for_mocker.json")), JsonObject::class.java)
        val jsonObject = JSONObject(data.toString())
        val jsonResponseData = jsonObject.getJSONObject("responseData")

        return getHotel(jsonResponseData)
    }

    private fun getHotel(obj: JSONObject): TripHotel {
        val tripParser = TripParser()
        val tripObj = tripParser.parseTrip(obj)
        val tripComponent = tripObj.tripComponents[0]
        return tripComponent as TripHotel
    }
}
