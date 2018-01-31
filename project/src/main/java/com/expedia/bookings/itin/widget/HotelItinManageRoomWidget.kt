package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.vm.HotelItinManageRoomViewModel
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class HotelItinManageRoomWidget(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {
    val roomDetailsView by bindView<HotelItinRoomDetails>(R.id.widget_hotel_itin_room_details)
    val hotelManageBookingHelpView by bindView<HotelItinManageBookingHelp>(R.id.widget_hotel_itin_manage_booking_help)
    val hotelCustomerSupportDetailsView by bindView<HotelItinCustomerSupportDetails>(R.id.widget_hotel_itin_customer_support)
    val manageBookingButton by bindView<Button>(R.id.itin_hotel_manage_booking_button)

    var viewModel: HotelItinManageRoomViewModel by notNullAndObservable { vm ->
        vm.roomDetailsSubject.subscribe { room ->
            roomDetailsView.setUpRoomAndOccupantInfo(room)
            roomDetailsView.expandRoomDetailsView()
            hotelManageBookingHelpView.showConfirmationNumberIfAvailable(room.hotelConfirmationNumber)
        }

        vm.roomChangeAndCancelRulesSubject.subscribe {
            roomDetailsView.setupAndShowChangeAndCancelRules(it)
        }

        vm.itinCardDataHotelSubject.subscribe { itinCardDataHotel ->
            manageBookingButton.setOnClickListener {
                (context as AppCompatActivity).startActivityForResult(buildWebViewIntent(R.string.itin_hotel_manage_booking_webview_title, itinCardDataHotel.detailsUrl, "overview-header", itinCardDataHotel.tripNumber).intent, Constants.ITIN_HOTEL_WEBPAGE_CODE)
            }
            hotelManageBookingHelpView.setUpWidget(itinCardDataHotel)
            hotelCustomerSupportDetailsView.setUpWidget(itinCardDataHotel.tripNumber)
        }
    }

    init {
        View.inflate(context, R.layout.hotel_itin_manage_room_widget, this)
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?, tripId: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setHotelItinTripId(tripId)
        return builder
    }
}
