package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.ItinToolbar
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.util.havePermissionToAccessLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale


class HotelItinExpandedMapActivity : HotelItinBaseActivity(), OnMapReadyCallback {

    lateinit var itinCardDataHotel: ItinCardDataHotel
    private val mapView: MapView by lazy {
        findViewById(R.id.expanded_map_view_hotel) as MapView
    }
    val directionsButton: FrameLayout by lazy {
        findViewById(R.id.directions_button) as FrameLayout
    }
    private val directionsButtonText: TextView by lazy {
        findViewById(R.id.directions_button_text) as TextView
    }

    private var googleMap: GoogleMap? = null
    private val MAP_ZOOM_LEVEL = 14f
    private val toolbar: ItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as ItinToolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultTripComponents()
        setContentView(R.layout.hotel_itin_expanded_map)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        updateItinCardDataHotel()
    }

    companion object {
        private const val ID_EXTRA = "ITINID"

        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinExpandedMapActivity::class.java)
            i.putExtra(HotelItinExpandedMapActivity.ID_EXTRA, id)
            return i
        }
    }

    fun setUpWidgets(itinCardDataHotel: ItinCardDataHotel) {
        toolbar.setUpWidget(itinCardDataHotel, itinCardDataHotel.propertyName, itinCardDataHotel.propertyLocation.toCityStateCountryAddressFormattedString())
        toolbar.setNavigationOnClickListener {
            super.finish()
            overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        }
        directionsButtonText.setCompoundDrawablesTint(ContextCompat.getColor(this, R.color.white))
        AccessibilityUtil.appendRoleContDesc(directionsButton, directionsButtonText.text.toString(), R.string.accessibility_cont_desc_role_button)
        directionsButton.setOnClickListener {
            val hotelLat = itinCardDataHotel.propertyLocation.latitude
            val hotelLong = itinCardDataHotel.propertyLocation.longitude
            val propertyName = itinCardDataHotel.propertyName

            val uri = String.format(Locale.getDefault(), "geo:0,0?q=") + android.net.Uri.encode(String.format("%s@%f,%f", propertyName, hotelLat, hotelLong), "UTF-8");
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
            intent.flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
            intent.data = Uri.parse(uri)
            this.startActivity(intent)

            OmnitureTracking.trackItinHotelDirectionsButton()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.isMyLocationEnabled = havePermissionToAccessLocation(this)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(getHotelLatLong(), MAP_ZOOM_LEVEL))
        addMarker(map)
    }

    override fun updateItinCardDataHotel() {
        val freshItinCardDataHotel = getItineraryManager().getItinCardDataFromItinId(intent.getStringExtra(ID_EXTRA)) as ItinCardDataHotel?
        if (freshItinCardDataHotel == null) {
            finish()
        } else {
            itinCardDataHotel = freshItinCardDataHotel
            setUpWidgets(itinCardDataHotel)
        }
    }

    private fun addMarker(map: GoogleMap) {
        val marker = MarkerOptions()
        marker.position(getHotelLatLong())
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_blue))
        map.addMarker(marker)
    }

    private fun getHotelLatLong(): LatLng {
        val hotelLat = itinCardDataHotel.propertyLocation.latitude
        val hotelLong = itinCardDataHotel.propertyLocation.longitude
        return LatLng(hotelLat, hotelLong)
    }
}