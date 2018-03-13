package com.expedia.bookings.itin.hotel.common

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiata.android.Log

class HotelItinExpandedMapActivity : HotelItinBaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    override fun onCameraIdle() {
        if (moveStarted) {
            if (!zoomTracked) {
                if (googleMap?.cameraPosition?.zoom != MAP_ZOOM_LEVEL) {
                    zoomTracked = true
                    if (googleMap?.cameraPosition!!.zoom > MAP_ZOOM_LEVEL) {
                        TripsTracking.trackItinExpandedMapZoomIn()
                    } else {
                        TripsTracking.trackItinExpandedMapZoomOut()
                    }
                }
            }
            if (checkForPan()) {
                TripsTracking.trackItinExpandedMapZoomPan()
                panTracked = true
            }
            if (panTracked && zoomTracked) {
                fullyTracked = true
            }
            moveStarted = false
        }
    }

    fun checkForPan(): Boolean {
        return !panTracked && currentZoom == googleMap?.cameraPosition?.zoom
                && googleMap?.cameraPosition?.target != startPosition
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE && !fullyTracked) {
            moveStarted = true
            currentZoom = googleMap?.cameraPosition!!.zoom
        }
    }

    lateinit var itinCardDataHotel: ItinCardDataHotel
    private val mapView by bindView<MapView>(R.id.expanded_map_view_hotel)
    val directionsButton by bindView<FrameLayout>(R.id.directions_button)
    private val directionsButtonText by bindView<TextView>(R.id.directions_button_text)

    private var googleMap: GoogleMap? = null
    private val MAP_ZOOM_LEVEL = 14f
    private lateinit var startPosition: LatLng
    private var fullyTracked = false
    private var zoomTracked = false
    private var panTracked = false
    private var moveStarted = false
    private var currentZoom = 0f
    private val toolbar by bindView<HotelItinToolbar>(R.id.widget_hotel_itin_toolbar)
    private val LOGGING_TAG = "HotelItinExpandedMapActivity"

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
            i.putExtra(ID_EXTRA, id)
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
            val hotelLat: Double? = itinCardDataHotel.propertyLocation.latitude
            val hotelLong: Double? = itinCardDataHotel.propertyLocation.longitude
            val propertyName: String? = itinCardDataHotel.propertyName

            val locationData = buildUriForHotel(hotelLat, hotelLong, propertyName)
            if (locationData != null) {
                val intent = Intent(Intent.ACTION_VIEW, locationData)
                intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                intent.flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                intent.data = locationData
                if (intent.resolveActivity(packageManager) != null) {
                    this.startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.itin_hotel_map_directions_no_app_available, Toast.LENGTH_SHORT).show()
                }
            }
            TripsTracking.trackItinHotelMapDirectionsButton()
        }
    }

    fun buildUriForHotel(hotelLat: Double?, hotelLong: Double?, propertyName: String?): Uri? {
        try {
            var urlEncodedPropertyName = ""
            if (!propertyName.isNullOrEmpty()) {
                urlEncodedPropertyName = Uri.encode(propertyName)
            }
            if (hotelLat != null && hotelLong != null) {
                return Uri.parse("geo:$hotelLat,$hotelLong?q=$urlEncodedPropertyName")
            }
        } catch (e: Exception) {
            Log.d(LOGGING_TAG, e.printStackTrace().toString())
        }
        return null
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.isIndoorEnabled = false
        googleMap?.uiSettings?.isTiltGesturesEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.isMyLocationEnabled = havePermissionToAccessLocation(this)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(getHotelLatLong(), MAP_ZOOM_LEVEL))
        addMarker(map)
        googleMap?.setOnCameraMoveStartedListener(this)
        googleMap?.setOnCameraIdleListener(this)
        startPosition = googleMap?.cameraPosition!!.target
        currentZoom = MAP_ZOOM_LEVEL
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
