package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.vm.HotelItinManageRoomViewModel
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class HotelItinManageBookingActivity : HotelItinBaseActivity() {

    val toolbar by bindView<HotelItinToolbar>(R.id.widget_hotel_itin_toolbar)
    lateinit var itinCardDataHotel: ItinCardDataHotel
    val manageRoomContainer by bindView<LinearLayout>(R.id.widget_hotel_manage_room_container)
    val manageRoomViewModel = HotelItinManageRoomViewModel(this)

    companion object {
        private const val ID_EXTRA = "ITINID"

        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinManageBookingActivity::class.java)
            i.putExtra(HotelItinManageBookingActivity.ID_EXTRA, id)
            return i
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_manage_booking)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        manageRoomContainer.addView(manageRoomViewModel.manageRoomWidget)
    }

    override fun onResume() {
        super.onResume()
        updateItinCardDataHotel()
    }

    fun setUpWidgets() {
        toolbar.setUpWidget(itinCardDataHotel, this.getString(R.string.itin_hotel_manage_booking_header))
        toolbar.setNavigationOnClickListener {
            super.finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }
    }

    override fun updateItinCardDataHotel() {
        val freshItinCardDataHotel = getItineraryManager().getItinCardDataFromItinId(intent.getStringExtra(ID_EXTRA)) as ItinCardDataHotel?
        if (freshItinCardDataHotel == null) {
            finish()
        } else {
            itinCardDataHotel = freshItinCardDataHotel
            setUpWidgets()
            manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)
        }
    }
}
