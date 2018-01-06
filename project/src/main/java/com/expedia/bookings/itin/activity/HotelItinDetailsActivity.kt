package com.expedia.bookings.itin.activity

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.itin.widget.HotelItinBookingDetails
import com.expedia.bookings.itin.widget.HotelItinCheckInCheckOutDetails
import com.expedia.bookings.itin.widget.HotelItinImage
import com.expedia.bookings.itin.widget.HotelItinLocationDetails
import com.expedia.bookings.itin.widget.HotelItinRoomDetails
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView

open class HotelItinDetailsActivity : HotelItinBaseActivity() {

    val locationDetailsView by bindView<HotelItinLocationDetails>(R.id.widget_hotel_itin_location_details)
    val roomDetailsView by bindView<HotelItinRoomDetails>(R.id.widget_hotel_itin_room_details)
    val roomDetailsHeader by bindView<View>(R.id.itin_hotel_room_details_header)
    val hotelImageView by bindView<HotelItinImage>(R.id.hotel_itin_image)
    val checkinCheckoutView by bindView<HotelItinCheckInCheckOutDetails>(R.id.widget_hotel_itin_checkin_checkout_details)
    val toolbar by bindView<HotelItinToolbar>(R.id.widget_hotel_itin_toolbar)
    val hotelBookingDetailsView by bindView<HotelItinBookingDetails>(R.id.widget_hotel_itin_booking_details)

    val roomDetailsChevron by bindView<ImageView>(R.id.itin_hotel_room_details_chevron)

    val container by bindView<ViewGroup>(R.id.container)

    lateinit var itinCardDataHotel: ItinCardDataHotel

    companion object {
        private const val ITIN_ID_EXTRA = "ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
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
        updateItinCardDataHotel()
    }

    override fun updateItinCardDataHotel() {
        val freshItinCardDataHotel = getItineraryManager().getItinCardDataFromItinId(intent.getStringExtra(ITIN_ID_EXTRA)) as ItinCardDataHotel?
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
        roomDetailsView.setUpWidget(itinCardDataHotel)
        if (itinCardDataHotel.lastHotelRoom != null) {
            roomDetailsView.collapseRoomDetailsView()
            roomDetailsChevron.visibility = View.VISIBLE
            roomDetailsView.isRowClickable = true
        }
        hotelBookingDetailsView.setUpWidget(itinCardDataHotel)
        toolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName)
        toolbar.setNavigationOnClickListener {
            super.finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }
        if (itinCardDataHotel.isSharedItin) {
            roomDetailsHeader.visibility = View.GONE
        } else if (ProductFlavorFeatureConfiguration.getInstance().shouldShowItinShare()) {
            toolbar.showShare()
        }

        OmnitureTracking.trackItinHotel(hotelHasMessagingURL())
    }

    private fun hotelHasMessagingURL(): Boolean = itinCardDataHotel.property.hasHotelMessagingUrl()
}
