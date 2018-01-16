package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.activity.HotelItinExpandedMapActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.vm.GoogleMapsLiteViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ItinActionsSection
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.SummaryButton
import com.google.android.gms.maps.model.LatLng
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinLocationDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val mapView by bindView<GoogleMapsLiteMapView>(R.id.widget_hotel_itin_map)
    val address: LinearLayout by bindView(R.id.widget_hotel_itin_address)
    val addressLine1: TextView by bindView(R.id.widget_hotel_itin_address_line_1)
    val addressLine2: TextView by bindView(R.id.widget_hotel_itin_address_line_2)
    val actionButtons: ItinActionsSection by bindView(R.id.action_button_layout)
    val directionsButton: ImageView by bindView(R.id.hotel_directions_button)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_location_details, this)
    }

    fun setupWidget(itinCardDataHotel: ItinCardDataHotel) {
        val phoneNumber: String
        val callActionButton: SummaryButton
        if (itinCardDataHotel.propertyLocation != null) {
            val mapVm = GoogleMapsLiteViewModel(
                    listOf(LatLng(itinCardDataHotel.propertyLocation.latitude, itinCardDataHotel.propertyLocation.longitude))
            )
            mapView.setViewModel(mapVm)
            mapView.setOnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                OmnitureTracking.trackItinHotelExpandMap()
            }
            if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppTripsMessageHotel, R.string.preference_enable_trips_hotel_messaging)) {
                directionsButton.visibility = View.VISIBLE
                directionsButton.setOnClickListener {
                    context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                    OmnitureTracking.trackItinHotelDirections()
                }
            }
        }
        addressLine1.text = itinCardDataHotel.propertyLocation.streetAddressString
        addressLine2.text = itinCardDataHotel.propertyLocation.toTwoLineAddressFormattedString()
        val textToCopy: String = Phrase.from(context, R.string.itin_hotel_details_address_clipboard_TEMPLATE)
                .put("addresslineone", addressLine1.text.toString())
                .put("addresslinetwo", addressLine2.text.toString()).format().toString()
        address.setOnClickListener {
            ClipboardUtils.setText(context, textToCopy)
            Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
        address.contentDescription = Phrase.from(context, R.string.itin_hotel_details_address_copy_content_description_TEMPLATE)
                .put("address", textToCopy).format().toString()

        if (!FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppTripsMessageHotel, R.string.preference_enable_trips_hotel_messaging)) {
            actionButtons.visibility = View.VISIBLE
            phoneNumber = itinCardDataHotel.localPhone
            callActionButton = SummaryButton(R.drawable.itin_call_hotel, phoneNumber, Phrase.from(context, R.string.itin_hotel_details_call_button_content_description_TEMPLATE).put("phonenumber", phoneNumber).format().toString(), OnClickListener {
                if (phoneNumber.isNotEmpty()) {
                    val pm = context.packageManager
                    if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                        SocialUtils.call(context, phoneNumber)
                    } else {
                        ClipboardUtils.setText(context, phoneNumber)
                        Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                    }
                }
            })

            val directionsActionButton = SummaryButton(R.drawable.ic_directions_icon_cta_button, context.getString(R.string.itin_action_directions), OnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                OmnitureTracking.trackItinHotelDirections()
            })
            if (phoneNumber.isEmpty()) actionButtons.bind(null, directionsActionButton) else actionButtons.bind(callActionButton, directionsActionButton)
            actionButtons.getmRightButton().setCompoundDrawablesTint(ContextCompat.getColor(context, R.color.app_primary))
        }
    }
}
