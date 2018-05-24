package com.expedia.bookings.itin.common

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.itin.scopes.ItinExpandedMapViewModelScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IntentableWithType
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.expedia.util.notNullAndObservable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

class ItinExpandedMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    val mapView by bindView<MapView>(R.id.expanded_map_view)
    val directionsButton by bindView<FrameLayout>(R.id.directions_button)
    val directionsButtonText by bindView<TextView>(R.id.directions_button_text)
    val toolbar by bindView<ItinToolbar>(R.id.widget_itin_toolbar)

    private var googleMap: GoogleMap? = null
    private val MAP_ZOOM_LEVEL = 14f
    private lateinit var startPosition: LatLng
    private var fullyTracked = false
    private var zoomTracked = false
    private var panTracked = false
    private var moveStarted = false
    private var currentZoom = 0f

    lateinit var readJsonUtil: IJsonToItinUtil
        @Inject set

    companion object : IntentableWithType {

        private const val ID_EXTRA = "ITINID"
        private const val ITIN_TYPE = "ITIN_TYPE"

        override fun createIntent(context: Context, id: String, type: String): Intent {
            val i = Intent(context, ItinExpandedMapActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            i.putExtra(ITIN_TYPE, type)
            return i
        }
    }

    var viewModel: ItinExpandedMapViewModel<ItinExpandedMapViewModelScope> by notNullAndObservable { vm ->
        vm.toolbarPairSubject.subscribe { pair ->
            pair.first?.let {
                toolbar.toolbarTitleText.text = pair.first
            }
            pair.second?.let {
                toolbar.toolbarSubTitleText.text = pair.second
                toolbar.toolbarSubTitleText.visibility = View.VISIBLE
            }

            toolbar.setNavigationOnClickListener {
                finish()
            }
        }

        vm.latLngSubject.subscribe { latLng ->
            moveMap(latLng)
        }

        directionsButton.subscribeOnClick(vm.directionButtonClickSubject)
        directionsButtonText.setCompoundDrawablesTint(ContextCompat.getColor(this, R.color.white))
        AccessibilityUtil.appendRoleContDesc(directionsButton, directionsButtonText.text.toString(), R.string.accessibility_cont_desc_role_button)
    }

    private fun moveMap(latLng: LatLng) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM_LEVEL))
        addMarker(latLng)
    }

    lateinit var repo: ItinRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.itin_expanded_map)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Ui.getApplication(this).defaultTripComponents()
        Ui.getApplication(this).tripComponent().inject(this)

        val activityLauncher = ActivityLauncher(this)
        repo = ItinRepo(intent.getStringExtra(ID_EXTRA), readJsonUtil, ItineraryManager.getInstance().syncFinishObservable)
        val tripsTracking = TripsTracking

        val scope = ItinExpandedMapViewModelScope(activityLauncher, this, repo, intent.getStringExtra(ITIN_TYPE), tripsTracking)
        viewModel = ItinExpandedMapViewModel(scope)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    override fun onCameraIdle() {
        if (moveStarted) {
            if (!zoomTracked && googleMap?.cameraPosition != null) {
                if (googleMap?.cameraPosition?.zoom != MAP_ZOOM_LEVEL) {
                    zoomTracked = true
                    if (googleMap?.cameraPosition!!.zoom > MAP_ZOOM_LEVEL) {
                        viewModel.trackItinExpandedMapZoomInSubject.onNext(Unit)
                    } else {
                        viewModel.trackItinExpandedMapZoomOutSubject.onNext(Unit)
                    }
                }
            }
            if (checkForPan()) {
                viewModel.trackItinExpandedMapZoomPanSubject.onNext(Unit)
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.isIndoorEnabled = false
        googleMap?.uiSettings?.isTiltGesturesEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.isMyLocationEnabled = havePermissionToAccessLocation(this)
        googleMap?.setOnCameraMoveStartedListener(this)
        googleMap?.setOnCameraIdleListener(this)
        moveMap(viewModel.latLngSubject.value)
        startPosition = googleMap?.cameraPosition!!.target
        currentZoom = MAP_ZOOM_LEVEL
    }

    private fun addMarker(latLng: LatLng) {
        val marker = MarkerOptions()
        marker.position(latLng)
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_blue))
        googleMap?.addMarker(marker)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
        repo.dispose()
    }
}
