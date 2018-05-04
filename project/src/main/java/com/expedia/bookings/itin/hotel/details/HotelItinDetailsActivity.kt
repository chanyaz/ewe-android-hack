package com.expedia.bookings.itin.hotel.details

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
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.itin.common.ItinBaseActivity
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.hotel.common.HotelItinToolbar
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

open class HotelItinDetailsActivity : ItinBaseActivity() {

    val locationDetailsView by bindView<HotelItinLocationDetails>(R.id.widget_hotel_itin_location_details)
    val hotelImageView by bindView<HotelItinImageWidget>(R.id.hotel_itin_image)
    val checkinCheckoutView by bindView<HotelItinCheckInCheckOutDetails>(R.id.widget_hotel_itin_checkin_checkout_details)
    val toolbar by bindView<HotelItinToolbar>(R.id.widget_itin_toolbar)
    val hotelBookingDetailsView by bindView<HotelItinBookingDetails>(R.id.widget_hotel_itin_booking_details)

    val roomDetailsHeader by bindView<TextView>(R.id.itin_hotel_room_details_header)
    val multiRoomContainer by bindView<LinearLayout>(R.id.hotel_itin_details_multi_room_container)

    val container by bindView<ViewGroup>(R.id.container)

    var itinManager: ItineraryManagerInterface = ItineraryManager.getInstance()

    lateinit var itinCardDataHotel: ItinCardDataHotel

    companion object {
        private const val UNIQUE_ID_EXTRA = "UNIQUE_ID_EXTRA"
        private const val ITIN_ID_EXTRA = "ITIN_ID_EXTRA"
        @JvmStatic
        fun createIntent(context: Context, id: String, itinId: String): Intent {
            val i = Intent(context, HotelItinDetailsActivity::class.java)
            i.putExtra(UNIQUE_ID_EXTRA, id)
            i.putExtra(ITIN_ID_EXTRA, itinId)
            return i
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val readJsonUtil: IJsonToItinUtil
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_card_details)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        readJsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()

        val itin = readJsonUtil.getItin(intent.getStringExtra(ITIN_ID_EXTRA))
        itin?.let { trip ->
            trip.firstHotel()?.let {
                val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(trip, ItinOmnitureUtils.LOB.HOTEL)
                TripsTracking.trackItinHotel(omnitureValues)
            }
        }
        locationDetailsView.taxiSetup(intent.getStringExtra(ITIN_ID_EXTRA))
    }

    override fun onResume() {
        super.onResume()
        onSyncFinish()
    }

    override fun onSyncFinish() {
        val freshItinCardDataHotel = itinManager.getItinCardDataFromItinId(intent.getStringExtra(UNIQUE_ID_EXTRA)) as ItinCardDataHotel?
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
}
