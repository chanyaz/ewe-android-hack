package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.GoogleMapsLiteViewModel
import com.expedia.bookings.utils.bindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GoogleMapsLiteMapView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet), OnMapReadyCallback {

    private val mapView by bindView<MapView>(R.id.google_maps_lite_mapview)
    private val DEFAULT_ZOOM = 14f
    private val DEFAULT_ICON = R.drawable.map_marker_blue
    lateinit private var viewModel: GoogleMapsLiteViewModel

    init {
        View.inflate(context, R.layout.widget_google_maps_lite, this)
        mapView.onCreate(null)
        mapView.isClickable = false
        mapView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    //call this method with a ViewModel to setup the map
    fun setViewModel(vm: GoogleMapsLiteViewModel) {
        viewModel = vm
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        updateCameraPosition(googleMap, viewModel.cameraPositionLatLng)
        for (markerPosition: LatLng in viewModel.markerPositionsLatLng) {
            addMarker(googleMap, markerPosition)
        }
    }

    private fun addMarker(map: GoogleMap?, latLng: LatLng, @DrawableRes icon: Int = DEFAULT_ICON) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(icon))
        map?.addMarker(markerOptions)
    }

    private fun updateCameraPosition(map: GoogleMap?, latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
}