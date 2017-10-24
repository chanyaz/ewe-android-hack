package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.EBRequestParams
import com.expedia.bookings.data.trips.EventbriteResponse
import com.expedia.bookings.data.trips.TcsRequestParams
import com.expedia.bookings.data.trips.TcsResponse
import com.expedia.bookings.data.trips.Trail
import com.expedia.bookings.data.trips.TrailsRequestParams
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinToolbar
import com.expedia.bookings.services.TripsHotelMapServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.Locale


class HotelItinExpandedMapActivity : HotelItinBaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    override fun onCameraIdle() {
        if (moveStarted) {
            if (!zoomTracked) {
                if (googleMap?.cameraPosition?.zoom != MAP_ZOOM_LEVEL) {
                    zoomTracked = true
                    if (googleMap?.cameraPosition!!.zoom > MAP_ZOOM_LEVEL) {
                        OmnitureTracking.trackItinExpandedMapZoomIn()
                    } else {
                        OmnitureTracking.trackItinExpandedMapZoomOut()
                    }
                }
            }
            if (checkForPan()) {
                OmnitureTracking.trackItinExpandedMapZoomPan()
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
    private lateinit var startPosition: LatLng
    private var fullyTracked = false
    private var zoomTracked = false
    private var panTracked = false
    private var moveStarted = false
    private var currentZoom = 0f
    private val toolbar: HotelItinToolbar by lazy {
        findViewById(R.id.widget_hotel_itin_toolbar) as HotelItinToolbar
    }

    private val tripsHotelMapServices: TripsHotelMapServices by lazy {
        TripsHotelMapServices(Schedulers.io(), AndroidSchedulers.mainThread())
    }
    var compositeSubscription: CompositeSubscription = CompositeSubscription()

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

        compositeSubscription.add(tripsHotelMapServices.getPoiNearby(
                TcsRequestParams(itinCardDataHotel.propertyLocation.latitude.toString(),
                        itinCardDataHotel.propertyLocation.longitude.toString(),
                        TripsHotelMapServices.Keys.TCS.value,
                        "EN",
                        arrayOf("POI"),
                        2,
                        true),
                poiObserver))

        compositeSubscription.add(tripsHotelMapServices.getEvents(EBRequestParams(
                itinCardDataHotel.propertyLocation.latitude,
                itinCardDataHotel.propertyLocation.longitude,
                "5mi",
                itinCardDataHotel.startDate.toString(ISODateTimeFormat.dateHourMinuteSecond()),
                itinCardDataHotel.endDate.plusDays(1).toString(ISODateTimeFormat.dateHourMinuteSecond()),
                "venue"),
                ebObserver))

        compositeSubscription.add(tripsHotelMapServices.getTrails(
                TrailsRequestParams(
                        itinCardDataHotel.propertyLocation.latitude.toString(),
                        itinCardDataHotel.propertyLocation.longitude.toString(),
                        TripsHotelMapServices.Keys.TRAILS.value,
                        "50",
                        "75"
                ), trailsObserver)
        )
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

            val uri = String.format(Locale.getDefault(), "geo:0,0?q=") + android.net.Uri.encode(String.format("%s@%f,%f", propertyName, hotelLat, hotelLong), "UTF-8")
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
        addMarker(R.drawable.ic_hotel_pin, getHotelLatLong())
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

    private fun addMarker(icon: Int, latlong: LatLng) {
        val marker = MarkerOptions()
        marker.position(latlong)
        marker.icon(bitmapDescriptorFromVector(this, icon))
        googleMap?.addMarker(marker)
    }

    private fun getHotelLatLong(): LatLng {
        val hotelLat = itinCardDataHotel.propertyLocation.latitude
        val hotelLong = itinCardDataHotel.propertyLocation.longitude
        return LatLng(hotelLat, hotelLong)
    }

    private val poiObserver: Observer<TcsResponse> = object : Observer<TcsResponse> {
        override fun onNext(t: TcsResponse?) {
            //Example: fetching the description of the first item from the response
            if (t != null) {
                val pointsOfInterest = t.sections.poi.data
                for (poi in pointsOfInterest) {
                    val lat = poi.geo.latitude.toDouble()
                    val long = poi.geo.longitude.toDouble()
                    val latLong = LatLng(lat, long)
                    addMarker(R.drawable.ic_landmark_pin, latLong)
                }
                Log.d("TCSRESPONSE: ", t.sections.poi.data[0].descriptions.data[0].value)
            }
        }

        override fun onError(e: Throwable?) {
            Log.d("TCSRESPONSE: ", e?.toString())
        }

        override fun onCompleted() {
        }
    }

    private val ebObserver: Observer<EventbriteResponse> = object : Observer<EventbriteResponse> {
        override fun onError(e: Throwable?) {
            Log.d("EBRESPONSE: ", e?.toString())
        }

        override fun onNext(t: EventbriteResponse?) {
            if (t != null) {
                val events = t.events
                for (event in events) {
                    val lat = event.venue.latitude
                    val long = event.venue.longitude
                    val latLong = LatLng(lat, long)
                    addMarker(R.drawable.ic_music_pin, latLong)
                }
                Log.d("EBRESPONSE: ", t.events[0].name.text)
            }
        }

        override fun onCompleted() {
        }
    }

    private val trailsObserver: Observer<Array<Trail>> = object : Observer<Array<Trail>> {
        override fun onError(e: Throwable?) {
            Log.d("TRAILSRESPONSE: ", e?.toString())
        }

        override fun onNext(trails: Array<Trail>?) {
            if (trails != null) {
                for (trail: Trail in trails) {
                    val lat = trail.latitude.toDouble()
                    val long = trail.longitude.toDouble()
                    val latLong = LatLng(lat, long)
                    addMarker(R.drawable.ic_trail_pin, latLong)
                    Log.d("TRAILSRESPONSE: ", trail.id)
                }
            }
        }

        override fun onCompleted() {
        }

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.unsubscribe()
    }
}
