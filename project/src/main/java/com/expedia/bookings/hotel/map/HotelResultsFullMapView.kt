package com.expedia.bookings.hotel.map

import android.content.Context
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.util.PermissionsUtils
import com.expedia.util.notNullAndObservable
import com.expedia.vm.hotel.HotelResultsMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.mobiata.android.LocationServices
import org.joda.time.DateTime

class HotelResultsFullMapView(context: Context, attrs: AttributeSet?) : MapView(context, attrs),
        OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<MapItem>? = null
    private lateinit var hotelMapClusterRenderer: HotelMapClusterRenderer

    var mapItems = arrayListOf<MapItem>()

    val mapViewModel: HotelResultsMapViewModel

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        this.googleMap = googleMap

        initMapSettings()
        initClusterManagement()
        addMapListeners()

        //todo wtf
//        mapView.viewTreeObserver.addOnGlobalLayoutListener(mapViewLayoutReadyListener)
    }

    init {
        mapViewModel = HotelResultsMapViewModel(context, lastBestLocationSafe())
    }

    fun clearMarkers() {
        clusterManager?.let { clusterManager ->
            mapItems.clear()

            clusterManager.clearItems()
            clusterManager.cluster()
            //mapCarouselContainer.visibility = View.INVISIBLE
        }
    }

    fun moveCamera(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(8f)
                .build()
        googleMap?.setPadding(0, 0, 0, 0)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun clearPreviousMarker() {
        val prevMapItem = mapViewModel.selectedMapMarker
        if (prevMapItem != null) {
            prevMapItem.isSelected = false
            if (!prevMapItem.hotel.isSoldOut) {
                hotelMapClusterRenderer.getMarker(prevMapItem)?.setIcon(prevMapItem.getHotelMarkerIcon())
            }
        }
    }

    private fun initMapSettings() {
        if (PermissionsUtils.havePermissionToAccessLocation(context)) {
            googleMap?.isMyLocationEnabled = true
        }
        val uiSettings = googleMap?.uiSettings
        //Explicitly disallow map-cluttering ui (but keep the gestures)
        if (uiSettings != null) {
            uiSettings.isCompassEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isIndoorLevelPickerEnabled = false
        }

        // todo find out what this is for.
        googleMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker): View? {
                val activity = context as AppCompatActivity
                val v = activity.layoutInflater.inflate(R.layout.marker_window, null)
                return v
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })
    }

    private fun initClusterManagement() {
        clusterManager = ClusterManager(context, googleMap)
        clusterManager!!.setAlgorithm(HotelMapClusterAlgorithm())
//        hotelMapClusterRenderer = HotelMapClusterRenderer(context, googleMap, clusterManager!!,
//                mapViewModel.clusterChangeSubject)
        clusterManager!!.setRenderer(hotelMapClusterRenderer)

        /*
        clusterManager!!.setOnClusterItemClickListener {
            trackMapPinTap()
            selectMarker(it)
            updateCarouselItems()
            true
        }

        clusterManager!!.setOnClusterClickListener {
            animateMapCarouselOut()
            clearPreviousMarker()
            val builder = LatLngBounds.builder()
            it.items.forEach { item ->
                builder.include(item.pos)
            }
            val bounds = builder.build()

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, resources.displayMetrics.density.toInt() * 50), object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                }

                override fun onCancel() {
                }
            })
            true
        }
         */
    }

    private fun addMapListeners() {
        var currentZoom = -1f

        googleMap?.setOnCameraChangeListener { position ->
            synchronized(currentZoom) {
                if (Math.abs(currentZoom - position.zoom) > .5) {
//                    clusterMarkers()
                    currentZoom = position.zoom
                }

                //todo for full screen this should be always.
//                if (Strings.equals(currentState, ResultsMap::class.java.name)) {
//                    showSearchThisArea()
//                }
            }
        }
        googleMap?.setOnMarkerClickListener(clusterManager!!)

    }

    private fun lastBestLocationSafe(): Location {
        val minTime = DateTime.now().millis - DateUtils.HOUR_IN_MILLIS
        val loc = LocationServices.getLastBestLocation(context, minTime)
        val location = Location("lastBestLocationSafe")
        location.latitude = loc?.latitude ?: 0.0
        location.longitude = loc?.longitude ?: 0.0
        return location
    }
}