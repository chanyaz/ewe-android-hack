package com.expedia.bookings.itin.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions

class GoogleMapsLiteMapView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet), OnMapReadyCallback {

    private val mapView by bindView<MapView>(R.id.google_maps_lite_mapview)
    private val DEFAULT_ZOOM = 14f
    private val DEFAULT_ICON = R.drawable.map_marker_blue
    private val AIRPORT_ICON = R.drawable.flight_itin_map_airport_icon
    private lateinit var viewModel: GoogleMapsLiteViewModel

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
        googleMap?.clear()
        googleMap?.isIndoorEnabled = false
        googleMap?.uiSettings?.isTiltGesturesEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        val markerPositions = viewModel.markerPositionsLatLng
        if (markerPositions.size == 1) {
            val markerPosition = markerPositions[0]
            updateCameraPosition(googleMap, markerPosition)
            addMarker(googleMap, markerPosition)
        } else {
            updateCameraPosition(googleMap, markerPositions)
            addPolyline(googleMap, markerPositions)
        }
    }

    private fun addPolyline(map: GoogleMap?, markerPositions: List<LatLng>) {
        markerPositions.indices.forEach {
            if (it + 1 < markerPositions.size) {
                val options = PolylineOptions()
                options.add(markerPositions[it])
                options.add(markerPositions[it + 1])
                options.color(ContextCompat.getColor(context, R.color.blue4))
                        .width(8f)
                        .pattern(listOf<PatternItem>(Dash(30f), Gap(20f)))
                        .jointType(JointType.ROUND)
                        .geodesic(true)
                        .startCap(CustomCap(bitmapDescriptorFromVector(context, AIRPORT_ICON), 6f))
                        .endCap(CustomCap(bitmapDescriptorFromVector(context, AIRPORT_ICON), 6f))
                map?.addPolyline(options)
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        vectorDrawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addMarker(map: GoogleMap?, latLng: LatLng) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(DEFAULT_ICON))
        map?.addMarker(markerOptions)
    }

    //when there is one marker, use default zoom
    private fun updateCameraPosition(map: GoogleMap?, position: LatLng) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM)
        map?.moveCamera(cameraUpdate)
    }

    //when there are multiple markers, use bounds
    private fun updateCameraPosition(map: GoogleMap?, listOfPositions: List<LatLng>) {
        val boundsBuilder = LatLngBounds.builder()
        listOfPositions.forEach {
            boundsBuilder.include(it)
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener {
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 4)
            map?.animateCamera(cameraUpdate)
        }
    }
}
