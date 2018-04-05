package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.TripPackage
import com.expedia.bookings.itin.hotel.details.HotelItinDetailsActivity
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.utils.Ui
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.android.synthetic.debug.activity_hotel_mocker.*
import org.json.JSONObject
import java.io.InputStreamReader

class HotelMocker : AppCompatActivity() {

    lateinit var fileUtil: ITripsJsonFileUtils

    private val id = "hotelMock"
    private val fileId = "MockFile"
    private val fileName = "api/trips/itin_package_mock.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_mocker)
        fileUtil = Ui.getApplication(this).appComponent().tripJsonFileUtils()
    }

    override fun onResume() {
        super.onResume()

        setupMockButton()
        ItineraryManager.getInstance().removeItin(id)
        fileUtil.deleteTripFile(fileId)
    }

    private fun setupMockButton() {
        val a = AnimationUtils.loadAnimation(this, R.anim.scale)
        a.reset()
        mock_build_btn.clearAnimation()
        mock_build_btn.startAnimation(a)
        mock_build_btn.setOnClickListener {
            val card = ItinCardDataHotel(fetchTripHotel())
            val manager = ItineraryManager.getInstance()
            card.id = id
            card.tripComponent.parentTrip.tripId = fileId
            manager.itinCardData.add(card)
            val json = JSONObject(createMock().toString())
            fileUtil.writeTripToFile(fileId, json.toString())
            this.startActivity(HotelItinDetailsActivity.createIntent(this, card.id, card.tripId),
                    ActivityOptionsCompat
                            .makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left_complete)
                            .toBundle())
        }
    }

    private fun fetchTripHotel(): TripHotel {
        val data = createMock()
        val jsonObject = JSONObject(data.toString())
        val jsonResponseData = jsonObject.getJSONObject("responseData")

        return getHotel(jsonResponseData)
    }

    private fun getHotel(obj: JSONObject): TripHotel {
        val tripParser = TripParser()
        val tripObj = tripParser.parseTrip(obj)
        val tripComponent = tripObj.tripComponents[0]
        return if (tripComponent is TripHotel)
            tripComponent
        else {
            val packageItin = tripComponent as TripPackage
            packageItin.tripComponents.first { it is TripHotel } as TripHotel
        }
    }

    private fun createMock(): JsonObject {
        return GsonBuilder().create().fromJson(InputStreamReader(this.assets.open(fileName)), JsonObject::class.java)
    }
}
