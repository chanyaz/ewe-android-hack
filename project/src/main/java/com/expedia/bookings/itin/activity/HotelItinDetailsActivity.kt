package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.HotelItinBookingDetails
import com.expedia.bookings.widget.itin.HotelItinImage
import com.expedia.bookings.widget.itin.HotelItinLocationDetails
import com.expedia.bookings.widget.itin.HotelItinCheckInCheckOutDetails
import com.expedia.bookings.widget.itin.HotelItinRoomDetails
import com.expedia.bookings.widget.itin.HotelItinToolbar

class HotelItinDetailsActivity() : AppCompatActivity() {

    val locationDetailsView: HotelItinLocationDetails by lazy {
        findViewById(R.id.widget_hotel_itin_location_details) as HotelItinLocationDetails
    }
    val roomDetailsView: HotelItinRoomDetails by lazy {
        findViewById(R.id.widget_hotel_itin_room_details) as HotelItinRoomDetails
    }
    val roomDetailsHeader: View by lazy {
        findViewById(R.id.itin_hotel_room_details_header)
    }
    val hotelImageView: HotelItinImage by lazy {
        findViewById(R.id.hotel_itin_image) as HotelItinImage
    }
    val checkinCheckoutView: HotelItinCheckInCheckOutDetails by lazy {
        findViewById(R.id.widget_hotel_itin_checkin_checkout_details) as HotelItinCheckInCheckOutDetails
    }
    val toolbar: HotelItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as HotelItinToolbar
    }
    val hotelBookingDetailsView: HotelItinBookingDetails by lazy {
        findViewById(R.id.widget_hotel_itin_booking_details) as HotelItinBookingDetails
    }
    val itinCardDataHotel: ItinCardDataHotel by lazy {
        ItineraryManager.getInstance().getItinCardDataFromItinId(intent.getStringExtra(ITIN_ID_EXTRA)) as ItinCardDataHotel
    }

    companion object {
        private const val ITIN_ID_EXTRA = "ITIN_ID"

        @JvmStatic fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinDetailsActivity::class.java)
            i.putExtra(HotelItinDetailsActivity.ITIN_ID_EXTRA, id)
            return i
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
        setUpWidgets(itinCardDataHotel)
    }

    fun setUpWidgets(itinCardDataHotel: ItinCardDataHotel) {
        locationDetailsView.setupWidget(itinCardDataHotel)
        hotelImageView.setUpWidget(itinCardDataHotel)
        checkinCheckoutView.setUpWidget(itinCardDataHotel)
        roomDetailsView.setUpWidget(itinCardDataHotel)
        hotelBookingDetailsView.setUpWidget(itinCardDataHotel)
        toolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName)
        toolbar.setNavigationOnClickListener {
            super.finish()
        }
        if (itinCardDataHotel.isSharedItin) {
            roomDetailsHeader.visibility = View.GONE
        }
        else if (ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
            toolbar.showShare()
        }
    }

}