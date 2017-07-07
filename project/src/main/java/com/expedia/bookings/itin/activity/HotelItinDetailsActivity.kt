package com.expedia.bookings.itin.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.HotelItinImage
import com.expedia.bookings.widget.itin.HotelItinRoomDetails

class HotelItinDetailsActivity : AppCompatActivity() {

    val roomDetailsView: HotelItinRoomDetails by lazy {
        findViewById(R.id.widget_hotel_itin_room_details) as HotelItinRoomDetails
    }

    val hotelImageView: HotelItinImage by lazy {
        findViewById(R.id.hotel_itin_image) as HotelItinImage
    }

    companion object IntentExtras {
        private const val ITIN_ID_EXTRA = "ITIN_ID"
        var Intent.id: String?
            get() = getStringExtra(ITIN_ID_EXTRA)
            set(id) {
                putExtra(ITIN_ID_EXTRA, id)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_card_details)
    }

    override fun onResume() {
        super.onResume()

        val itinCardDataHotel: ItinCardDataHotel = ItineraryManager.getInstance().getItinCardDataFromItinId(intent.id) as ItinCardDataHotel
        roomDetailsView.setUpWidget(itinCardDataHotel)
        hotelImageView.setUpWidget(itinCardDataHotel)
    }
}
