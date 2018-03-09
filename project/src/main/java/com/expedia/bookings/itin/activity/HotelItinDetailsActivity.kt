package com.expedia.bookings.itin.activity

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripHotelRoom
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinBookingDetails
import com.expedia.bookings.itin.widget.HotelItinCheckInCheckOutDetails
import com.expedia.bookings.itin.widget.HotelItinImageWidget
import com.expedia.bookings.itin.widget.HotelItinLocationDetails
import com.expedia.bookings.itin.widget.HotelItinRoomDetails
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

open class HotelItinDetailsActivity : HotelItinBaseActivity() {

    val locationDetailsView by bindView<HotelItinLocationDetails>(R.id.widget_hotel_itin_location_details)
    val hotelImageView by bindView<HotelItinImageWidget>(R.id.hotel_itin_image)
    val checkinCheckoutView by bindView<HotelItinCheckInCheckOutDetails>(R.id.widget_hotel_itin_checkin_checkout_details)
    val toolbar by bindView<HotelItinToolbar>(R.id.widget_hotel_itin_toolbar)
    val hotelBookingDetailsView by bindView<HotelItinBookingDetails>(R.id.widget_hotel_itin_booking_details)

    val roomDetailsHeader by bindView<TextView>(R.id.itin_hotel_room_details_header)
    val multiRoomContainer by bindView<LinearLayout>(R.id.hotel_itin_details_multi_room_container)

    val container by bindView<ViewGroup>(R.id.container)

    lateinit var itinCardDataHotel: ItinCardDataHotel

    companion object {
        private const val UNIQUE_ID_EXTRA = "UNIQUE_ID_EXTRA"
        private const val ITIN_ID_EXTRA = "ITIN_ID_EXTRA"

        @JvmStatic
        fun createIntent(context: Context, id: String, itinId: String): Intent {
            val i = Intent(context, HotelItinDetailsActivity::class.java)
            i.putExtra(HotelItinDetailsActivity.UNIQUE_ID_EXTRA, id)
            i.putExtra(HotelItinDetailsActivity.ITIN_ID_EXTRA, itinId)
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
        updateItinCardDataHotel()
    }

    override fun updateItinCardDataHotel() {
        val freshItinCardDataHotel = getItineraryManager().getItinCardDataFromItinId(intent.getStringExtra(UNIQUE_ID_EXTRA)) as ItinCardDataHotel?
        if (freshItinCardDataHotel == null) {
            finish()
        } else {
            itinCardDataHotel = freshItinCardDataHotel
            setUpWidgets()
        }
    }

    fun setUpWidgets() {
        container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        locationDetailsView.setupWidget(itinCardDataHotel)
        hotelImageView.setUpWidget(itinCardDataHotel)
        checkinCheckoutView.setUpWidget(itinCardDataHotel)

        val rooms = itinCardDataHotel.rooms
        if (itinCardDataHotel.isSharedItin || rooms.isEmpty()) {
            roomDetailsHeader.visibility = View.GONE
            multiRoomContainer.visibility = View.GONE
        } else {
            if (rooms.size == 1) {
                roomDetailsHeader.text = Phrase.from(this, R.string.itin_hotel_details_room_details_title_text).format().toString()
            } else {
                roomDetailsHeader.text = Phrase.from(this, R.string.itin_hotel_details_room_details_multi_room_title_text).format().toString()
            }
            addRoomsToContainer(rooms)
        }

        hotelBookingDetailsView.setUpWidget(itinCardDataHotel)
        toolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName)
        toolbar.setNavigationOnClickListener {
            super.finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }
        if (!itinCardDataHotel.isSharedItin && ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
            toolbar.showShare()
        }

        TripsTracking.trackItinHotel(hotelHasMessagingURL())
    }

    private fun addRoomsToContainer(rooms: MutableList<TripHotelRoom?>) {
        multiRoomContainer.removeAllViews()
        rooms.filterNotNull().forEach {
            val roomDetailsView = HotelItinRoomDetails(this, null)

            roomDetailsView.setUpRoomAndOccupantInfo(it)
            roomDetailsView.setUpAndShowAmenities(it)

            roomDetailsView.collapseRoomDetailsView()
            roomDetailsView.showChevron()

            roomDetailsView.collapsedRoomDetails.setOnClickListener {
                val count = multiRoomContainer.childCount
                (0 until count)
                        .map { multiRoomContainer.getChildAt(it) as HotelItinRoomDetails }
                        .filter { it != roomDetailsView }
                        .forEach { it.collapseRoomDetailsView() }
                roomDetailsView.doOnClick()
            }

            multiRoomContainer.addView(roomDetailsView)
        }
    }

    private fun hotelHasMessagingURL(): Boolean = itinCardDataHotel.property.hasHotelMessagingUrl()
}
