package com.expedia.bookings.itin.hotel.details

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.hotel.common.HotelItinExpandedMapActivity
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.itin.common.GoogleMapsLiteViewModel
import com.expedia.bookings.itin.common.GoogleMapsLiteMapView
import com.expedia.bookings.itin.hotel.taxi.HotelItinTaxiActivity
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Ui.getApplication
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.squareup.phrase.Phrase

class HotelItinLocationDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val mapView by bindView<GoogleMapsLiteMapView>(R.id.widget_hotel_itin_map)
    val address: LinearLayout by bindView(R.id.widget_hotel_itin_address)
    val addressLine1: TextView by bindView(R.id.widget_hotel_itin_address_line_1)
    val addressLine2: TextView by bindView(R.id.widget_hotel_itin_address_line_2)
    val directionsButton: ImageView by bindView(R.id.hotel_directions_button)
    val taxiButton: TextView by bindView(R.id.taxi_button)
    val taxiContainer: LinearLayout by bindView(R.id.taxi_container)
    var gsonUtil: IJsonToItinUtil = getApplication(context).appComponent().jsonUtilProvider()
    var tripTracking: ITripsTracking = TripsTracking

    init {
        View.inflate(context, R.layout.widget_hotel_itin_location_details, this)
    }

    fun setupWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.propertyLocation != null) {
            val mapVm = GoogleMapsLiteViewModel(
                    listOf(LatLng(itinCardDataHotel.propertyLocation.latitude, itinCardDataHotel.propertyLocation.longitude))
            )
            mapView.setViewModel(mapVm)
            mapView.setOnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                TripsTracking.trackItinHotelExpandMap()
            }
            directionsButton.contentDescription = context.getString(R.string.itin_action_directions)
            directionsButton.setOnClickListener {
                context.startActivity(HotelItinExpandedMapActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                TripsTracking.trackItinHotelDirections()
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
    }

    fun taxiSetup(itinId: String?) {
        val itinLocalLanguage = gsonUtil.getItin(itinId)?.firstHotel()?.localizedHotelPropertyInfo?.localizationLanguage
        val eligibiltyForTaxiCard = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelTripTaxiCard) && itinLocalLanguage != null
        if (eligibiltyForTaxiCard && itinId != null) {
            taxiContainer.visibility = View.VISIBLE
            taxiButton.text = itinLocalLanguage
            AccessibilityUtil.appendRoleContDesc(taxiButton, R.string.accessibility_cont_desc_role_button)
            taxiButton.setOnClickListener {
                val intent = HotelItinTaxiActivity.createIntent(context, itinId)
                context.startActivity(intent)
                tripTracking.trackHotelTaxiCardClick()
            }
        }
    }
}
