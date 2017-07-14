package com.expedia.bookings.itin.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.HotelItinBookingDetails
import com.expedia.bookings.widget.itin.HotelItinImage
import com.expedia.bookings.widget.itin.HotelItinLocationDetails
import com.expedia.bookings.widget.itin.HotelItinRoomDetails
import com.expedia.bookings.widget.itin.HotelItinToolbar

class HotelItinDetailsActivity() : AppCompatActivity() {

    val locationDetailsView: HotelItinLocationDetails by lazy {
        findViewById(R.id.widget_hotel_itin_location_details) as HotelItinLocationDetails
    }
    val roomDetailsView: HotelItinRoomDetails by lazy {
        findViewById(R.id.widget_hotel_itin_room_details) as HotelItinRoomDetails
    }
    val hotelImageView: HotelItinImage by lazy {
        findViewById(R.id.hotel_itin_image) as HotelItinImage
    }
    val toolbar: HotelItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as HotelItinToolbar
    }
    val hotelBookingDetailsView: HotelItinBookingDetails by lazy {
        findViewById(R.id.widget_hotel_itin_booking_details) as HotelItinBookingDetails
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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()


        val itinCardDataHotel: ItinCardDataHotel = ItineraryManager.getInstance().getItinCardDataFromItinId(intent.id) as ItinCardDataHotel
        locationDetailsView.setupWidget(itinCardDataHotel)
        roomDetailsView.setUpWidget(itinCardDataHotel)
        hotelImageView.setUpWidget(itinCardDataHotel)
        toolbar.setUpWidget(itinCardDataHotel)
        toolbar.setNavigationOnClickListener {
            super.finish()
        }
        hotelBookingDetailsView.setUpWidget(itinCardDataHotel)
    }

}