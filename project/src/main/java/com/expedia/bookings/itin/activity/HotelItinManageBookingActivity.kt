package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.HotelItinManageBookingHelp
import com.expedia.bookings.widget.itin.HotelItinRoomDetails
import com.expedia.bookings.widget.itin.HotelItinToolbar

class HotelItinManageBookingActivity : AppCompatActivity() {

    val roomDetailsView: HotelItinRoomDetails by lazy {
        findViewById(R.id.widget_hotel_itin_room_details) as HotelItinRoomDetails
    }
    val toolbar: HotelItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as HotelItinToolbar
    }
    val manageBookingButton: Button by lazy {
        findViewById(R.id.itin_hotel_manage_booking_button) as Button
    }
    val itinCardDataHotel: ItinCardDataHotel by lazy {
        ItineraryManager.getInstance()
                .getItinCardDataFromItinId(intent.getStringExtra(ID_EXTRA)) as ItinCardDataHotel
    }
    val hotelManageBookingHelpView: HotelItinManageBookingHelp by lazy {
        findViewById(R.id.widget_hotel_itin_manage_booking_help) as HotelItinManageBookingHelp
    }

    companion object {
        private const val ID_EXTRA = "ITINID"

        fun createIntent(context: Context, id: String): Intent {
            val i : Intent = Intent(context, HotelItinManageBookingActivity::class.java)
            i.putExtra(HotelItinManageBookingActivity.ID_EXTRA, id)
            return i
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_manage_booking)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()

        roomDetailsView.setUpWidget(itinCardDataHotel)
        toolbar.setUpWidget(itinCardDataHotel, this.getString(R.string.itin_hotel_manage_booking_header))
        toolbar.setNavigationOnClickListener {
            super.finish()
        }
        manageBookingButton.setOnClickListener {
            this.startActivity(buildWebViewIntent(R.string.itin_hotel_manage_booking_webview_title, itinCardDataHotel.detailsUrl, "overview-header").intent)
        }
        hotelManageBookingHelpView.setUpWidget(itinCardDataHotel)
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(this)
        if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setAttemptForceMobileSite(true)
        return builder
    }

}