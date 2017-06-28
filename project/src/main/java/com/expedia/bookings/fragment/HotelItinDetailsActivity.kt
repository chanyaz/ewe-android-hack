package com.expedia.bookings.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.Ui

class HotelItinDetailsActivity : AppCompatActivity() {

    private var itinCardDataHotel: ItinCardDataHotel? = null

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

        itinCardDataHotel = ItineraryManager.getInstance().getItinCardDataFromItinId(intent.id) as ItinCardDataHotel
    }
}
