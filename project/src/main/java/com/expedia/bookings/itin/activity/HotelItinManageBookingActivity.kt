package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.vm.HotelItinManageRoomViewModel
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase

class HotelItinManageBookingActivity : HotelItinBaseActivity() {

    val toolbar by bindView<HotelItinToolbar>(R.id.widget_hotel_itin_toolbar)
    lateinit var itinCardDataHotel: ItinCardDataHotel

    val manageRoomContainer by bindView<LinearLayout>(R.id.widget_hotel_manage_room_container)
    val manageRoomViewModel = HotelItinManageRoomViewModel(this)

    val roomTabs by bindView<TabLayout>(R.id.hotel_itin_room_tabs)
    val numberOfRoomsText by bindView<TextView>(R.id.hotel_itin_number_of_rooms_text)

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
        manageRoomViewModel.closeActivitySubject.subscribe {
            finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }

        manageRoomViewModel.initializeTabsSubject.subscribe { numberOfRooms ->
            roomTabs.visibility = View.VISIBLE
            if (numberOfRooms > 3) {
                roomTabs.tabMode = TabLayout.MODE_SCROLLABLE
            }
            roomTabs.addOnTabSelectedListener(manageRoomViewModel)
        }
        manageRoomViewModel.clearTabsSubject.subscribe {
            roomTabs.removeAllTabs()
        }
        manageRoomViewModel.hideTabsSubject.subscribe {
            roomTabs.visibility = View.GONE
        }
        manageRoomViewModel.addTabSubject.subscribe {
            val tab = roomTabs.newTab()
            tab.text = Phrase.from(this, R.string.itin_hotel_manage_booking_room_tab_title_TEMPLATE).put("number", it).format().toString()
            roomTabs.addTab(tab)
        }
        manageRoomViewModel.hideNumberOfRoomsTextSubject.subscribe {
            numberOfRoomsText.visibility = View.GONE
        }
        manageRoomViewModel.showNumberOfRoomsTextSubject.subscribe {
            numberOfRoomsText.visibility = View.VISIBLE
            numberOfRoomsText.text = Phrase.from(this, R.string.itin_hotel_manage_booking_total_rooms_text_TEMPLATE).put("number", it).format().toString()
        }
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
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        } else {
            itinCardDataHotel = freshItinCardDataHotel
            setUpWidgets()
            manageRoomViewModel.refreshItinCardDataSubject.onNext(itinCardDataHotel)
        }
    }
}
